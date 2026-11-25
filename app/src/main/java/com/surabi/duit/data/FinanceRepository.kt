package com.surabi.duit.data

import androidx.room.withTransaction
import androidx.paging.Pager
import androidx.paging.PagingData
import java.io.Writer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FinanceRepository(private val database: FinanceDatabase) {
    private val dao = database.financeDao()

    val workspaces: Flow<List<WorkspaceEntity>> = dao.observeWorkspaces()

    fun accounts(workspaceId: Long): Flow<List<AccountBalanceRow>> =
        dao.observeAccountBalances(workspaceId)

    fun categories(workspaceId: Long): Flow<List<CategoryEntity>> =
        dao.observeCategories(workspaceId)

    fun transactions(workspaceId: Long): Flow<List<TransactionEntity>> =
        dao.observeTransactions(workspaceId)

    fun pagedSearchTransactions(
        workspaceId: Long,
        query: String,
        type: String,
        startAt: Long,
        endAt: Long,
        accountId: Long? = null,
        categoryId: Long? = null,
        minAmount: Long? = null,
        maxAmount: Long? = null,
        sortOrder: String = "NEWEST"
    ): Flow<PagingData<TransactionEntity>> {
        require(type in setOf("ALL", "INCOME", "EXPENSE", "TRANSFER", "OTHER")) {
            "Filter jenis transaksi tidak valid."
        }
        require(startAt <= endAt) { "Rentang tanggal transaksi tidak valid." }
        require(minAmount == null || minAmount >= 0L) { "Nominal minimum tidak boleh negatif." }
        require(maxAmount == null || maxAmount >= 0L) { "Nominal maksimum tidak boleh negatif." }
        require(minAmount == null || maxAmount == null || minAmount <= maxAmount) {
            "Nominal minimum tidak boleh melebihi nominal maksimum."
        }
        return Pager(config = TransactionPagingPolicy.config()) {
            dao.pagingSearchTransactions(
                workspaceId = workspaceId,
                query = query.trim(),
                type = type,
                startAt = startAt,
                endAt = endAt,
                accountId = accountId,
                categoryId = categoryId,
                minAmount = minAmount,
                maxAmount = maxAmount,
                sortOrder = sortOrder
            )
        }.flow
    }

    /** Streams every matching non-voided transaction to CSV in bounded database batches. */
    suspend fun exportTransactionsToCsv(request: TransactionExportRequest, writer: Writer) {
        require(request.type in setOf("ALL", "INCOME", "EXPENSE", "TRANSFER", "OTHER")) {
            "Filter jenis transaksi tidak valid."
        }
        require(request.startAt <= request.endAt) { "Rentang tanggal transaksi tidak valid." }
        require(request.minAmount == null || request.minAmount >= 0L) { "Nominal minimum tidak boleh negatif." }
        require(request.maxAmount == null || request.maxAmount >= 0L) { "Nominal maksimum tidak boleh negatif." }
        require(request.minAmount == null || request.maxAmount == null || request.minAmount <= request.maxAmount) {
            "Nominal minimum tidak boleh melebihi nominal maksimum."
        }
        require(request.accountId == null || dao.getAccount(request.accountId, request.workspaceId) != null) {
            "Akun filter tidak ditemukan pada workspace ini."
        }
        if (request.categoryId != null) {
            require(dao.getCategoryKind(request.categoryId, request.workspaceId) != null) {
                "Kategori filter tidak ditemukan pada workspace ini."
            }
        }

        val batchSize = 200
        var offset = 0
        TransactionCsvExporter.appendHeader(writer)
        while (true) {
            val batch = dao.exportSearchTransactionsPage(
                workspaceId = request.workspaceId,
                query = request.query.trim(),
                type = request.type,
                startAt = request.startAt,
                endAt = request.endAt,
                accountId = request.accountId,
                categoryId = request.categoryId,
                minAmount = request.minAmount,
                maxAmount = request.maxAmount,
                sortOrder = request.sortOrder,
                limit = batchSize,
                offset = offset
            )
            if (batch.isEmpty()) break
            TransactionCsvExporter.appendRows(writer, batch, request.accountNames, request.categoryNames)
            writer.flush()
            offset += batch.size
            if (batch.size < batchSize) break
        }
    }

    fun income(workspaceId: Long, startAt: Long, endAt: Long): Flow<Long> =
        dao.observeIncome(workspaceId, startAt, endAt)

    fun expenses(workspaceId: Long, startAt: Long, endAt: Long): Flow<Long> =
        dao.observeExpense(workspaceId, startAt, endAt)

    fun monthlyCashflow(workspaceId: Long, startAt: Long, endAt: Long): Flow<List<MonthlyCashflowRow>> =
        dao.observeMonthlyCashflow(workspaceId, startAt, endAt)

    fun budgets(workspaceId: Long): Flow<List<BudgetOverviewRow>> =
        dao.observeBudgetOverview(workspaceId)

    fun goals(workspaceId: Long): Flow<List<GoalOverviewRow>> = dao.observeGoals(workspaceId)
    fun assets(workspaceId: Long): Flow<List<AssetEntity>> = dao.observeAssets(workspaceId)
    fun liabilities(workspaceId: Long): Flow<List<LiabilityOverviewRow>> = dao.observeLiabilities(workspaceId)
    fun receivables(workspaceId: Long): Flow<List<ReceivableOverviewRow>> = dao.observeReceivables(workspaceId)
    fun searchTransactions(
        workspaceId: Long,
        query: String,
        type: String,
        startAt: Long,
        endAt: Long,
        accountId: Long? = null,
        categoryId: Long? = null,
        minAmount: Long? = null,
        maxAmount: Long? = null,
        sortOrder: String = "NEWEST"
    ): Flow<List<TransactionEntity>> {
        return flow {
            require(type in setOf("ALL", "INCOME", "EXPENSE", "TRANSFER", "OTHER")) {
                "Filter jenis transaksi tidak valid."
            }
            require(startAt <= endAt) { "Rentang tanggal transaksi tidak valid." }
            require(accountId == null || dao.getAccount(accountId, workspaceId) != null) {
                "Akun filter tidak ditemukan pada workspace ini."
            }
            if (categoryId != null) {
                require(dao.getCategoryKind(categoryId, workspaceId) != null) {
                    "Kategori filter tidak ditemukan pada workspace ini."
                }
            }
            require(minAmount == null || minAmount >= 0L) { "Nominal minimum tidak boleh negatif." }
            require(maxAmount == null || maxAmount >= 0L) { "Nominal maksimum tidak boleh negatif." }
            require(minAmount == null || maxAmount == null || minAmount <= maxAmount) {
                "Nominal minimum tidak boleh melebihi nominal maksimum."
            }
            require(sortOrder in setOf("NEWEST", "OLDEST", "AMOUNT_ASC", "AMOUNT_DESC")) {
                "Pengurutan transaksi tidak valid."
            }
            emitAll(
                dao.searchTransactions(
                    workspaceId = workspaceId,
                    query = query.trim().take(100),
                    type = type,
                    startAt = startAt,
                    endAt = endAt,
                    accountId = accountId,
                    categoryId = categoryId,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    sortOrder = sortOrder
                )
            )
        }
    }

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

    suspend fun setAccountMinimumBalance(workspaceId: Long, accountId: Long, minimumBalance: Long) {
        require(minimumBalance >= 0L) { "Batas saldo minimum tidak boleh negatif." }

        val now = System.currentTimeMillis()
        database.withTransaction {
            val account = dao.getAccount(accountId, workspaceId)
                ?: throw IllegalArgumentException("Akun tidak ditemukan pada workspace ini.")
            val updated = dao.updateAccountMinimumBalance(accountId, workspaceId, minimumBalance)
            check(updated == 1) { "Batas saldo minimum gagal diperbarui." }
            dao.insertAuditLog(
                AuditLogEntity(
                    workspaceId = workspaceId,
                    entityType = "ACCOUNT",
                    entityId = accountId,
                    action = "MINIMUM_BALANCE_UPDATE",
                    description = "Batas saldo minimum akun ${account.name} diubah menjadi ${minimumBalance} rupiah.",
                    createdAt = now
                )
            )
        }
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
            val id = dao.insertTransaction(
                TransactionEntity(
                    workspaceId = workspaceId, type = type, title = cleanTitle, amountMinor = amount,
                    fromAccountId = source.id, toAccountId = toAccountId, categoryId = categoryId,
                    note = note.trim().take(500), occurredAt = now, createdAt = now
                )
            )
            dao.insertAuditLog(AuditLogEntity(workspaceId, "TRANSACTION", id, "CREATE", "Transaksi $type: $cleanTitle", now))
            id
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

    suspend fun createSavingsGoal(workspaceId: Long, name: String, targetAmount: Long, targetDate: Long?, priority: Int): Long {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Nama target wajib diisi." }
        require(cleanName.length <= 80) { "Nama target maksimal 80 karakter." }
        require(targetAmount > 0L) { "Target harus lebih besar dari nol." }
        require(priority in 1..5) { "Prioritas target harus 1 sampai 5." }
        require(dao.getWorkspace(workspaceId) != null) { "Workspace tidak ditemukan." }
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val id = dao.insertGoal(SavingsGoalEntity(workspaceId = workspaceId, name = cleanName,
                targetMinor = targetAmount, targetDate = targetDate, priority = priority, createdAt = now))
            dao.insertAuditLog(AuditLogEntity(workspaceId, "GOAL", id, "CREATE", "Membuat target tabungan $cleanName", now))
            id
        }
    }

    suspend fun moveGoalFunds(workspaceId: Long, goalId: Long, accountId: Long, amount: Long,
                              isWithdrawal: Boolean, note: String): Long {
        require(amount > 0L) { "Nominal harus lebih besar dari nol." }
        val goal = dao.getGoal(goalId, workspaceId) ?: throw IllegalArgumentException("Target tabungan tidak ditemukan.")
        val account = dao.getAccount(accountId, workspaceId) ?: throw IllegalArgumentException("Akun tidak ditemukan pada workspace ini.")
        val now = System.currentTimeMillis()
        return database.withTransaction {
            if (isWithdrawal) require(amount <= dao.goalSavedAmount(goal.id, workspaceId)) {
                "Penarikan melebihi dana yang sudah dialokasikan pada target."
            }
            val type = if (isWithdrawal) "GOAL_WITHDRAWAL" else "GOAL_CONTRIBUTION"
            val id = dao.insertTransaction(TransactionEntity(workspaceId = workspaceId, type = type,
                title = if (isWithdrawal) "Penarikan target: ${goal.name}" else "Setoran target: ${goal.name}",
                amountMinor = amount, fromAccountId = account.id, goalId = goal.id,
                note = note.trim().take(500), occurredAt = now, createdAt = now))
            dao.insertAuditLog(AuditLogEntity(workspaceId, "GOAL_MOVEMENT", id, "CREATE", "$type ${goal.name}", now))
            id
        }
    }

    suspend fun createAsset(workspaceId: Long, name: String, category: String,
                            acquisitionCost: Long, currentValue: Long, notes: String): Long {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Nama aset wajib diisi." }
        require(cleanName.length <= 80) { "Nama aset maksimal 80 karakter." }
        require(acquisitionCost >= 0L && currentValue >= 0L) { "Nilai aset tidak boleh negatif." }
        require(category in setOf("VEHICLE", "PROPERTY", "LAND", "GOLD", "ELECTRONIC", "EQUIPMENT", "OTHER")) {
            "Kategori aset tidak valid."
        }
        require(dao.getWorkspace(workspaceId) != null) { "Workspace tidak ditemukan." }
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val id = dao.insertAsset(AssetEntity(workspaceId = workspaceId, name = cleanName, category = category,
                acquisitionCostMinor = acquisitionCost, currentValueMinor = currentValue, acquiredAt = now,
                notes = notes.trim().take(500), updatedAt = now))
            dao.insertAuditLog(AuditLogEntity(workspaceId, "ASSET", id, "CREATE", "Mencatat aset $cleanName", now))
            id
        }
    }

    suspend fun updateAssetValue(workspaceId: Long, assetId: Long, currentValue: Long) {
        require(currentValue >= 0L) { "Nilai aset tidak boleh negatif." }
        val now = System.currentTimeMillis()
        database.withTransaction {
            require(dao.updateAssetValue(assetId, workspaceId, currentValue, now) == 1) {
                "Aset tidak ditemukan pada workspace ini."
            }
            dao.insertAuditLog(AuditLogEntity(workspaceId, "ASSET", assetId, "VALUE_UPDATE",
                "Memperbarui nilai aset menjadi $currentValue", now))
        }
    }

    suspend fun createLiability(workspaceId: Long, name: String, counterparty: String,
        originalAmount: Long, dueAt: Long?, receivingAccountId: Long?, notes: String): Long {
        val cleanName = name.trim()
        val cleanCounterparty = counterparty.trim()
        require(cleanName.isNotEmpty()) { "Nama utang wajib diisi." }
        require(cleanName.length <= 80) { "Nama utang maksimal 80 karakter." }
        require(cleanCounterparty.isNotEmpty()) { "Nama pemberi pinjaman wajib diisi." }
        require(originalAmount > 0L) { "Nilai utang harus lebih besar dari nol." }
        require(dao.getWorkspace(workspaceId) != null) { "Workspace tidak ditemukan." }
        val receivingAccount = receivingAccountId?.let {
            dao.getAccount(it, workspaceId) ?: throw IllegalArgumentException("Akun penerima tidak ditemukan pada workspace ini.")
        }
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val id = dao.insertLiability(LiabilityEntity(workspaceId = workspaceId, name = cleanName,
                counterparty = cleanCounterparty, originalAmountMinor = originalAmount, dueAt = dueAt,
                notes = notes.trim().take(500), createdAt = now))
            if (receivingAccount != null) {
                dao.insertTransaction(TransactionEntity(workspaceId = workspaceId, type = "LIABILITY_RECEIVED",
                    title = "Pencairan utang: $cleanName", amountMinor = originalAmount,
                    fromAccountId = receivingAccount.id, liabilityId = id,
                    note = "Pencairan pinjaman dari $cleanCounterparty", occurredAt = now, createdAt = now))
            }
            dao.insertAuditLog(AuditLogEntity(workspaceId, "LIABILITY", id, "CREATE", "Mencatat utang $cleanName", now))
            id
        }
    }

    suspend fun createReceivable(
        workspaceId: Long,
        name: String,
        counterparty: String,
        originalAmount: Long,
        dueAt: Long?,
        notes: String
    ): Long {
        val cleanName = name.trim()
        val cleanCounterparty = counterparty.trim()
        require(cleanName.isNotEmpty()) { "Nama piutang atau tagihan wajib diisi." }
        require(cleanName.length <= 80) { "Nama piutang maksimal 80 karakter." }
        require(cleanCounterparty.isNotEmpty()) { "Nama pelanggan atau pihak yang berutang wajib diisi." }
        require(cleanCounterparty.length <= 100) { "Nama pihak maksimal 100 karakter." }
        require(originalAmount > 0L) { "Nilai piutang harus lebih besar dari nol." }
        require(notes.length <= 500) { "Catatan maksimal 500 karakter." }
        require(dao.getWorkspace(workspaceId) != null) { "Workspace tidak ditemukan." }

        val now = System.currentTimeMillis()
        return database.withTransaction {
            val id = dao.insertReceivable(
                ReceivableEntity(
                    workspaceId = workspaceId,
                    name = cleanName,
                    counterparty = cleanCounterparty,
                    originalAmountMinor = originalAmount,
                    dueAt = dueAt,
                    notes = notes.trim(),
                    createdAt = now
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(workspaceId, "RECEIVABLE", id, "CREATE",
                    "Mencatat piutang $cleanName kepada $cleanCounterparty", now)
            )
            id
        }
    }

    suspend fun collectReceivable(
        workspaceId: Long,
        receivableId: Long,
        accountId: Long,
        amount: Long,
        note: String
    ): Long {
        require(amount > 0L) { "Nominal penerimaan harus lebih besar dari nol." }
        require(note.length <= 500) { "Catatan maksimal 500 karakter." }
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val receivable = dao.getReceivable(receivableId, workspaceId)
                ?: throw IllegalArgumentException("Piutang tidak ditemukan pada workspace ini.")
            val account = dao.getAccount(accountId, workspaceId)
                ?: throw IllegalArgumentException("Akun penerima tidak ditemukan pada workspace ini.")
            val outstanding = dao.receivableOutstanding(receivable.id, workspaceId) ?: 0L
            require(outstanding > 0L) { "Piutang ini sudah lunas." }
            require(amount <= outstanding) { "Penerimaan melebihi sisa piutang." }

            val id = dao.insertTransaction(
                TransactionEntity(
                    workspaceId = workspaceId,
                    type = "RECEIVABLE_COLLECTION",
                    title = "Penerimaan piutang: ${receivable.name}",
                    amountMinor = amount,
                    fromAccountId = account.id,
                    note = note.trim(),
                    occurredAt = now,
                    createdAt = now,
                    receivableId = receivable.id
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(workspaceId, "RECEIVABLE", receivable.id, "COLLECTION",
                    "Menerima ${amount} rupiah dari ${receivable.counterparty}", now)
            )
            id
        }
    }

    suspend fun payLiability(workspaceId: Long, liabilityId: Long, accountId: Long,
                             amount: Long, note: String): Long {
        require(amount > 0L) { "Nominal pembayaran harus lebih besar dari nol." }
        val liability = dao.getLiability(liabilityId, workspaceId)
            ?: throw IllegalArgumentException("Utang tidak ditemukan.")
        val account = dao.getAccount(accountId, workspaceId)
            ?: throw IllegalArgumentException("Akun pembayaran tidak ditemukan pada workspace ini.")
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val outstanding = dao.liabilityOutstanding(liability.id, workspaceId) ?: 0L
            require(outstanding > 0L) { "Utang ini sudah lunas." }
            require(amount <= outstanding) { "Pembayaran melebihi sisa utang." }
            val id = dao.insertTransaction(TransactionEntity(workspaceId = workspaceId, type = "LIABILITY_PAYMENT",
                title = "Pembayaran utang: ${liability.name}", amountMinor = amount,
                fromAccountId = account.id, liabilityId = liability.id, note = note.trim().take(500),
                occurredAt = now, createdAt = now))
            dao.insertAuditLog(AuditLogEntity(workspaceId, "LIABILITY_PAYMENT", id, "CREATE",
                "Membayar utang ${liability.name}", now))
            id
        }
    }

    suspend fun voidTransaction(workspaceId: Long, transactionId: Long, reason: String) {
        val cleanReason = reason.trim()
        require(cleanReason.isNotEmpty()) { "Alasan pembatalan wajib diisi." }
        require(cleanReason.length <= 300) { "Alasan maksimal 300 karakter." }
        val now = System.currentTimeMillis()
        database.withTransaction {
            val existing = dao.getTransaction(transactionId, workspaceId)
                ?: throw IllegalArgumentException("Transaksi tidak ditemukan.")
            require(!existing.isVoided) { "Transaksi sudah dibatalkan." }
            require(dao.markTransactionVoided(transactionId, workspaceId) == 1) { "Transaksi gagal dibatalkan." }
            dao.insertAuditLog(AuditLogEntity(workspaceId, "TRANSACTION", transactionId, "VOID",
                "Membatalkan ${existing.title}: $cleanReason", now))
        }
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
