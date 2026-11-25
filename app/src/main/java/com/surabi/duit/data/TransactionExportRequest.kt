package com.surabi.duit.data

/** Immutable snapshot of filters used when exporting the full matching transaction history. */
data class TransactionExportRequest(
    val workspaceId: Long,
    val query: String,
    val type: String,
    val startAt: Long,
    val endAt: Long,
    val accountId: Long?,
    val categoryId: Long?,
    val minAmount: Long?,
    val maxAmount: Long?,
    val sortOrder: String,
    val accountNames: Map<Long, String>,
    val categoryNames: Map<Long, String>
)
