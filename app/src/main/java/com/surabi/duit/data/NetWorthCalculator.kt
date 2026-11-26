package com.surabi.duit.data

/**
 * One shared definition of tracked net worth for the dashboard and wealth page.
 * Goal funds are added back because contributions have already reduced account balances.
 */
object NetWorthCalculator {
    fun calculate(
        accountBalances: Iterable<Long>,
        allocatedGoalFunds: Iterable<Long>,
        nonCashAssets: Iterable<Long>,
        receivables: Iterable<Long>,
        liabilities: Iterable<Long>
    ): Long = accountBalances.sum() +
        allocatedGoalFunds.sum() +
        nonCashAssets.sum() +
        receivables.sum() -
        liabilities.sum()
}
