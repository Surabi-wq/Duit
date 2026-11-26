package com.surabi.duit.data

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Calendar-based recurrence arithmetic. Month/year schedules retain the original day anchor,
 * so a January 31 schedule moves to February's last day and returns to March 31.
 */
object RecurringSchedulePolicy {
    val supportedCadences = setOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY")

    fun parseDate(value: String): Long? {
        if (!Regex("""\d{2}-\d{2}-\d{4}""").matches(value)) return null
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")).apply {
            isLenient = false
        }
        val position = ParsePosition(0)
        val parsed: Date = formatter.parse(value, position) ?: return null
        if (position.index != value.length || position.errorIndex >= 0) return null
        return Calendar.getInstance().apply {
            timeInMillis = parsed.time
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun nextDueAt(
        currentDueAt: Long,
        cadence: String,
        intervalCount: Int,
        anchorDayOfMonth: Int,
        preserveMonthEnd: Boolean
    ): Long {
        require(cadence in supportedCadences) { "Frekuensi jadwal tidak valid." }
        require(intervalCount in 1..36) { "Interval jadwal harus antara 1 dan 36." }
        require(anchorDayOfMonth in 1..31) { "Tanggal acuan jadwal tidak valid." }

        val calendar = Calendar.getInstance().apply { timeInMillis = currentDueAt }
        when (cadence) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_MONTH, intervalCount)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, intervalCount)
            "MONTHLY" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, intervalCount)
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, if (preserveMonthEnd) lastDay else anchorDayOfMonth.coerceAtMost(lastDay))
            }
            "YEARLY" -> {
                calendar.add(Calendar.YEAR, intervalCount)
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, anchorDayOfMonth.coerceAtMost(lastDay))
            }
        }
        return calendar.timeInMillis
    }

    fun anchorDay(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_MONTH)

    fun isMonthEnd(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun cadenceLabel(cadence: String, intervalCount: Int): String {
        val unit = when (cadence) {
            "DAILY" -> "hari"
            "WEEKLY" -> "minggu"
            "MONTHLY" -> "bulan"
            "YEARLY" -> "tahun"
            else -> "periode"
        }
        return if (intervalCount == 1) "Setiap $unit" else "Setiap $intervalCount $unit"
    }
}
