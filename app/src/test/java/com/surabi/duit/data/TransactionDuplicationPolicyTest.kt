package com.surabi.duit.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDuplicationPolicyTest {
    @Test
    fun supportsOrdinaryIncomeExpenseAndTransfers() {
        assertTrue(TransactionDuplicationPolicy.canDuplicate("INCOME"))
        assertTrue(TransactionDuplicationPolicy.canDuplicate("EXPENSE"))
        assertTrue(TransactionDuplicationPolicy.canDuplicate("TRANSFER"))
    }

    @Test
    fun rejectsSpecialLedgerMovementsAndUnknownTypes() {
        assertFalse(TransactionDuplicationPolicy.canDuplicate("GOAL_CONTRIBUTION"))
        assertFalse(TransactionDuplicationPolicy.canDuplicate("GOAL_WITHDRAWAL"))
        assertFalse(TransactionDuplicationPolicy.canDuplicate("LIABILITY_PAYMENT"))
        assertFalse(TransactionDuplicationPolicy.canDuplicate("RECEIVABLE_COLLECTION"))
        assertFalse(TransactionDuplicationPolicy.canDuplicate("UNKNOWN"))
        assertFalse(TransactionDuplicationPolicy.canDuplicate("income"))
    }
}
