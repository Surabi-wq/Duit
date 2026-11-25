package com.surabi.duit.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val createdAt: Long
)

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workspaceId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val type: String,
    val openingBalanceMinor: Long,
    val minimumBalanceMinor: Long = 0,
    val isArchived: Boolean = false,
    val createdAt: Long
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workspaceId"),
        Index(value = ["workspaceId", "name", "kind"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val kind: String,
    val colorHex: String,
    val isSystem: Boolean = false
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromAccountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = LiabilityEntity::class,
            parentColumns = ["id"],
            childColumns = ["liabilityId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ReceivableEntity::class,
            parentColumns = ["id"],
            childColumns = ["receivableId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["workspaceId", "occurredAt"]),
        Index("fromAccountId"),
        Index("toAccountId"),
        Index("categoryId"),
        Index("goalId"),
        Index("liabilityId"),
        Index("receivableId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val type: String,
    val title: String,
    val amountMinor: Long,
    val fromAccountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val occurredAt: Long,
    val createdAt: Long,
    val isVoided: Boolean = false,
    val goalId: Long? = null,
    val liabilityId: Long? = null,
    val receivableId: Long? = null
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("workspaceId"), Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val limitMinor: Long,
    val categoryId: Long? = null,
    val periodStart: Long,
    val periodEnd: Long,
    val createdAt: Long
)

data class MonthlyCashflowRow(
    val monthKey: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)

data class AccountBalanceRow(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val type: String,
    val openingBalanceMinor: Long,
    val balanceMinor: Long,
    val minimumBalanceMinor: Long
)

data class BudgetOverviewRow(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val limitMinor: Long,
    val categoryId: Long?,
    val periodStart: Long,
    val periodEnd: Long,
    val spentMinor: Long
)

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(entity = WorkspaceEntity::class, parentColumns = ["id"], childColumns = ["workspaceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("workspaceId")]
)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val targetMinor: Long,
    val targetDate: Long?,
    val priority: Int,
    val createdAt: Long,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "assets",
    foreignKeys = [
        ForeignKey(entity = WorkspaceEntity::class, parentColumns = ["id"], childColumns = ["workspaceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("workspaceId"), Index(value = ["workspaceId", "name"])]
)
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val category: String,
    val acquisitionCostMinor: Long,
    val currentValueMinor: Long,
    val acquiredAt: Long,
    val notes: String = "",
    val updatedAt: Long
)

@Entity(
    tableName = "liabilities",
    foreignKeys = [
        ForeignKey(entity = WorkspaceEntity::class, parentColumns = ["id"], childColumns = ["workspaceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("workspaceId"), Index("dueAt")]
)
data class LiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val counterparty: String,
    val originalAmountMinor: Long,
    val dueAt: Long?,
    val notes: String = "",
    val createdAt: Long,
    val isClosed: Boolean = false
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["workspaceId", "createdAt"]), Index("entityId")]
)
data class AuditLogEntity(
    val workspaceId: Long,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val description: String,
    val createdAt: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Entity(
    tableName = "receivables",
    foreignKeys = [
        ForeignKey(entity = WorkspaceEntity::class, parentColumns = ["id"], childColumns = ["workspaceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("workspaceId"), Index("dueAt")]
)
data class ReceivableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceId: Long,
    val name: String,
    val counterparty: String,
    val originalAmountMinor: Long,
    val dueAt: Long?,
    val notes: String = "",
    val createdAt: Long
)

data class GoalOverviewRow(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val targetMinor: Long,
    val targetDate: Long?,
    val priority: Int,
    val savedMinor: Long
)

data class LiabilityOverviewRow(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val counterparty: String,
    val originalAmountMinor: Long,
    val outstandingMinor: Long,
    val dueAt: Long?,
    val isClosed: Boolean
)

data class ReceivableOverviewRow(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val counterparty: String,
    val originalAmountMinor: Long,
    val outstandingMinor: Long,
    val dueAt: Long?
)

@Dao
interface FinanceDao {
    @Query("SELECT * FROM workspaces ORDER BY createdAt ASC")
    fun observeWorkspaces(): Flow<List<WorkspaceEntity>>

    @Query("SELECT COUNT(*) FROM workspaces")
    suspend fun workspaceCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkspace(workspace: WorkspaceEntity): Long

    @Query("SELECT * FROM workspaces WHERE id = :workspaceId LIMIT 1")
    suspend fun getWorkspace(workspaceId: Long): WorkspaceEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity): Long

    @Query("SELECT * FROM accounts WHERE id = :accountId AND workspaceId = :workspaceId LIMIT 1")
    suspend fun getAccount(accountId: Long, workspaceId: Long): AccountEntity?

    @Query("UPDATE accounts SET minimumBalanceMinor = :minimumBalanceMinor WHERE id = :accountId AND workspaceId = :workspaceId")
    suspend fun updateAccountMinimumBalance(accountId: Long, workspaceId: Long, minimumBalanceMinor: Long): Int

    @Query("""
        SELECT
            a.id AS id,
            a.workspaceId AS workspaceId,
            a.name AS name,
            a.type AS type,
            a.openingBalanceMinor AS openingBalanceMinor,
            a.minimumBalanceMinor AS minimumBalanceMinor,
            a.openingBalanceMinor + COALESCE(SUM(
                CASE
                    WHEN t.type = 'INCOME' AND t.fromAccountId = a.id THEN t.amountMinor
                    WHEN t.type = 'EXPENSE' AND t.fromAccountId = a.id THEN -t.amountMinor
                    WHEN t.type = 'TRANSFER' AND t.fromAccountId = a.id THEN -t.amountMinor
                    WHEN t.type = 'TRANSFER' AND t.toAccountId = a.id THEN t.amountMinor
                    WHEN t.type = 'GOAL_CONTRIBUTION' AND t.fromAccountId = a.id THEN -t.amountMinor
                    WHEN t.type = 'GOAL_WITHDRAWAL' AND t.fromAccountId = a.id THEN t.amountMinor
                    WHEN t.type = 'LIABILITY_PAYMENT' AND t.fromAccountId = a.id THEN -t.amountMinor
                    WHEN t.type = 'LIABILITY_RECEIVED' AND t.fromAccountId = a.id THEN t.amountMinor
                    WHEN t.type = 'RECEIVABLE_COLLECTION' AND t.fromAccountId = a.id THEN t.amountMinor
                    ELSE 0
                END
            ), 0) AS balanceMinor
        FROM accounts a
        LEFT JOIN transactions t
            ON t.workspaceId = a.workspaceId
            AND t.isVoided = 0
            AND (t.fromAccountId = a.id OR (t.type = 'TRANSFER' AND t.toAccountId = a.id))
        WHERE a.workspaceId = :workspaceId AND a.isArchived = 0
        GROUP BY a.id
        ORDER BY a.createdAt ASC
    """)
    fun observeAccountBalances(workspaceId: Long): Flow<List<AccountBalanceRow>>

    @Query("SELECT * FROM categories WHERE workspaceId = :workspaceId ORDER BY kind ASC, name ASC")
    fun observeCategories(workspaceId: Long): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT kind FROM categories WHERE id = :categoryId AND workspaceId = :workspaceId LIMIT 1")
    suspend fun getCategoryKind(categoryId: Long, workspaceId: Long): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("""
        SELECT * FROM transactions
        WHERE workspaceId = :workspaceId AND isVoided = 0
        ORDER BY occurredAt DESC, id DESC
        LIMIT 20
    """)
    fun observeTransactions(workspaceId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE workspaceId = :workspaceId AND isVoided = 0
    """)
    fun observeTransactionCount(workspaceId: Long): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0) FROM transactions
        WHERE workspaceId = :workspaceId AND type = 'INCOME'
        AND isVoided = 0 AND occurredAt BETWEEN :startAt AND :endAt
    """)
    fun observeIncome(workspaceId: Long, startAt: Long, endAt: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0) FROM transactions
        WHERE workspaceId = :workspaceId AND type = 'EXPENSE'
        AND isVoided = 0 AND occurredAt BETWEEN :startAt AND :endAt
    """)
    fun observeExpense(workspaceId: Long, startAt: Long, endAt: Long): Flow<Long>

    @Query("""
        SELECT
            strftime('%Y-%m', occurredAt / 1000, 'unixepoch', 'localtime') AS monthKey,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS incomeMinor,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expenseMinor
        FROM transactions
        WHERE workspaceId = :workspaceId
          AND isVoided = 0
          AND type IN ('INCOME', 'EXPENSE')
          AND occurredAt BETWEEN :startAt AND :endAt
        GROUP BY monthKey
        ORDER BY monthKey ASC
    """)
    fun observeMonthlyCashflow(workspaceId: Long, startAt: Long, endAt: Long): Flow<List<MonthlyCashflowRow>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Query("SELECT * FROM savings_goals WHERE id = :goalId AND workspaceId = :workspaceId AND isArchived = 0 LIMIT 1")
    suspend fun getGoal(goalId: Long, workspaceId: Long): SavingsGoalEntity?

    @Query("""
        SELECT g.id AS id, g.workspaceId AS workspaceId, g.name AS name,
               g.targetMinor AS targetMinor, g.targetDate AS targetDate, g.priority AS priority,
               COALESCE(SUM(
                   CASE WHEN t.type = 'GOAL_CONTRIBUTION' AND t.isVoided = 0 THEN t.amountMinor
                        WHEN t.type = 'GOAL_WITHDRAWAL' AND t.isVoided = 0 THEN -t.amountMinor
                        ELSE 0 END
               ), 0) AS savedMinor
        FROM savings_goals g
        LEFT JOIN transactions t ON t.goalId = g.id AND t.workspaceId = g.workspaceId
        WHERE g.workspaceId = :workspaceId AND g.isArchived = 0
        GROUP BY g.id
        ORDER BY g.priority DESC, g.createdAt DESC
    """)
    fun observeGoals(workspaceId: Long): Flow<List<GoalOverviewRow>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE WHEN type = 'GOAL_CONTRIBUTION' THEN amountMinor
                 WHEN type = 'GOAL_WITHDRAWAL' THEN -amountMinor
                 ELSE 0 END
        ), 0)
        FROM transactions
        WHERE goalId = :goalId AND workspaceId = :workspaceId AND isVoided = 0
    """)
    suspend fun goalSavedAmount(goalId: Long, workspaceId: Long): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Query("SELECT * FROM assets WHERE workspaceId = :workspaceId ORDER BY updatedAt DESC, id DESC")
    fun observeAssets(workspaceId: Long): Flow<List<AssetEntity>>

    @Query("UPDATE assets SET currentValueMinor = :currentValue, updatedAt = :updatedAt WHERE id = :assetId AND workspaceId = :workspaceId")
    suspend fun updateAssetValue(assetId: Long, workspaceId: Long, currentValue: Long, updatedAt: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLiability(liability: LiabilityEntity): Long

    @Query("SELECT * FROM liabilities WHERE id = :liabilityId AND workspaceId = :workspaceId LIMIT 1")
    suspend fun getLiability(liabilityId: Long, workspaceId: Long): LiabilityEntity?

    @Query("""
        SELECT l.id AS id, l.workspaceId AS workspaceId, l.name AS name,
               l.counterparty AS counterparty, l.originalAmountMinor AS originalAmountMinor,
               MAX(0, l.originalAmountMinor - COALESCE(SUM(
                   CASE WHEN t.type = 'LIABILITY_PAYMENT' AND t.isVoided = 0 THEN t.amountMinor ELSE 0 END
               ), 0)) AS outstandingMinor,
               l.dueAt AS dueAt, l.isClosed AS isClosed
        FROM liabilities l
        LEFT JOIN transactions t ON t.liabilityId = l.id AND t.workspaceId = l.workspaceId
        WHERE l.workspaceId = :workspaceId
        GROUP BY l.id
        ORDER BY CASE WHEN l.dueAt IS NULL THEN 1 ELSE 0 END, l.dueAt ASC
    """)
    fun observeLiabilities(workspaceId: Long): Flow<List<LiabilityOverviewRow>>

    @Query("""
        SELECT MAX(0, l.originalAmountMinor - COALESCE(SUM(
            CASE WHEN t.type = 'LIABILITY_PAYMENT' AND t.isVoided = 0 THEN t.amountMinor ELSE 0 END
        ), 0))
        FROM liabilities l
        LEFT JOIN transactions t ON t.liabilityId = l.id AND t.workspaceId = l.workspaceId
        WHERE l.id = :liabilityId AND l.workspaceId = :workspaceId
        GROUP BY l.id
    """)
    suspend fun liabilityOutstanding(liabilityId: Long, workspaceId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReceivable(receivable: ReceivableEntity): Long

    @Query("SELECT * FROM receivables WHERE id = :receivableId AND workspaceId = :workspaceId LIMIT 1")
    suspend fun getReceivable(receivableId: Long, workspaceId: Long): ReceivableEntity?

    @Query("""
        SELECT r.id AS id, r.workspaceId AS workspaceId, r.name AS name,
               r.counterparty AS counterparty, r.originalAmountMinor AS originalAmountMinor,
               MAX(0, r.originalAmountMinor - COALESCE(SUM(
                   CASE WHEN t.type = 'RECEIVABLE_COLLECTION' AND t.isVoided = 0
                        THEN t.amountMinor ELSE 0 END
               ), 0)) AS outstandingMinor,
               r.dueAt AS dueAt
        FROM receivables r
        LEFT JOIN transactions t ON t.receivableId = r.id AND t.workspaceId = r.workspaceId
        WHERE r.workspaceId = :workspaceId
        GROUP BY r.id
        ORDER BY CASE WHEN r.dueAt IS NULL THEN 1 ELSE 0 END, r.dueAt ASC, r.createdAt DESC
    """)
    fun observeReceivables(workspaceId: Long): Flow<List<ReceivableOverviewRow>>

    @Query("""
        SELECT MAX(0, r.originalAmountMinor - COALESCE(SUM(
            CASE WHEN t.type = 'RECEIVABLE_COLLECTION' AND t.isVoided = 0
                 THEN t.amountMinor ELSE 0 END
        ), 0))
        FROM receivables r
        LEFT JOIN transactions t ON t.receivableId = r.id AND t.workspaceId = r.workspaceId
        WHERE r.id = :receivableId AND r.workspaceId = :workspaceId
        GROUP BY r.id
    """)
    suspend fun receivableOutstanding(receivableId: Long, workspaceId: Long): Long?

    @Query("SELECT * FROM transactions WHERE id = :transactionId AND workspaceId = :workspaceId LIMIT 1")
    suspend fun getTransaction(transactionId: Long, workspaceId: Long): TransactionEntity?

    @Query("UPDATE transactions SET isVoided = 1 WHERE id = :transactionId AND workspaceId = :workspaceId AND isVoided = 0")
    suspend fun markTransactionVoided(transactionId: Long, workspaceId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("""
        SELECT t.* FROM transactions AS t
        WHERE t.workspaceId = :workspaceId AND t.isVoided = 0
          AND (
              :query = ''
              OR t.title LIKE '%' || :query || '%'
              OR t.note LIKE '%' || :query || '%'
              OR EXISTS (
                  SELECT 1 FROM categories AS c
                  WHERE c.id = t.categoryId
                    AND c.workspaceId = t.workspaceId
                    AND c.name LIKE '%' || :query || '%'
              )
          )
          AND (
              :type = 'ALL'
              OR (:type = 'OTHER' AND t.type NOT IN ('INCOME', 'EXPENSE', 'TRANSFER'))
              OR t.type = :type
          )
          AND t.occurredAt BETWEEN :startAt AND :endAt
          AND (:accountId IS NULL OR t.fromAccountId = :accountId OR t.toAccountId = :accountId)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:minAmount IS NULL OR t.amountMinor >= :minAmount)
          AND (:maxAmount IS NULL OR t.amountMinor <= :maxAmount)
        ORDER BY
          CASE WHEN :sortOrder = 'OLDEST' THEN t.occurredAt END ASC,
          CASE WHEN :sortOrder = 'AMOUNT_ASC' THEN t.amountMinor END ASC,
          CASE WHEN :sortOrder = 'AMOUNT_DESC' THEN t.amountMinor END DESC,
          t.occurredAt DESC,
          t.id DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchTransactions(
        workspaceId: Long,
        query: String,
        type: String,
        startAt: Long,
        endAt: Long,
        accountId: Long?,
        categoryId: Long?,
        minAmount: Long?,
        maxAmount: Long?,
        sortOrder: String,
        limit: Int,
        offset: Int
    ): List<TransactionEntity>

    @Query("""
        SELECT COUNT(*) FROM transactions AS t
        WHERE t.workspaceId = :workspaceId AND t.isVoided = 0
          AND (
              :query = ''
              OR t.title LIKE '%' || :query || '%'
              OR t.note LIKE '%' || :query || '%'
              OR EXISTS (
                  SELECT 1 FROM categories AS c
                  WHERE c.id = t.categoryId
                    AND c.workspaceId = t.workspaceId
                    AND c.name LIKE '%' || :query || '%'
              )
          )
          AND (
              :type = 'ALL'
              OR (:type = 'OTHER' AND t.type NOT IN ('INCOME', 'EXPENSE', 'TRANSFER'))
              OR t.type = :type
          )
          AND t.occurredAt BETWEEN :startAt AND :endAt
          AND (:accountId IS NULL OR t.fromAccountId = :accountId OR t.toAccountId = :accountId)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:minAmount IS NULL OR t.amountMinor >= :minAmount)
          AND (:maxAmount IS NULL OR t.amountMinor <= :maxAmount)
    """)
    suspend fun countSearchTransactions(
        workspaceId: Long,
        query: String,
        type: String,
        startAt: Long,
        endAt: Long,
        accountId: Long?,
        categoryId: Long?,
        minAmount: Long?,
        maxAmount: Long?
    ): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Query("""
        SELECT
            b.id AS id,
            b.workspaceId AS workspaceId,
            b.name AS name,
            b.limitMinor AS limitMinor,
            b.categoryId AS categoryId,
            b.periodStart AS periodStart,
            b.periodEnd AS periodEnd,
            COALESCE(SUM(
                CASE WHEN t.type = 'EXPENSE' AND t.isVoided = 0 THEN t.amountMinor ELSE 0 END
            ), 0) AS spentMinor
        FROM budgets b
        LEFT JOIN transactions t
            ON t.workspaceId = b.workspaceId
            AND t.occurredAt BETWEEN b.periodStart AND b.periodEnd
            AND (b.categoryId IS NULL OR t.categoryId = b.categoryId)
        WHERE b.workspaceId = :workspaceId
        GROUP BY b.id
        ORDER BY b.periodStart DESC, b.id DESC
    """)
    fun observeBudgetOverview(workspaceId: Long): Flow<List<BudgetOverviewRow>>
}

@Database(
    entities = [
        WorkspaceEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        AssetEntity::class,
        LiabilityEntity::class,
        ReceivableEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workspaceId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        targetMinor INTEGER NOT NULL,
                        targetDate INTEGER,
                        priority INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL,
                        FOREIGN KEY(workspaceId) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_goals_workspaceId ON savings_goals(workspaceId)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS assets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workspaceId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        acquisitionCostMinor INTEGER NOT NULL,
                        currentValueMinor INTEGER NOT NULL,
                        acquiredAt INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(workspaceId) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_assets_workspaceId ON assets(workspaceId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_assets_workspaceId_name ON assets(workspaceId, name)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS liabilities (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workspaceId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        counterparty TEXT NOT NULL,
                        originalAmountMinor INTEGER NOT NULL,
                        dueAt INTEGER,
                        notes TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isClosed INTEGER NOT NULL,
                        FOREIGN KEY(workspaceId) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_liabilities_workspaceId ON liabilities(workspaceId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_liabilities_dueAt ON liabilities(dueAt)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workspaceId INTEGER NOT NULL,
                        entityType TEXT NOT NULL,
                        entityId INTEGER NOT NULL,
                        action TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_workspaceId_createdAt ON audit_logs(workspaceId, createdAt)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_audit_logs_entityId ON audit_logs(entityId)")
                database.execSQL("ALTER TABLE transactions ADD COLUMN goalId INTEGER REFERENCES savings_goals(id) ON DELETE SET NULL")
                database.execSQL("ALTER TABLE transactions ADD COLUMN liabilityId INTEGER REFERENCES liabilities(id) ON DELETE SET NULL")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_goalId ON transactions(goalId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_liabilityId ON transactions(liabilityId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS receivables (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workspaceId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        counterparty TEXT NOT NULL,
                        originalAmountMinor INTEGER NOT NULL,
                        dueAt INTEGER,
                        notes TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(workspaceId) REFERENCES workspaces(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_receivables_workspaceId ON receivables(workspaceId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_receivables_dueAt ON receivables(dueAt)")
                database.execSQL("ALTER TABLE transactions ADD COLUMN receivableId INTEGER REFERENCES receivables(id) ON DELETE SET NULL")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_receivableId ON transactions(receivableId)")
            }
        }

        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "duit-local.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
        }
    }
}
