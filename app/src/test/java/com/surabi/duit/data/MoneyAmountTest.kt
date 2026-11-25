package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyAmountTest {
    @Test
    fun parsesWholeRupiahWithoutPrecisionLoss() {
        assertEquals(1250000L, MoneyAmount.parseWholeRupiah("1250000"))
        assertEquals(0L, MoneyAmount.parseWholeRupiah("0"))
    }

    @Test
    fun rejectsBlankFormattedAndNonNumericInput() {
        assertNull(MoneyAmount.parseWholeRupiah(""))
        assertNull(MoneyAmount.parseWholeRupiah("   "))
        assertNull(MoneyAmount.parseWholeRupiah("1.250.000"))
        assertNull(MoneyAmount.parseWholeRupiah("-250"))
        assertNull(MoneyAmount.parseWholeRupiah("1250,50"))
        assertNull(MoneyAmount.parseWholeRupiah("abc"))
    }

    @Test
    fun rejectsOverflowInsteadOfWrapping() {
        assertNull(MoneyAmount.parseWholeRupiah("999999999999999999999999999"))
    }

    @Test
    fun requirePositiveRejectsZero() {
        assertThrows(IllegalArgumentException::class.java) {
            MoneyAmount.requirePositiveRupiah("0")
        }
    }

    @Test
    fun requirePositiveReturnsValidAmount() {
        assertEquals(45000L, MoneyAmount.requirePositiveRupiah("45000"))
    }
}
