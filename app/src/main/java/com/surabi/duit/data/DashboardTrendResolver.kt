package com.surabi.duit.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardTrendPoint(
    val monthKey: String,
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)

/**
 * Builds six local-calendar month buckets for the dashboard.
 * Missing months are represented as zero values; values are never fabricated.
 */
object DashboardTrendResolver {
    private val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val labelFormat = SimpleDateFormat("MMM", Locale("id", "ID"))

    fun bounds(now: Long = System.currentTimeMillis()): DashboardPeriodRange {
        val end = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val start = (end.clone() as Calendar).apply {
            add(Calendar.MONTH, -5)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return DashboardPeriodRange(start.timeInMillis, end.timeInMillis)
    }

    fun points(
        rows: List<MonthlyCashflowRow>,
        now: Long = System.currentTimeMillis()
    ): List<DashboardTrendPoint> {
        val valuesByMonth = rows.associateBy { it.monthKey }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.add(Calendar.MONTH, -5)

        return (0 until 6).map {
            val date = calendar.time
            val key = keyFormat.format(date)
            val row = valuesByMonth[key]
            DashboardTrendPoint(
                monthKey = key,
                label = labelFormat.format(date).replaceFirstChar { it.uppercase(Locale("id", "ID")) },
                incomeMinor = row?.incomeMinor ?: 0L,
                expenseMinor = row?.expenseMinor ?: 0L
            ).also {
                calendar.add(Calendar.MONTH, 1)
            }
        }
    }
}
