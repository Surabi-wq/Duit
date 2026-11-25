package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DashboardTrendResolverTest {
    @Test
    fun boundsCoverSixLocalMonthsThroughEndOfToday() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 26, 10, 15, 30)
            set(Calendar.MILLISECOND, 123)
        }.timeInMillis
        val bounds = DashboardTrendResolver.bounds(now)
        val start = Calendar.getInstance().apply { timeInMillis = bounds.startAt }
        val end = Calendar.getInstance().apply { timeInMillis = bounds.endAt }
        assertEquals(Calendar.JUNE, start.get(Calendar.MONTH))
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(26, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(999, end.get(Calendar.MILLISECOND))
    }

    @Test
    fun missingMonthsAreZeroFilledAndRowsRemainChronological() {
        val now = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 26, 10, 0, 0) }.timeInMillis
        val points = DashboardTrendResolver.points(
            listOf(
                MonthlyCashflowRow("2026-07", 3_000L, 1_000L),
                MonthlyCashflowRow("2026-11", 8_000L, 2_000L)
            ),
            now
        )
        assertEquals(6, points.size)
        assertEquals("2026-06", points.first().monthKey)
        assertEquals(0L, points[0].incomeMinor)
        assertEquals(3_000L, points[1].incomeMinor)
        assertEquals("2026-11", points.last().monthKey)
        assertEquals(8_000L, points.last().incomeMinor)
        assertTrue(points.zipWithNext().all { (a, b) -> a.monthKey < b.monthKey })
    }
}
