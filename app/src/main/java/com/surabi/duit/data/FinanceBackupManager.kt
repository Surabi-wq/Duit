package com.surabi.duit.data

import android.database.Cursor
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Full offline backup/restore for the Room database.
 *
 * The archive contains every application-owned SQLite table and its typed values.
 * Room's schema metadata and SQLite's internal sequence tables are deliberately
 * excluded: the installed app remains responsible for schema migrations.
 */
object FinanceBackupManager {
    private const val FORMAT_NAME = "DUIT_FULL_BACKUP"
    private const val FORMAT_VERSION = 1
    private const val DATABASE_VERSION = 5
    private const val DATA_MAGIC = 0x44554954 // DUIT
    private const val MANIFEST_ENTRY = "manifest.properties"
    private const val DATA_ENTRY = "tables.bin"
    private const val MAX_TABLES = 64
    private const val MAX_COLUMNS = 256
    private const val MAX_ROWS_PER_TABLE = 5_000_000L
    private const val MAX_VALUE_BYTES = 64 * 1024 * 1024

    data class BackupSummary(
        val appVersion: String,
        val databaseVersion: Int,
        val createdAt: Long,
        val tableCount: Int,
        val totalRows: Long
    )

    private data class TableSchema(val name: String, val columns: List<String>)

    /** Writes a consistent, compressed snapshot while Room holds a database transaction. */
    suspend fun createBackup(
        database: FinanceDatabase,
        output: OutputStream,
        appVersion: String
    ): BackupSummary = database.withTransaction {
        val sql = database.openHelper.writableDatabase
        val currentVersion = sql.version
        check(currentVersion == DATABASE_VERSION) {
            "Versi database tidak didukung untuk backup ini: $currentVersion."
        }

        val schemas = applicationTables(sql).map { name ->
            TableSchema(name, tableColumns(sql, name))
        }
        require(schemas.isNotEmpty()) { "Tidak ada tabel data Duit yang dapat dicadangkan." }
        val rowCounts = schemas.associate { schema -> schema.name to countRows(sql, schema.name) }
        val createdAt = System.currentTimeMillis()
        val summary = BackupSummary(
            appVersion = appVersion.ifBlank { "unknown" },
            databaseVersion = currentVersion,
            createdAt = createdAt,
            tableCount = schemas.size,
            totalRows = rowCounts.values.sum()
        )

        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            val manifest = Properties().apply {
                setProperty("format", FORMAT_NAME)
                setProperty("formatVersion", FORMAT_VERSION.toString())
                setProperty("appVersion", summary.appVersion)
                setProperty("databaseVersion", summary.databaseVersion.toString())
                setProperty("createdAt", summary.createdAt.toString())
                setProperty("tableCount", summary.tableCount.toString())
                rowCounts.forEach { (table, count) -> setProperty("rows.$table", count.toString()) }
            }
            manifest.store(zip, "Duit full offline backup")
            zip.closeEntry()

            zip.putNextEntry(ZipEntry(DATA_ENTRY))
            val data = DataOutputStream(zip)
            data.writeInt(DATA_MAGIC)
            data.writeInt(FORMAT_VERSION)
            data.writeInt(schemas.size)

            // All schemas are written before the rows so restore can validate compatibility
            // before deleting a single record from the current database.
            schemas.forEach { schema ->
                writeString(data, schema.name)
                data.writeInt(schema.columns.size)
                schema.columns.forEach { writeString(data, it) }
            }

            schemas.forEach { schema ->
                val expectedRows = rowCounts.getValue(schema.name)
                data.writeLong(expectedRows)
                sql.query("SELECT * FROM ${quoteIdentifier(schema.name)}").use { cursor ->
                    var written = 0L
                    while (cursor.moveToNext()) {
                        writeRow(data, cursor)
                        written++
                    }
                    check(written == expectedRows) {
                        "Jumlah baris tabel ${schema.name} berubah saat backup."
                    }
                }
            }
            data.flush()
            zip.closeEntry()
        }
        summary
    }

    /** Reads and validates human-readable backup metadata without modifying app data. */
    fun inspectBackup(input: InputStream): BackupSummary {
        ZipInputStream(BufferedInputStream(input)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: error("File bukan backup Duit yang valid.")
                if (entry.name == MANIFEST_ENTRY) {
                    val properties = Properties().apply { load(zip) }
                    zip.closeEntry()
                    validateManifest(properties)
                    val tableCount = properties.getProperty("tableCount").toInt()
                    val totalRows = properties.stringPropertyNames()
                        .filter { it.startsWith("rows.") }
                        .sumOf { key ->
                            properties.getProperty(key).toLongOrNull()
                                ?: error("Metadata jumlah data pada backup rusak.")
                        }
                    return BackupSummary(
                        appVersion = properties.getProperty("appVersion", "unknown"),
                        databaseVersion = properties.getProperty("databaseVersion").toInt(),
                        createdAt = properties.getProperty("createdAt").toLong(),
                        tableCount = tableCount,
                        totalRows = totalRows
                    )
                }
                zip.closeEntry()
            }
        }
    }

    /**
     * Replaces all current application data atomically. Any invalid archive, incompatible
     * schema, failed insert, or foreign-key violation rolls the transaction back.
     */
    suspend fun restoreBackup(database: FinanceDatabase, input: InputStream): BackupSummary =
        database.withTransaction {
            val sql = database.openHelper.writableDatabase
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                val manifestEntry = findEntry(zip, MANIFEST_ENTRY)
                val properties = Properties().apply { load(zip) }
                zip.closeEntry()
                validateManifest(properties)

                val backupVersion = properties.getProperty("databaseVersion").toInt()
                require(backupVersion == sql.version && backupVersion == DATABASE_VERSION) {
                    "Backup memakai versi database $backupVersion, sedangkan aplikasi ini memakai ${sql.version}. " +
                        "Gunakan versi aplikasi yang kompatibel."
                }

                val dataEntry = findEntry(zip, DATA_ENTRY)
                val data = DataInputStream(zip)
                require(data.readInt() == DATA_MAGIC) { "Isi backup tidak dikenali." }
                require(data.readInt() == FORMAT_VERSION) { "Versi format backup tidak didukung." }
                val archivedTableCount = data.readInt()
                require(archivedTableCount in 1..MAX_TABLES) { "Jumlah tabel pada backup tidak valid." }

                val archivedSchemas = buildList {
                    repeat(archivedTableCount) {
                        val name = readString(data)
                        val columnCount = data.readInt()
                        require(columnCount in 1..MAX_COLUMNS) { "Struktur kolom backup tidak valid." }
                        val columns = List(columnCount) { readString(data) }
                        require(columns.distinct().size == columns.size) {
                            "Backup berisi nama kolom yang berulang."
                        }
                        add(TableSchema(name, columns))
                    }
                }
                require(archivedSchemas.map { it.name }.distinct().size == archivedSchemas.size) {
                    "Backup berisi tabel yang berulang."
                }

                // Compare all table and column names before clearing existing data.
                val currentSchemas = applicationTables(sql).associateWith { tableColumns(sql, it) }
                require(archivedSchemas.map { it.name }.toSet() == currentSchemas.keys) {
                    "Struktur tabel backup tidak cocok dengan database aplikasi ini."
                }
                archivedSchemas.forEach { archived ->
                    val currentColumns = currentSchemas[archived.name]
                        ?: error("Tabel ${archived.name} tidak tersedia.")
                    require(archived.columns.toSet() == currentColumns.toSet()) {
                        "Struktur kolom tabel ${archived.name} tidak cocok. Data saat ini tidak diubah."
                    }
                }

                // Defer relationships until commit so restore is independent of table order.
                sql.execSQL("PRAGMA defer_foreign_keys = ON")
                currentSchemas.keys.forEach { table ->
                    sql.execSQL("DELETE FROM ${quoteIdentifier(table)}")
                }

                val restoredCounts = linkedMapOf<String, Long>()
                archivedSchemas.forEach { schema ->
                    val rowCount = data.readLong()
                    require(rowCount in 0..MAX_ROWS_PER_TABLE) {
                        "Jumlah baris tabel ${schema.name} pada backup tidak masuk akal."
                    }
                    val expectedRows = properties.getProperty("rows.${schema.name}")?.toLongOrNull()
                        ?: error("Metadata tabel ${schema.name} tidak tersedia.")
                    require(rowCount == expectedRows) {
                        "Jumlah data tabel ${schema.name} tidak cocok dengan manifest backup."
                    }
                    val statement = sql.compileStatement(
                        "INSERT INTO ${quoteIdentifier(schema.name)} (" +
                            schema.columns.joinToString(",") { quoteIdentifier(it) } +
                            ") VALUES (" + List(schema.columns.size) { "?" }.joinToString(",") + ")"
                    )
                    try {
                        repeat(rowCount.toInt()) {
                            statement.clearBindings()
                            repeat(schema.columns.size) { columnIndex ->
                                bindValue(data, statement, columnIndex + 1)
                            }
                            statement.executeInsert()
                        }
                    } finally {
                        statement.close()
                    }
                    restoredCounts[schema.name] = rowCount
                }

                require(data.read() == -1) { "Ada data tambahan yang tidak dikenal pada arsip backup." }
                zip.closeEntry() // Forces ZIP CRC verification before the transaction can commit.
                val foreignKeyViolations = sql.query("PRAGMA foreign_key_check").use { cursor ->
                    cursor.count
                }
                check(foreignKeyViolations == 0) {
                    "Pemulihan dibatalkan karena ditemukan relasi data yang tidak valid."
                }

                BackupSummary(
                    appVersion = properties.getProperty("appVersion", "unknown"),
                    databaseVersion = backupVersion,
                    createdAt = properties.getProperty("createdAt").toLong(),
                    tableCount = restoredCounts.size,
                    totalRows = restoredCounts.values.sum()
                )
            }
        }

    private fun validateManifest(properties: Properties) {
        require(properties.getProperty("format") == FORMAT_NAME) {
            "File ini bukan arsip backup Duit."
        }
        require(properties.getProperty("formatVersion")?.toIntOrNull() == FORMAT_VERSION) {
            "Versi format backup tidak didukung oleh aplikasi ini."
        }
        require(properties.getProperty("databaseVersion")?.toIntOrNull() != null) {
            "Informasi versi database pada backup tidak valid."
        }
        require(properties.getProperty("createdAt")?.toLongOrNull() != null) {
            "Tanggal pembuatan backup tidak valid."
        }
        val tableCount = properties.getProperty("tableCount")?.toIntOrNull()
        require(tableCount != null && tableCount in 1..MAX_TABLES) {
            "Jumlah tabel pada manifest backup tidak valid."
        }
    }

    private fun findEntry(zip: ZipInputStream, name: String): ZipEntry {
        while (true) {
            val entry = zip.nextEntry ?: error("Arsip backup tidak memiliki file $name.")
            if (entry.name == name) return entry
            zip.closeEntry()
        }
    }

    private fun applicationTables(database: SupportSQLiteDatabase): List<String> =
        database.query(
            "SELECT name FROM sqlite_master " +
                "WHERE type = 'table' AND name NOT LIKE 'sqlite_%' " +
                "AND name NOT IN ('room_master_table', 'android_metadata') ORDER BY name"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }

    private fun tableColumns(database: SupportSQLiteDatabase, table: String): List<String> =
        database.query("PRAGMA table_info(${quoteIdentifier(table)})").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            require(nameIndex >= 0) { "Tidak dapat membaca struktur tabel $table." }
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }

    private fun countRows(database: SupportSQLiteDatabase, table: String): Long =
        database.query("SELECT COUNT(*) FROM ${quoteIdentifier(table)}").use { cursor ->
            check(cursor.moveToFirst()) { "Tidak dapat menghitung isi tabel $table." }
            cursor.getLong(0)
        }

    private fun writeRow(output: DataOutputStream, cursor: Cursor) {
        repeat(cursor.columnCount) { index ->
            when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_NULL -> output.writeByte(0)
                Cursor.FIELD_TYPE_INTEGER -> {
                    output.writeByte(1)
                    output.writeLong(cursor.getLong(index))
                }
                Cursor.FIELD_TYPE_FLOAT -> {
                    output.writeByte(2)
                    output.writeDouble(cursor.getDouble(index))
                }
                Cursor.FIELD_TYPE_STRING -> {
                    output.writeByte(3)
                    writeString(output, cursor.getString(index))
                }
                Cursor.FIELD_TYPE_BLOB -> {
                    output.writeByte(4)
                    val bytes = cursor.getBlob(index)
                    require(bytes.size <= MAX_VALUE_BYTES) { "Nilai data terlalu besar untuk backup." }
                    output.writeInt(bytes.size)
                    output.write(bytes)
                }
                else -> error("Tipe data SQLite tidak dikenal.")
            }
        }
    }

    private fun bindValue(input: DataInputStream, statement: SupportSQLiteStatement, index: Int) {
        when (input.readUnsignedByte()) {
            0 -> statement.bindNull(index)
            1 -> statement.bindLong(index, input.readLong())
            2 -> statement.bindDouble(index, input.readDouble())
            3 -> statement.bindString(index, readString(input))
            4 -> {
                val length = input.readInt()
                require(length in 0..MAX_VALUE_BYTES) { "Ukuran lampiran/objek pada backup tidak valid." }
                val bytes = ByteArray(length)
                input.readFully(bytes)
                statement.bindBlob(index, bytes)
            }
            else -> error("Tipe nilai pada backup tidak dikenal.")
        }
    }

    private fun writeString(output: DataOutputStream, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_VALUE_BYTES) { "Teks pada backup melebihi batas ukuran." }
        output.writeInt(bytes.size)
        output.write(bytes)
    }

    private fun readString(input: DataInputStream): String {
        val length = input.readInt()
        require(length in 0..MAX_VALUE_BYTES) { "Ukuran teks pada backup tidak valid." }
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    private fun quoteIdentifier(identifier: String): String =
        "\"" + identifier.replace("\"", "\"\"") + "\""
}
