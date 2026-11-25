package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionCsvExporterTest {
    @Test
    fun writesUtf8BomHeaderAndTransactionValues() {
        val transaction = TransactionEntity(
            id = 12,
            workspaceId = 1,
            type = "EXPENSE",
            title = "Belanja, mingguan",
            amountMinor = 125000,
            fromAccountId = 3,
            categoryId = 7,
            note = "Nota \"asli\"",
            occurredAt = 0,
            createdAt = 0
        )

        val csv = TransactionCsvExporter.toCsv(
            listOf(transaction),
            accountNames = mapOf(3L to "Dompet Utama"),
            categoryNames = mapOf(7L to "Belanja")
        )

        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("\"ID\""))
        assertTrue(csv.contains("\"Belanja, mingguan\""))
        assertTrue(csv.contains("\"Dompet Utama\""))
        assertTrue(csv.contains("\"Nota \"\"asli\"\"\""))
        assertTrue(csv.contains("\"125000\""))
    }

    @Test
    fun prefixesFormulaLikeTextToPreventSpreadsheetFormulaInjection() {
        val transaction = TransactionEntity(
            id = 1,
            workspaceId = 1,
            type = "EXPENSE",
            title = "=HYPERLINK(\"https://example.invalid\")",
            amountMinor = 1,
            fromAccountId = 1,
            note = "@SUM(1,1)",
            occurredAt = 0,
            createdAt = 0
        )

        val csv = TransactionCsvExporter.toCsv(listOf(transaction), mapOf(1L to "+Wallet"), emptyMap())

        assertTrue(csv.contains("\"'=HYPERLINK(\"\"https://example.invalid\"\")\""))
        assertTrue(csv.contains("\"'@SUM(1,1)\""))
        assertTrue(csv.contains("\"'+Wallet\""))
    }
    @Test
    fun streamsMultipleBatchesWithOnlyOneHeader() {
        val first = TransactionEntity(
            id = 1,
            workspaceId = 1,
            type = "EXPENSE",
            title = "Belanja pertama",
            amountMinor = 10000,
            fromAccountId = 1,
            occurredAt = 1,
            createdAt = 1
        )
        val second = first.copy(id = 2, title = "Belanja kedua", amountMinor = 20000)

        val output = StringBuilder()
        TransactionCsvExporter.writeHeader(output)
        TransactionCsvExporter.writeRows(output, listOf(first), mapOf(1L to "Dompet"), emptyMap())
        TransactionCsvExporter.writeRows(output, listOf(second), mapOf(1L to "Dompet"), emptyMap())

        val csv = output.toString()
        assertTrue(csv.startsWith("\uFEFF"))
        assertEquals(1, csv.split("\"ID\"").size - 1)
        assertTrue(csv.contains("\"1\""))
        assertTrue(csv.contains("\"Belanja pertama\""))
        assertTrue(csv.contains("\"Belanja kedua\""))
    }

}
