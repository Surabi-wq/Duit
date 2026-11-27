package com.surabi.duit.data

/**
 * Canonical profile types used by Duit 1.7.0. Legacy workspace types are mapped
 * deterministically so existing workspaces retain their data ownership.
 */
object ProfileTypePolicy {
    const val PERSONAL = "PERSONAL"
    const val FAMILY = "FAMILY"
    const val BUSINESS = "BUSINESS"
    const val PROJECT = "PROJECT"
    const val CUSTOM = "CUSTOM"

    val supportedTypes = setOf(PERSONAL, FAMILY, BUSINESS, PROJECT, CUSTOM)

    fun normalize(value: String): String {
        val normalized = value.trim().uppercase()
        require(normalized in supportedTypes) { "Jenis profil tidak didukung." }
        return normalized
    }

    fun fromWorkspaceType(value: String): String = when (value.trim().uppercase()) {
        "PERSONAL" -> PERSONAL
        "FAMILY" -> FAMILY
        "BUSINESS", "COMPANY" -> BUSINESS
        "PROJECT" -> PROJECT
        else -> CUSTOM
    }
}
