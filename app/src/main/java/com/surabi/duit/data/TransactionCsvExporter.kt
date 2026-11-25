package com.surabi.duit.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes spreadsheet-safe CSV data. The streaming methods support bounded
 * database batches; text cells are protected against formula injection.
 */
object TransactionCsvExporter {
    fun toCsv(
        transactions: List<TransactionEntity>,
        accountNames: Map<Long, String>,
        categoryNames: Map<Long, String>
    ): String = buildString {
        appendHeader(this)
        appendRows(this, transactions, accountNames, categoryNames)
    }

    fun appendHeader(target: Appendable) {
        target.append('\uFEFF')
        target.append(
            listOf(
                "ID", "Tanggal dan waktu", "Jenis", "Keterangan", "Akun sumber",
                "Akun tujuan", "Kategori", "Nominal (IDR)", "Catatan"
            ).joinToString(",") { csvCell(it) }
        ).append('\n')
    }

    fun appendRows(
        target: Appendable,
        transactions: List<TransactionEntity>,
        accountNames: Map<Long, String>,
        categoryNames: Map<Long, String>
    ) {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        transactions.forEach { transaction ->
            val values = listOf(
                transaction.id.toString(),
                dateFormatter.format(Date(transaction.occurredAt)),
                transactionTypeLabel(transaction.type),
                transaction.title,
                accountNames[transaction.fromAccountId].orEmpty(),
                transaction.toAccountId?.let { accountNames[it] }.orEmpty(),
                transaction.categoryId?.let { categoryNames[it] }.orEmpty(),
                transaction.amountMinor.toString(),
                transaction.note
            )
            target.append(values.joinToString(",") { csvCell(it) }).append('\n')
        }
    }

    private fun csvCell(value: String): String {
        val normalized = value.replace("\r\n", "\n").replace('\r', '\n')
        val firstNonWhitespace = normalized.firstOrNull { !it.isWhitespace() }
        val protected = if (
            firstNonWhitespace == '=' || firstNonWhitespace == '+' ||
            firstNonWhitespace == '-' || firstNonWhitespace == '@'
        ) {
            "'$normalized"
        } else {
            normalized
        }
        return "\"" + protected.replace("\"", "\"\"") + "\""
    }

    private fun transactionTypeLabel(type: String): String = when (type) {
        "INCOME" -> "Pemasukan"
        "EXPENSE" -> "Pengeluaran"
        "TRANSFER" -> "Transfer"
        "GOAL_CONTRIBUTION" -> "Setoran target"
        "GOAL_WITHDRAWAL" -> "Penarikan target"
        "LIABILITY_PAYMENT" -> "Pembayaran utang"
        "LIABILITY_RECEIVED" -> "Pencairan pinjaman"
        "RECEIVABLE_COLLECTION" -> "Penerimaan piutang"
        else -> "Transaksi"
    }
}
