package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DashboardPeriodResolverTest {
    @Test
    fun thisMonthStartsAtFirstDayAndEndsAtEndOfToday() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 26, 10, 15, 30)
            set(Calendar.MILLISECOND, 123)
        }.timeInMillis
        val range = DashboardPeriodResolver.bounds(DashboardPeriod.THIS_MONTH, now)
        val start = Calendar.getInstance().apply { timeInMillis = range.startAt }
        val end = Calendar.getInstance().apply { timeInMillis = range.endAt }
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(26, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(999, end.get(Calendar.MILLISECOND))
    }

    @Test
    fun lastThirtyDaysIncludesTodayAndTwentyNinePreviousCalendarDays() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 1, 11, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val range = DashboardPeriodResolver.bounds(DashboardPeriod.LAST_30_DAYS, now)
        val start = Calendar.getInstance().apply { timeInMillis = range.startAt }
        assertEquals(Calendar.JANUARY, start.get(Calendar.MONTH))
        assertEquals(31, start.get(Calendar.DAY_OF_MONTH))
        assertTrue(range.startAt < range.endAt)
    }

    @Test
    fun thisYearStartsOnJanuaryFirst() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 20, 9, 0, 0)
        }.timeInMillis
        val range = DashboardPeriodResolver.bounds(DashboardPeriod.THIS_YEAR, now)
        val start = Calendar.getInstance().apply { timeInMillis = range.startAt }
        assertEquals(Calendar.JANUARY, start.get(Calendar.MONTH))
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(2026, start.get(Calendar.YEAR))
    }
}
