package com.surabi.duit.data

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TransactionDateBounds(
    val startAt: Long,
    val endAt: Long
)

/**
 * Parses inclusive local-calendar date filters without accepting partial or
 * normalized-invalid dates. The end date includes the whole local day.
 */
object TransactionDateRange {
    private val inputPattern = Regex("""\d{2}-\d{2}-\d{4}""")

    fun parse(startDate: String, endDate: String): TransactionDateBounds? {
        val cleanStart = startDate.trim()
        val cleanEnd = endDate.trim()

        val startAt = if (cleanStart.isEmpty()) {
            Long.MIN_VALUE
        } else {
            parseStartOfDay(cleanStart) ?: return null
        }

        val endAt = if (cleanEnd.isEmpty()) {
            Long.MAX_VALUE
        } else {
            parseEndOfDay(cleanEnd) ?: return null
        }

        if (startAt > endAt) return null
        return TransactionDateBounds(startAt, endAt)
    }

    private fun parseStartOfDay(value: String): Long? {
        val parsed = parseDate(value) ?: return null
        val parsedCalendar = Calendar.getInstance().apply { timeInMillis = parsed }
        return runCatching {
            Calendar.getInstance().apply {
                clear()
                set(
                    parsedCalendar.get(Calendar.YEAR),
                    parsedCalendar.get(Calendar.MONTH),
                    parsedCalendar.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.getOrNull()
    }

    private fun parseEndOfDay(value: String): Long? {
        val start = parseStartOfDay(value) ?: return null
        return runCatching {
            Calendar.getInstance().apply {
                timeInMillis = start
                add(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }.timeInMillis
        }.getOrNull()
    }

    private fun parseDate(value: String): Long? {
        if (!inputPattern.matches(value)) return null
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply {
            isLenient = false
        }
        val position = ParsePosition(0)
        val parsed = formatter.parse(value, position) ?: return null
        return parsed.time.takeIf { position.index == value.length && position.errorIndex < 0 }
    }
}
