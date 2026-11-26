package com.surabi.duit.data

/**
 * Shared net-worth formula used by the dashboard and wealth screens.
 *
 * Goal funds are included because goal contributions are deducted from account
 * balances; adding them back represents the user's allocated cash.
 */
object NetWorthCalculator {
    fun calculate(
        accountBalances: Iterable<Long>,
        allocatedGoalFunds: Iterable<Long>,
        nonCashAssets: Iterable<Long>,
        receivables: Iterable<Long>,
        liabilities: Iterable<Long>
    ): Long {
        return accountBalances.sum() +
            allocatedGoalFunds.sum() +
            nonCashAssets.sum() +
            receivables.sum() -
            liabilities.sum()
    }
}
