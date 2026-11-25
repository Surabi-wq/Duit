package com.surabi.duit.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: FinanceDatabase) {
    private val dao = database.financeDao()

    val workspaces: Flow<List<WorkspaceEntity>> = dao.observeWorkspaces()

    fun accounts(workspaceId: Long): Flow<List<AccountBalanceRow>> =
        dao.observeAccountBalances(workspaceId)

    fun categories(workspaceId: Long): Flow<List<CategoryEntity>> =
        dao.observeCategories(workspaceId)

    fun transactions(workspaceId: Long): Flow<List<TransactionEntity>> =
        dao.observeTransactions(workspaceId)

    fun income(workspaceId: Long, startAt: Long, endAt: Long): Flow<Long> =
        dao.observeIncome(workspaceId, startAt, endAt)

    fun expenses(workspaceId: Long, startAt: Long, endAt: Long): Flow<Long> =
        dao.observeExpense(workspaceId, startAt, endAt)

    fun budgets(workspaceId: Long): Flow<List<BudgetOverviewRow>> =
        dao.observeBudgetOverview(workspaceId)

    suspend fun ensureDefaults() {
        database.withTransaction {
            if (dao.workspaceCount() == 0) {
                val now = System.currentTimeMillis()
                val workspaceId = dao.insertWorkspace(
                    WorkspaceEntity(name = "Pribadi", type = "PERSONAL", createdAt = now)
                )
                dao.insertAccount(
                    AccountEntity(
                        workspaceId = workspaceId,
                        name = "Dompet Utama",
                        type = "CASH",
                        openingBalanceMinor = 0L,
                        createdAt = now
                    )
                )
                dao.insertCategories(defaultCategories(workspaceId))
            }
        }
    }

    suspend fun createWorkspace(name: String, type: String): Long {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Nama workspace wajib diisi." }
        require(cleanName.length <= 60) { "Nama workspace maksimal 60 karakter." }
        val allowedTypes = setOf("PERSONAL", "FAMILY", "BUSINESS", "COMPANY", "PROJECT", "OTHER")
        require(type in allowedTypes) { "Jenis workspace tidak valid." }

        return database.withTransaction {
            val now = System.currentTimeMillis()
            val id = dao.insertWorkspace(
                WorkspaceEntity(name = cleanName, type = type, createdAt = now)
            )
            dao.insertAccount(
                AccountEntity(
                    workspaceId = id,
                    name = "Dompet Utama",
                    type = "CASH",
                    openingBalanceMinor = 0L,
                    createdAt = now
                )
            )
            dao.insertCategories(defaultCategories(id))
            id
        }
    }

    suspend fun createAccount(
        workspaceId: Long,
        name: String,
        type: String,
        openingBalance: Long
    ): Long {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Nama akun wajib diisi." }
        require(cleanName.length <= 60) { "Nama akun maksimal 60 karakter." }
        require(openingBalance >= 0L) { "Saldo awal tidak boleh negatif. Catat saldo minus sebagai kewajiban." }
        require(dao.getWorkspace(workspaceId) != null) { "Workspace tidak ditemukan." }
        val allowedTypes = setOf("CASH", "BANK", "SAVINGS", "EWALLET", "CREDIT_CARD", "INVESTMENT", "OTHER")
        require(type in allowedTypes) { "Jenis akun tidak valid." }

        return dao.insertAccount(
            AccountEntity(
                workspaceId = workspaceId,
                name = cleanName,
                type = type,
                openingBalanceMinor = openingBalance,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createTransaction(
        workspaceId: Long,
        type: String,
        amount: Long,
        title: String,
        fromAccountId: Long,
        toAccountId: Long?,
        categoryId: Long?,
        note: String
    ): Long {
        require(amount > 0L) { "Nominal harus lebih besar dari nol." }
        val cleanTitle = title.trim()
        require(cleanTitle.isNotEmpty()) { "Keterangan transaksi wajib diisi." }
        require(cleanTitle.length <= 100) { "Keterangan maksimal 100 karakter." }
        require(type in setOf("INCOME", "EXPENSE", "TRANSFER")) { "Jenis transaksi tidak valid." }

        val source = dao.getAccount(fromAccountId, workspaceId)
            ?: throw IllegalArgumentException("Akun sumber tidak ditemukan pada workspace ini.")

        if (type == "TRANSFER") {
            require(toAccountId != null) { "Akun tujuan transfer wajib dipilih." }
            require(toAccountId != fromAccountId) { "Akun sumber dan tujuan harus berbeda." }
            dao.getAccount(toAccountId, workspaceId)
                ?: throw IllegalArgumentException("Akun tujuan tidak ditemukan pada workspace ini.")
            require(categoryId == null) { "Transfer tidak memakai kategori pemasukan/pengeluaran." }
        } else {
            require(toAccountId == null) { "Akun tujuan hanya digunakan untuk transfer." }
            if (categoryId != null) {
                val categoryKind = dao.getCategoryKind(categoryId, workspaceId)
                    ?: throw IllegalArgumentException("Kategori tidak ditemukan pada workspace ini.")
                require(categoryKind == type) { "Kategori tidak sesuai dengan jenis transaksi." }
            }
        }

        val now = System.currentTimeMillis()
        return database.withTransaction {
            dao.insertTransaction(
                TransactionEntity(
                    workspaceId = workspaceId,
                    type = type,
                    title = cleanTitle,
                    amountMinor = amount,
                    fromAccountId = source.id,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    note = note.trim().take(500),
                    occurredAt = now,
                    createdAt = now
                )
            )
        }
    }

    suspend fun createMonthlyBudget(
        workspaceId: Long,
        name: String,
        limit: Long,
        categoryId: Long?
    ): Long {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Nama anggaran wajib diisi." }
        require(limit > 0L) { "Batas anggaran harus lebih besar dari nol." }
        require(dao.getWorkspace(workspaceId) != null) { "Workspace tidak ditemukan." }
        if (categoryId != null) {
            require(dao.getCategoryKind(categoryId, workspaceId) == "EXPENSE") {
                "Anggaran kategori hanya dapat memakai kategori pengeluaran."
            }
        }

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis

        return dao.insertBudget(
            BudgetEntity(
                workspaceId = workspaceId,
                name = cleanName,
                limitMinor = limit,
                categoryId = categoryId,
                periodStart = start,
                periodEnd = end,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    private fun defaultCategories(workspaceId: Long): List<CategoryEntity> {
        val expenseNames = listOf(
            "Makanan", "Transportasi", "Belanja", "Tagihan", "Kesehatan",
            "Pendidikan", "Hiburan", "Tempat Tinggal", "Lainnya"
        )
        val incomeNames = listOf(
            "Gaji", "Penjualan", "Bonus", "Hadiah", "Investasi", "Lainnya"
        )
        return expenseNames.map {
            CategoryEntity(
                workspaceId = workspaceId,
                name = it,
                kind = "EXPENSE",
                colorHex = "#D97757",
                isSystem = true
            )
        } + incomeNames.map {
            CategoryEntity(
                workspaceId = workspaceId,
                name = it,
                kind = "INCOME",
                colorHex = "#16875D",
                isSystem = true
            )
        }
    }
}
