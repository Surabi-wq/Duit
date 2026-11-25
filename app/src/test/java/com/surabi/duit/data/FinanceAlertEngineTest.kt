package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceAlertEngineTest {
    @Test
    fun warnsWhenAccountFallsBelowMinimumAndMarksNegativeBalanceCritical() {
        val accounts = listOf(
            account(id = 1, name = "Tunai", balance = 50_000, minimum = 100_000),
            account(id = 2, name = "Bank", balance = -5_000, minimum = 0)
        )
        val alerts = FinanceAlertEngine.evaluate(accounts, emptyList(), emptyList(), emptyList(), 0, 0, 10_000)
        assertEquals(2, alerts.size)
        assertEquals(FinanceAlertSeverity.CRITICAL, alerts.first().severity)
        assertTrue(alerts.any { it.title == "Saldo di bawah batas minimum" })
    }

    @Test
    fun warnsAtEightyPercentAndMarksOverspentBudgetCritical() {
        val budgets = listOf(
            budget(id = 1, name = "Makan", limit = 1_000_000, spent = 800_000),
            budget(id = 2, name = "Transport", limit = 500_000, spent = 550_000)
        )
        val alerts = FinanceAlertEngine.evaluate(emptyList(), budgets, emptyList(), emptyList(), 0, 0, 10_000)
        assertEquals(2, alerts.size)
        assertEquals(FinanceAlertSeverity.CRITICAL, alerts.first().severity)
        assertTrue(alerts.any { it.title == "Anggaran hampir habis" })
    }

    @Test
    fun warnsForNegativeCashflowAndOverdueDebtAndReceivable() {
        val debt = LiabilityOverviewRow(1, 1, "Pinjaman", "Bank", 100_000, 50_000, 1_000, false)
        val bill = ReceivableOverviewRow(2, 1, "Invoice", "Pelanggan", 80_000, 30_000, 1_000)
        val alerts = FinanceAlertEngine.evaluate(emptyList(), emptyList(), listOf(debt), listOf(bill), 10, 20, 10_000)
        assertEquals(3, alerts.size)
        assertEquals(2, alerts.count { it.severity == FinanceAlertSeverity.CRITICAL })
        assertTrue(alerts.any { it.title == "Arus kas bulan ini negatif" })
    }

    @Test
    fun ignoresSettledAndNotYetDueObligations() {
        val debt = LiabilityOverviewRow(1, 1, "Lunas", "Bank", 100_000, 0, 1_000, true)
        val bill = ReceivableOverviewRow(2, 1, "Belum jatuh tempo", "Pelanggan", 80_000, 30_000, 20_000)
        val alerts = FinanceAlertEngine.evaluate(emptyList(), emptyList(), listOf(debt), listOf(bill), 100, 20, 10_000)
        assertTrue(alerts.isEmpty())
    }

    private fun account(id: Long, name: String, balance: Long, minimum: Long) =
        AccountBalanceRow(id, 1, name, "CASH", 0, balance, minimum)

    private fun budget(id: Long, name: String, limit: Long, spent: Long) =
        BudgetOverviewRow(id, 1, name, limit, null, 0, 10_000, spent)
}
