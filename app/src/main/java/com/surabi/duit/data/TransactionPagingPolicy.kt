package com.surabi.duit.data

import androidx.paging.PagingConfig

/** Bounded paging policy for long transaction histories. */
internal object TransactionPagingPolicy {
    const val PAGE_SIZE = 40
    const val INITIAL_LOAD_SIZE = 60
    const val PREFETCH_DISTANCE = 20
    const val MAX_CACHED_ITEMS = 200

    fun config(): PagingConfig = PagingConfig(
        pageSize = PAGE_SIZE,
        initialLoadSize = INITIAL_LOAD_SIZE,
        prefetchDistance = PREFETCH_DISTANCE,
        enablePlaceholders = false,
        maxSize = MAX_CACHED_ITEMS
    )
}
