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
        )
    ],
    indices = [
        Index(value = ["workspaceId", "occurredAt"]),
        Index("fromAccountId"),
        Index("toAccountId"),
        Index("categoryId")
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
    val isVoided: Boolean = false
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
        LIMIT 200
    """)
    fun observeTransactions(workspaceId: Long): Flow<List<TransactionEntity>>

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
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "duit-local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
