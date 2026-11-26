package com.surabi.duit.data

/** Only ordinary cash-flow transactions can be safely copied into the standard transaction form. */
object TransactionDuplicationPolicy {
    private val supportedTypes = setOf("INCOME", "EXPENSE", "TRANSFER")

    fun canDuplicate(type: String): Boolean = type in supportedTypes
}
