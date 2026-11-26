package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Test

class NetWorthCalculatorTest {
    @Test
    fun goal_funds_are_added_back_and_liabilities_are_subtracted() {
        val result = NetWorthCalculator.calculate(
            accountBalances = listOf(1_000_000L, 500_000L),
            allocatedGoalFunds = listOf(250_000L),
            nonCashAssets = listOf(2_000_000L),
            receivables = listOf(300_000L),
            liabilities = listOf(700_000L)
        )
        assertEquals(3_350_000L, result)
    }

    @Test
    fun empty_inputs_are_zero_and_liabilities_reduce_net_worth() {
        assertEquals(
            125L,
            NetWorthCalculator.calculate(
                accountBalances = listOf(200L),
                allocatedGoalFunds = emptyList(),
                nonCashAssets = emptyList(),
                receivables = emptyList(),
                liabilities = listOf(75L)
            )
        )
        assertEquals(
            0L,
            NetWorthCalculator.calculate(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        )
    }
}
