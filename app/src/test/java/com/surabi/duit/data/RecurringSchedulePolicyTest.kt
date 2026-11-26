package com.surabi.duit.data

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RecurringSchedulePolicyTest {
    private fun date(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, 9, 0, 0)
        }.timeInMillis

    private fun parts(timestamp: Long): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        return Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun monthlyEndOfMonthDoesNotDrift() {
        val january31 = date(2026, 1, 31)
        val february = RecurringSchedulePolicy.nextDueAt(january31, "MONTHLY", 1, 31, true)
        val march = RecurringSchedulePolicy.nextDueAt(february, "MONTHLY", 1, 31, true)
        assertEquals(Triple(2026, 2, 28), parts(february))
        assertEquals(Triple(2026, 3, 31), parts(march))
    }

    @Test
    fun monthlyDayAnchorIsRestoredAfterShortMonth() {
        val january30 = date(2026, 1, 30)
        val february = RecurringSchedulePolicy.nextDueAt(january30, "MONTHLY", 1, 30, false)
        val march = RecurringSchedulePolicy.nextDueAt(february, "MONTHLY", 1, 30, false)
        assertEquals(Triple(2026, 2, 28), parts(february))
        assertEquals(Triple(2026, 3, 30), parts(march))
    }

    @Test
    fun yearlyLeapDayClampsAndReturnsToLeapDay() {
        val leapDay = date(2024, 2, 29)
        val nextYear = RecurringSchedulePolicy.nextDueAt(leapDay, "YEARLY", 1, 29, false)
        val yearAfter = RecurringSchedulePolicy.nextDueAt(nextYear, "YEARLY", 1, 29, false)
        val thirdYear = RecurringSchedulePolicy.nextDueAt(yearAfter, "YEARLY", 1, 29, false)
        val nextLeapYear = RecurringSchedulePolicy.nextDueAt(thirdYear, "YEARLY", 1, 29, false)
        assertEquals(Triple(2025, 2, 28), parts(nextYear))
        assertEquals(Triple(2028, 2, 29), parts(nextLeapYear))
    }

    @Test
    fun weeklyIntervalAdvancesByExactNumberOfWeeks() {
        val monday = date(2026, 1, 5)
        val next = RecurringSchedulePolicy.nextDueAt(monday, "WEEKLY", 2, 5, false)
        assertEquals(Triple(2026, 1, 19), parts(next))
    }

    @Test
    fun dateParserRejectsImpossibleCalendarDates() {
        assertNull(RecurringSchedulePolicy.parseDate("31-02-2026"))
        assertNull(RecurringSchedulePolicy.parseDate("01-13-2026"))
        assertNull(RecurringSchedulePolicy.parseDate("2026-01-01"))
    }

    @Test
    fun recurrenceValidationRejectsUnsupportedValues() {
        assertThrows(IllegalArgumentException::class.java) {
            RecurringSchedulePolicy.nextDueAt(date(2026, 1, 1), "HOURLY", 1, 1, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RecurringSchedulePolicy.nextDueAt(date(2026, 1, 1), "MONTHLY", 0, 1, false)
        }
    }
}
