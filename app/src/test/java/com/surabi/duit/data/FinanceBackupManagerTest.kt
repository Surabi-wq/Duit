package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FinanceBackupManagerTest {
    @Test
    fun inspectBackupReadsVersionAndTotalRowCount() {
        val manifest = Properties().apply {
            setProperty("format", "DUIT_FULL_BACKUP")
            setProperty("formatVersion", "1")
            setProperty("appVersion", "1.5.0")
            setProperty("databaseVersion", "5")
            setProperty("createdAt", "1760000000000")
            setProperty("tableCount", "3")
            setProperty("rows.accounts", "4")
            setProperty("rows.transactions", "20")
            setProperty("rows.workspaces", "2")
        }
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.properties"))
            manifest.store(zip, "test")
            zip.closeEntry()
        }

        val result = FinanceBackupManager.inspectBackup(ByteArrayInputStream(bytes.toByteArray()))
        assertEquals("1.5.0", result.appVersion)
        assertEquals(5, result.databaseVersion)
        assertEquals(3, result.tableCount)
        assertEquals(26L, result.totalRows)
    }

    @Test
    fun inspectBackupRejectsNonDuitArchive() {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("not-a-backup.txt"))
            zip.write("not a backup".toByteArray())
            zip.closeEntry()
        }

        assertThrows(IllegalStateException::class.java) {
            FinanceBackupManager.inspectBackup(ByteArrayInputStream(bytes.toByteArray()))
        }
    }
}
