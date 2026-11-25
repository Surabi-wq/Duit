package com.surabi.duit.data

import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionCsvExporterTest {
    @Test
    fun appendableExportProducesSameOutputAsConvenienceExporter() {
        val transactions = listOf(
            TransactionEntity(
                id = 21, workspaceId = 2, type = "INCOME", title = "Gaji", amountMinor = 3500000,
                fromAccountId = 4, categoryId = 8, note = "Bulanan", occurredAt = 1000, createdAt = 1000
            ),
            TransactionEntity(
                id = 22, workspaceId = 2, type = "EXPENSE", title = "Makan", amountMinor = 45000,
                fromAccountId = 4, categoryId = 9, note = "Siang", occurredAt = 2000, createdAt = 2000
            )
        )
        val accounts = mapOf(4L to "Rekening utama")
        val categories = mapOf(8L to "Gaji", 9L to "Makanan")
        val expected = TransactionCsvExporter.toCsv(transactions, accounts, categories)
        val actual = StringBuilder().also { output ->
            TransactionCsvExporter.appendHeader(output)
            TransactionCsvExporter.appendRows(output, transactions.take(1), accounts, categories)
            TransactionCsvExporter.appendRows(output, transactions.drop(1), accounts, categories)
        }.toString()
        assertTrue(actual == expected)
    }

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
}
