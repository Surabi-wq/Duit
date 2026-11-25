package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MinimumBalanceParserTest {
    @Test
    fun parsesWholeRupiahAndAllowsZeroToDisableWarning() {
        assertEquals(0L, MinimumBalanceParser.parse("0"))
        assertEquals(125000L, MinimumBalanceParser.parse("125000"))
        assertEquals(900000L, MinimumBalanceParser.parse(" 900000 "))
    }

    @Test
    fun rejectsBlankNegativeDecimalAndFormattedInput() {
        assertNull(MinimumBalanceParser.parse(""))
        assertNull(MinimumBalanceParser.parse("   "))
        assertNull(MinimumBalanceParser.parse("-1"))
        assertNull(MinimumBalanceParser.parse("1.000"))
        assertNull(MinimumBalanceParser.parse("1,000"))
        assertNull(MinimumBalanceParser.parse("abc"))
    }

    @Test
    fun rejectsValuesOutsideLongRange() {
        assertNull(MinimumBalanceParser.parse("9223372036854775808"))
    }

    @Test
    fun acceptsMaximumLongValue() {
        assertEquals(Long.MAX_VALUE, MinimumBalanceParser.parse(Long.MAX_VALUE.toString()))
    }
}
