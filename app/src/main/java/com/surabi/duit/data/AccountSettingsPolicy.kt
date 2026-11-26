package com.surabi.duit.data

/** Validation rules shared by account setup and account settings. */
object AccountSettingsPolicy {
    val supportedTypes: Set<String> = setOf(
        "CASH", "BANK", "SAVINGS", "EWALLET", "CREDIT_CARD", "INVESTMENT", "OTHER"
    )

    fun normalizeName(value: String): String {
        val normalized = value.trim()
        require(normalized.isNotEmpty()) { "Nama akun wajib diisi." }
        require(normalized.length <= 60) { "Nama akun maksimal 60 karakter." }
        return normalized
    }

    fun requireSupportedType(type: String) {
        require(type in supportedTypes) { "Jenis akun tidak valid." }
    }

    fun requireValidMinimumBalance(value: Long) {
        require(value >= 0L) { "Batas saldo minimum tidak boleh negatif." }
    }
}

/** Guards selection rules for transfers between two distinct accounts. */
object AccountTransferPolicy {
    fun validateSelection(sourceAccountId: Long, destinationAccountId: Long?) {
        require(sourceAccountId > 0L) { "Akun sumber transfer tidak valid." }
        require(destinationAccountId != null) { "Akun tujuan transfer wajib dipilih." }
        require(destinationAccountId > 0L) { "Akun tujuan transfer tidak valid." }
        require(destinationAccountId != sourceAccountId) {
            "Akun sumber dan tujuan harus berbeda."
        }
    }
}
