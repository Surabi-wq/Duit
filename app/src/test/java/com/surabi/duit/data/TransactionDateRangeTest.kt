package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class TransactionDateRangeTest {
    @Test
    fun includesTheWholeEndDate() {
        val bounds = TransactionDateRange.parse("15-03-2026", "15-03-2026")
        assertNotNull(bounds)
        val range = requireNotNull(bounds)

        val start = Calendar.getInstance().apply { timeInMillis = range.startAt }
        assertEquals(15, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, start.get(Calendar.MINUTE))
        assertEquals(0, start.get(Calendar.SECOND))
        assertEquals(0, start.get(Calendar.MILLISECOND))

        val end = Calendar.getInstance().apply { timeInMillis = range.endAt }
        assertEquals(15, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, end.get(Calendar.MINUTE))
        assertEquals(59, end.get(Calendar.SECOND))
        assertEquals(999, end.get(Calendar.MILLISECOND))
    }

    @Test
    fun acceptsLeapDayAndRejectsInvalidCalendarDate() {
        assertNotNull(TransactionDateRange.parse("29-02-2024", "29-02-2024"))
        assertNull(TransactionDateRange.parse("29-02-2025", "29-02-2025"))
        assertNull(TransactionDateRange.parse("31-04-2026", "31-04-2026"))
    }

    @Test
    fun rejectsPartialDateAndStartAfterEnd() {
        assertNull(TransactionDateRange.parse("15-03-2026abc", "16-03-2026"))
        assertNull(TransactionDateRange.parse("16-03-2026", "15-03-2026"))
    }

    @Test
    fun emptyBoundUsesUnboundedTimestamp() {
        val bounds = requireNotNull(TransactionDateRange.parse("", ""))
        assertEquals(Long.MIN_VALUE, bounds.startAt)
        assertEquals(Long.MAX_VALUE, bounds.endAt)
    }
}
