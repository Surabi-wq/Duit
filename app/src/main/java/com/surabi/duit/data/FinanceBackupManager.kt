package com.surabi.duit.data
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Offline ZIP backup with SHA-256 manifest and staged, integrity-checked restore. */
object FinanceBackupManager {
    private const val DB = "duit-local.db"
    private const val MANIFEST = "manifest.properties"
    private const val MAX_BYTES = 536870912L
    private const val MAX_MANIFEST_BYTES = 16 * 1024

    fun createBackup(context: Context, database: FinanceDatabase, destination: java.io.OutputStream) {
        checkpoint(database)
        val file = context.getDatabasePath(DB)
        check(file.isFile && file.length() > 0) { "Database lokal tidak ditemukan." }
        val text = "formatVersion=1\ndatabase=$DB\nsize=${file.length()}\nsha256=${digest(file)}\n"
        ZipOutputStream(destination.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(DB)); FileInputStream(file).use { it.copyTo(zip, 65536) }; zip.closeEntry()
            zip.putNextEntry(ZipEntry(MANIFEST)); zip.write(text.toByteArray(Charsets.UTF_8)); zip.closeEntry()
        }
    }

    fun restoreBackup(context: Context, database: FinanceDatabase, source: InputStream) {
        val stage = File(context.cacheDir, "duit-restore-${System.nanoTime()}.db")
        val safety = File(context.noBackupFilesDir, "duit-safety-${System.nanoTime()}.db")
        val target = context.getDatabasePath(DB)
        var safetyPrepared = false
        var restoreCommitted = false
        var rollbackCompleted = false
        try {
            var manifest: String? = null
            var found = false
            ZipInputStream(BufferedInputStream(source)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    when (entry.name) {
                        DB -> {
                            check(!found) { "Database duplikat pada arsip." }
                            FileOutputStream(stage).use { out ->
                                val buffer = ByteArray(65536)
                                var total = 0L
                                while (true) {
                                    val n = zip.read(buffer)
                                    if (n < 0) break
                                    total += n
                                    check(total <= MAX_BYTES) { "Ukuran backup melebihi batas." }
                                    out.write(buffer, 0, n)
                                }
                            }
                            found = true
                        }
                        MANIFEST -> {
                            check(manifest == null) { "Manifest duplikat." }
                            val bytes = ByteArrayOutputStream()
                            val buffer = ByteArray(1024)
                            var total = 0
                            while (true) {
                                val n = zip.read(buffer)
                                if (n < 0) break
                                total += n
                                check(total <= MAX_MANIFEST_BYTES) { "Manifest backup terlalu besar." }
                                bytes.write(buffer, 0, n)
                            }
                            manifest = bytes.toByteArray().toString(Charsets.UTF_8)
                        }
                        else -> error("Entri arsip tidak dikenal: ${entry.name}")
                    }
                    zip.closeEntry()
                }
            }
            check(found && manifest != null) { "File bukan backup Duit yang lengkap." }
            val props = parseManifest(manifest!!)
            check(stage.length() == props["size"]?.toLongOrNull()) { "Ukuran database tidak cocok." }
            check(digest(stage).equals(props["sha256"], true)) { "Checksum SHA-256 tidak cocok." }
            verify(stage)

            checkpoint(database)
            check(target.isFile && target.length() > 0L) { "Database aktif tidak ditemukan." }
            target.copyTo(safety, true)
            check(safety.isFile && safety.length() == target.length()) {
                "Salinan pengaman database tidak berhasil dibuat."
            }
            safetyPrepared = true

            try {
                FinanceDatabase.closeForBackupRestore()
                deleteSidecarFiles(target)
                stage.copyTo(target, true)
                verify(target)
                verifyOpenedDatabase(context)
                restoreCommitted = true
            } catch (failure: Throwable) {
                runCatching { FinanceDatabase.closeForBackupRestore() }
                deleteSidecarFiles(target)
                try {
                    safety.copyTo(target, true)
                    verify(target)
                    verifyOpenedDatabase(context)
                    rollbackCompleted = true
                } catch (rollbackFailure: Throwable) {
                    rollbackFailure.addSuppressed(failure)
                    throw IllegalStateException(
                        "Restore gagal dan pemulihan otomatis belum berhasil. Salinan pengaman masih disimpan di ${safety.absolutePath}. Jangan hapus file tersebut.",
                        rollbackFailure
                    )
                }
                throw IllegalStateException(
                    "Restore gagal; database sebelumnya berhasil dikembalikan.",
                    failure
                )
            }
        } finally {
            stage.delete()
            if (!safetyPrepared || restoreCommitted || rollbackCompleted) safety.delete()
        }
    }

    private fun parseManifest(text: String): Map<String, String> {
        val values = linkedMapOf<String, String>()
        val allowedKeys = setOf("formatVersion", "database", "size", "sha256")
        text.lineSequence().filter { it.isNotBlank() }.forEach { line ->
            val separator = line.indexOf('=')
            check(separator > 0) { "Manifest backup tidak valid." }
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim()
            check(key in allowedKeys && key !in values) { "Kunci manifest tidak dikenal atau duplikat." }
            values[key] = value
        }
        check(values.keys == allowedKeys) { "Manifest backup tidak lengkap." }
        check(values["formatVersion"] == "1" && values["database"] == DB) {
            "Versi/manifest backup tidak didukung."
        }
        check(values["size"]?.toLongOrNull()?.let { it in 1..MAX_BYTES } == true) {
            "Ukuran database pada manifest tidak valid."
        }
        check(values["sha256"]?.matches(Regex("(?i)[0-9a-f]{64}")) == true) {
            "Checksum pada manifest tidak valid."
        }
        return values
    }

    private fun checkpoint(database: FinanceDatabase) {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
            check(!cursor.moveToFirst() || cursor.getInt(0) == 0) {
                "Database sedang sibuk; tutup proses lain lalu ulangi."
            }
        }
    }

    private fun deleteSidecarFiles(databaseFile: File) {
        File(databaseFile.path + "-wal").delete()
        File(databaseFile.path + "-shm").delete()
    }

    private fun verifyOpenedDatabase(context: Context) {
        val reopened = FinanceDatabase.getInstance(context)
        reopened.openHelper.writableDatabase.query("PRAGMA integrity_check").use { cursor ->
            check(cursor.moveToFirst() && cursor.getString(0) == "ok") {
                "Integritas database setelah restore gagal."
            }
        }
    }

    private fun verify(file: File) {
        check(file.isFile && file.length() in 1..MAX_BYTES) { "Ukuran database tidak valid." }
        val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            db.rawQuery("PRAGMA integrity_check", null).use { c ->
                check(c.moveToFirst() && c.getString(0) == "ok") { "Database backup rusak." }
            }
            db.rawQuery("PRAGMA foreign_key_check", null).use { c ->
                check(!c.moveToFirst()) { "Database backup memiliki relasi yang tidak valid." }
            }
            db.rawQuery("PRAGMA user_version", null).use { c ->
                check(c.moveToFirst() && c.getInt(0) in 1..5) { "Versi skema backup tidak didukung." }
            }
        } finally { db.close() }
    }

    private fun digest(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(65536)
            while (true) { val n = input.read(buffer); if (n < 0) break; md.update(buffer, 0, n) }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
