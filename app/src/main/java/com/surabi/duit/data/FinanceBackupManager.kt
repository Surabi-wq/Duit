package com.surabi.duit.data
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.BufferedInputStream
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

    fun createBackup(context: Context, database: FinanceDatabase, destination: java.io.OutputStream) {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { c ->
            check(!c.moveToFirst() || c.getInt(0) == 0) { "Database sedang sibuk; ulangi backup." }
        }
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
        val safety = File(context.cacheDir, "duit-safety-${System.nanoTime()}.db")
        val target = context.getDatabasePath(DB)
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
                                val buffer = ByteArray(65536); var total = 0L
                                while (true) { val n = zip.read(buffer); if (n < 0) break; total += n
                                    check(total <= MAX_BYTES) { "Ukuran backup melebihi batas." }; out.write(buffer, 0, n) }
                            }; found = true
                        }
                        MANIFEST -> { check(manifest == null) { "Manifest duplikat." }; manifest = zip.readBytes().toString(Charsets.UTF_8) }
                        else -> error("Entri arsip tidak dikenal: ${entry.name}")
                    }
                    zip.closeEntry()
                }
            }
            check(found && manifest != null) { "File bukan backup Duit yang lengkap." }
            val props = manifest!!.lineSequence().filter { it.contains("=") }.associate { it.substringBefore("=") to it.substringAfter("=") }
            check(props["formatVersion"] == "1" && props["database"] == DB) { "Versi/manifest backup tidak didukung." }
            check(stage.length() == props["size"]?.toLongOrNull()) { "Ukuran database tidak cocok." }
            check(digest(stage).equals(props["sha256"], true)) { "Checksum SHA-256 tidak cocok." }
            verify(stage)
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            check(target.isFile) { "Database aktif tidak ditemukan." }
            target.copyTo(safety, true)
            FinanceDatabase.closeForBackupRestore()
            File(target.path + "-wal").delete(); File(target.path + "-shm").delete()
            try {
                stage.copyTo(target, true); verify(target)
                FinanceDatabase.getInstance(context).openHelper.writableDatabase.query("PRAGMA integrity_check").use { c ->
                    check(c.moveToFirst() && c.getString(0) == "ok") { "Integritas database setelah restore gagal." }
                }
            } catch (failure: Throwable) {
                FinanceDatabase.closeForBackupRestore(); safety.copyTo(target, true); FinanceDatabase.getInstance(context)
                throw IllegalStateException("Restore gagal; database sebelumnya dikembalikan.", failure)
            }
            safety.delete()
        } finally { stage.delete(); safety.delete() }
    }

    private fun verify(file: File) {
        check(file.isFile && file.length() in 1..MAX_BYTES) { "Ukuran database tidak valid." }
        val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            db.rawQuery("PRAGMA integrity_check", null).use { c ->
                check(c.moveToFirst() && c.getString(0) == "ok") { "Database backup rusak." }
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
