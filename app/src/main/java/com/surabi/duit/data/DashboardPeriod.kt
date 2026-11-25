package com.surabi.duit.data

import java.util.Calendar

enum class DashboardPeriod {
    THIS_MONTH,
    LAST_30_DAYS,
    THIS_YEAR
}

data class DashboardPeriodRange(
    val startAt: Long,
    val endAt: Long
)

/**
 * Produces local-calendar date ranges for dashboard queries.
 * All bounds are inclusive and end at the final millisecond of the current day.
 */
object DashboardPeriodResolver {
    fun bounds(period: DashboardPeriod, now: Long = System.currentTimeMillis()): DashboardPeriodRange {
        val end = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val start = (end.clone() as Calendar).apply {
            when (period) {
                DashboardPeriod.THIS_MONTH -> set(Calendar.DAY_OF_MONTH, 1)
                DashboardPeriod.LAST_30_DAYS -> add(Calendar.DAY_OF_MONTH, -29)
                DashboardPeriod.THIS_YEAR -> {
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return DashboardPeriodRange(start.timeInMillis, end.timeInMillis)
    }

    fun label(period: DashboardPeriod): String = when (period) {
        DashboardPeriod.THIS_MONTH -> "Bulan ini"
        DashboardPeriod.LAST_30_DAYS -> "30 hari terakhir"
        DashboardPeriod.THIS_YEAR -> "Tahun ini"
    }
}
