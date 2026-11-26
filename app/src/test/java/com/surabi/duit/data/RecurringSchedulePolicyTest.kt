package com.surabi.duit.data

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RecurringSchedulePolicyTest {
    private fun date(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply { clear(); set(year, month - 1, day, 9, 0, 0) }.timeInMillis

    private fun parts(timestamp: Long): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        return Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test fun monthlyEndOfMonthDoesNotDrift() {
        val january = date(2026, 1, 31)
        val february = RecurringSchedulePolicy.nextDueAt(january, "MONTHLY", 1, 31, true)
        val march = RecurringSchedulePolicy.nextDueAt(february, "MONTHLY", 1, 31, true)
        assertEquals(Triple(2026, 2, 28), parts(february))
        assertEquals(Triple(2026, 3, 31), parts(march))
    }

    @Test fun monthlyDayAnchorIsRestoredAfterShortMonth() {
        val january = date(2026, 1, 30)
        val february = RecurringSchedulePolicy.nextDueAt(january, "MONTHLY", 1, 30, false)
        val march = RecurringSchedulePolicy.nextDueAt(february, "MONTHLY", 1, 30, false)
        assertEquals(Triple(2026, 2, 28), parts(february))
        assertEquals(Triple(2026, 3, 30), parts(march))
    }

    @Test fun yearlyLeapDayEventuallyReturnsToLeapDay() {
        var current = date(2024, 2, 29)
        current = RecurringSchedulePolicy.nextDueAt(current, "YEARLY", 1, 29, false)
        assertEquals(Triple(2025, 2, 28), parts(current))
        current = RecurringSchedulePolicy.nextDueAt(current, "YEARLY", 1, 29, false)
        current = RecurringSchedulePolicy.nextDueAt(current, "YEARLY", 1, 29, false)
        current = RecurringSchedulePolicy.nextDueAt(current, "YEARLY", 1, 29, false)
        assertEquals(Triple(2028, 2, 29), parts(current))
    }

    @Test fun weeklyIntervalAdvancesByExactNumberOfWeeks() {
        val next = RecurringSchedulePolicy.nextDueAt(date(2026, 1, 5), "WEEKLY", 2, 5, false)
        assertEquals(Triple(2026, 1, 19), parts(next))
    }

    @Test fun dateParserRejectsImpossibleCalendarDates() {
        assertNull(RecurringSchedulePolicy.parseDate("31-02-2026"))
        assertNull(RecurringSchedulePolicy.parseDate("01-13-2026"))
        assertNull(RecurringSchedulePolicy.parseDate("2026-01-01"))
    }

    @Test fun unsupportedCadenceAndInvalidIntervalAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            RecurringSchedulePolicy.nextDueAt(date(2026, 1, 1), "HOURLY", 1, 1, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RecurringSchedulePolicy.nextDueAt(date(2026, 1, 1), "MONTHLY", 0, 1, false)
        }
    }
}
