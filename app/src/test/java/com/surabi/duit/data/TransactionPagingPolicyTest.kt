package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TransactionPagingPolicyTest {
    @Test
    fun transactionPagingUsesSmallPagesAndBoundedMemory() {
        val config = TransactionPagingPolicy.config()
        assertEquals(40, config.pageSize)
        assertEquals(60, config.initialLoadSize)
        assertEquals(20, config.prefetchDistance)
        assertEquals(200, config.maxSize)
        assertFalse(config.enablePlaceholders)
    }
}
