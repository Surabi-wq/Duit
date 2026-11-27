package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AccountSettingsPolicyTest {
    @Test
    fun supportsAllExistingAccountTypes() {
        assertTrue(AccountSettingsPolicy.supportedTypes.containsAll(
            setOf("CASH", "BANK", "SAVINGS", "EWALLET", "CREDIT_CARD", "INVESTMENT", "OTHER")
        ))
    }

    @Test
    fun trimsAccountNameAndRejectsBlankOrLongValues() {
        assertEquals("Rekening Utama", AccountSettingsPolicy.normalizeName("  Rekening Utama  "))
        assertThrows(IllegalArgumentException::class.java) { AccountSettingsPolicy.normalizeName("   ") }
        assertThrows(IllegalArgumentException::class.java) { AccountSettingsPolicy.normalizeName("A".repeat(61)) }
    }

    @Test
    fun rejectsUnsupportedAccountTypeAndNegativeMinimum() {
        assertThrows(IllegalArgumentException::class.java) { AccountSettingsPolicy.requireSupportedType("CRYPTO") }
        assertThrows(IllegalArgumentException::class.java) { AccountSettingsPolicy.requireValidMinimumBalance(-1L) }
    }

    @Test
    fun acceptsDistinctTransferAccounts() {
        AccountTransferPolicy.validateSelection(10L, 20L)
    }

    @Test
    fun rejectsMissingInvalidOrIdenticalTransferAccounts() {
        assertThrows(IllegalArgumentException::class.java) { AccountTransferPolicy.validateSelection(10L, null) }
        assertThrows(IllegalArgumentException::class.java) { AccountTransferPolicy.validateSelection(10L, 0L) }
        assertThrows(IllegalArgumentException::class.java) { AccountTransferPolicy.validateSelection(10L, 10L) }
    }
}
