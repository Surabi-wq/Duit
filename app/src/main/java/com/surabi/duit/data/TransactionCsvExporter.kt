package com.surabi.duit.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds a spreadsheet-safe CSV snapshot for the transaction rows currently
 * shown to the user. Text cells are protected against formula injection.
 */
object TransactionCsvExporter {
    private val dateFormatter = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }

    fun toCsv(
        transactions: List<TransactionEntity>,
        accountNames: Map<Long, String>,
        categoryNames: Map<Long, String>
    ): String = buildString {
        append('\uFEFF')
        appendLine(
            listOf(
                "ID",
                "Tanggal dan waktu",
                "Jenis",
                "Keterangan",
                "Akun sumber",
                "Akun tujuan",
                "Kategori",
                "Nominal (IDR)",
                "Catatan"
            ).joinToString(",") { csvCell(it) }
        )

        transactions.forEach { transaction ->
            val values = listOf(
                transaction.id.toString(),
                dateFormatter.get().format(Date(transaction.occurredAt)),
                transactionTypeLabel(transaction.type),
                transaction.title,
                accountNames[transaction.fromAccountId].orEmpty(),
                transaction.toAccountId?.let { accountNames[it] }.orEmpty(),
                transaction.categoryId?.let { categoryNames[it] }.orEmpty(),
                transaction.amountMinor.toString(),
                transaction.note
            )
            appendLine(values.joinToString(",") { csvCell(it) })
        }
    }

    private fun csvCell(value: String): String {
        val normalized = value.replace("\r\n", "\n").replace('\r', '\n')
        val first = normalized.firstOrNull()
        val protected = if (first == '=' || first == '+' || first == '-' ||
            first == '@' || first == '\t' || first == '\n'
        ) {
            "'$normalized"
        } else {
            normalized
        }
        return "\"\u0024{protected.replace("\"", "\"\"")}\""
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
