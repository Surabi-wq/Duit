package com.surabi.duit.data

import java.util.Calendar

data class FinanceAlert(
    val title: String,
    val message: String,
    val severity: FinanceAlertSeverity
)

enum class FinanceAlertSeverity { CRITICAL, WARNING }

/**
 * Deterministic, offline-only dashboard rules. This class is read-only:
 * it never mutates balances, budgets, transactions, or due dates.
 */
object FinanceAlertEngine {
    private fun isOverdue(dueAt: Long?, now: Long): Boolean {
        if (dueAt == null) return false
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // A due date is considered late only after that calendar day has ended.
        return dueAt < calendar.timeInMillis
    }

    fun evaluate(
        accounts: List<AccountBalanceRow>,
        budgets: List<BudgetOverviewRow>,
        liabilities: List<LiabilityOverviewRow>,
        receivables: List<ReceivableOverviewRow>,
        income: Long,
        expenses: Long,
        now: Long
    ): List<FinanceAlert> {
        val alerts = mutableListOf<FinanceAlert>()

        accounts.forEach { account ->
            when {
                account.balanceMinor < 0L -> alerts += FinanceAlert(
                    "Saldo akun negatif",
                    "${account.name}: saldo ${account.balanceMinor} rupiah. Periksa transaksi dan saldo awal.",
                    FinanceAlertSeverity.CRITICAL
                )
                account.minimumBalanceMinor > 0L && account.balanceMinor < account.minimumBalanceMinor ->
                    alerts += FinanceAlert(
                        "Saldo di bawah batas minimum",
                        "${account.name}: saldo saat ini ${account.balanceMinor} rupiah, batas minimum ${account.minimumBalanceMinor} rupiah.",
                        FinanceAlertSeverity.WARNING
                    )
            }
        }

        budgets.filter { it.limitMinor > 0L }.forEach { budget ->
            val ratio = budget.spentMinor.toDouble() / budget.limitMinor.toDouble()
            when {
                ratio > 1.0 -> alerts += FinanceAlert(
                    "Anggaran terlampaui",
                    "${budget.name}: realisasi ${budget.spentMinor} rupiah dari batas ${budget.limitMinor} rupiah.",
                    FinanceAlertSeverity.CRITICAL
                )
                ratio >= 0.8 -> alerts += FinanceAlert(
                    "Anggaran hampir habis",
                    "${budget.name}: ${(ratio * 100).toInt()}% anggaran telah digunakan.",
                    FinanceAlertSeverity.WARNING
                )
            }
        }

        if (expenses > income) {
            alerts += FinanceAlert(
                "Arus kas bulan ini negatif",
                "Pengeluaran melebihi pemasukan sebesar ${expenses - income} rupiah.",
                FinanceAlertSeverity.WARNING
            )
        }

        liabilities.filter { it.outstandingMinor > 0L && it.dueAt != null && isOverdue(it.dueAt, now) }
            .forEach { debt ->
                alerts += FinanceAlert(
                    "Utang melewati jatuh tempo",
                    "${debt.name}: sisa kewajiban ${debt.outstandingMinor} rupiah.",
                    FinanceAlertSeverity.CRITICAL
                )
            }

        receivables.filter { it.outstandingMinor > 0L && it.dueAt != null && it.dueAt < now }
            .forEach { bill ->
                alerts += FinanceAlert(
                    "Piutang melewati jatuh tempo",
                    "${bill.name}: sisa tagihan ${bill.outstandingMinor} rupiah dari ${bill.counterparty}.",
                    FinanceAlertSeverity.WARNING
                )
            }

        return alerts.sortedBy { if (it.severity == FinanceAlertSeverity.CRITICAL) 0 else 1 }
    }
}
