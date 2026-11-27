package com.surabi.duit.data

object MoneyAmount {
    /**
     * Parses a whole-rupiah amount without floating point conversion.
     * Input must contain digits only; formatting belongs in the UI layer.
     */
    fun parseWholeRupiah(input: String): Long? {
        val value = input.trim()
        if (value.isEmpty() || value.any { !it.isDigit() }) return null
        return value.toLongOrNull()
    }

    fun requirePositiveRupiah(input: String): Long {
        val amount = parseWholeRupiah(input)
            ?: throw IllegalArgumentException("Nominal harus berupa angka rupiah yang valid.")
        require(amount > 0L) { "Nominal harus lebih besar dari nol." }
        return amount
    }
}
