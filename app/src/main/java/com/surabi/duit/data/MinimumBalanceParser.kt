package com.surabi.duit.data

/**
 * Strictly parses an optional account minimum-balance threshold as whole Rupiah.
 * A value of zero is valid and disables the minimum-balance warning.
 */
object MinimumBalanceParser {
    fun parse(input: String): Long? {
        val normalized = input.trim()
        if (normalized.isEmpty() || normalized.any { it !in '0'..'9' }) return null
        return normalized.toLongOrNull()
    }
}
