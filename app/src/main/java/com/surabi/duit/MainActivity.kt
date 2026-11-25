package com.surabi.duit

import android.os.Bundle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Savings
import com.surabi.duit.data.DashboardPeriod
import com.surabi.duit.data.DashboardPeriodResolver
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surabi.duit.data.AccountBalanceRow
import com.surabi.duit.data.MinimumBalanceParser
import com.surabi.duit.data.AssetEntity
import com.surabi.duit.data.BudgetOverviewRow
import com.surabi.duit.data.CategoryEntity
import com.surabi.duit.data.FinanceDatabase
import com.surabi.duit.data.FinanceAlertEngine
import com.surabi.duit.data.FinanceAlertSeverity
import com.surabi.duit.data.FinanceRepository
import com.surabi.duit.data.GoalOverviewRow
import com.surabi.duit.data.LiabilityOverviewRow
import com.surabi.duit.data.ReceivableOverviewRow
import com.surabi.duit.data.TransactionEntity
import com.surabi.duit.data.TransactionSearchFilter
import com.surabi.duit.data.TransactionDateRange
import com.surabi.duit.data.TransactionCsvExporter
import com.surabi.duit.data.WorkspaceEntity
import com.surabi.duit.ui.DuitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(245, 248, 246)
        window.navigationBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val repository = FinanceRepository(FinanceDatabase.getInstance(applicationContext))
        setContent {
            DuitTheme {
                DuitApp(repository)
            }
        }
    }
}

private enum class AppTab(
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("Ringkas", Icons.Filled.Home),
    TRANSACTIONS("Transaksi", Icons.Filled.ReceiptLong),
    BUDGETS("Rencana", Icons.Filled.PieChart),
    ACCOUNTS("Aset", Icons.Filled.AccountBalanceWallet),
    GUIDE("Panduan", Icons.Filled.Info)
}

private data class TransactionExportRequest(
    val workspaceId: Long,
    val filter: TransactionSearchFilter,
    val accountNames: Map<Long, String>,
    val categoryNames: Map<Long, String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuitApp(repository: FinanceRepository) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("duit-ui-preferences", 0)
    }
    var amountsHidden by remember {
        mutableStateOf(preferences.getBoolean("amounts_hidden", false))
    }
    var dashboardWidgets by remember {
        mutableStateOf(
            DashboardWidgetSettings(
                cashflow = preferences.getBoolean("widget_cashflow", true),
                trend = preferences.getBoolean("widget_trend", true),
                accounts = preferences.getBoolean("widget_accounts", true),
                planning = preferences.getBoolean("widget_planning", true),
                alerts = preferences.getBoolean("widget_alerts", true),
                dueDates = preferences.getBoolean("widget_due_dates", true),
                activity = preferences.getBoolean("widget_activity", true)
            )
        )
    }
    var showDashboardWidgetSettings by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<TransactionExportRequest?>(null) }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val request = pendingExport
        pendingExport = null
        if (uri != null && request != null) {
            scope.launch {
                val writeResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("Lokasi penyimpanan tidak dapat dibuka.")
                        output.bufferedWriter(Charsets.UTF_8).use { writer ->
                            TransactionCsvExporter.writeHeader(writer)
                            var offset = 0
                            var exported = 0
                            while (true) {
                                val batch = repository.searchTransactionsPage(
                                    workspaceId = request.workspaceId,
                                    filter = request.filter,
                                    offset = offset,
                                    limit = 500
                                )
                                if (batch.isEmpty()) break
                                TransactionCsvExporter.writeRows(
                                    writer, batch, request.accountNames, request.categoryNames
                                )
                                offset += batch.size
                                exported += batch.size
                                if (batch.size < 500) break
                            }
                            exported
                        }
                    }
                }
                writeResult
                    .onSuccess { snackbar.showSnackbar("Ekspor CSV berhasil: " + it + " transaksi.") }
                    .onFailure { snackbar.showSnackbar(it.message ?: "Ekspor CSV gagal disimpan.") }
            }
        }
    }
    val workspaces by repository.workspaces.collectAsState(initial = emptyList())
    var selectedWorkspaceId by remember { mutableLongStateOf(0L) }
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showQuickActions by remember { mutableStateOf(false) }
    var dashboardPeriod by remember { mutableStateOf(DashboardPeriod.THIS_MONTH) }
    var databaseReady by remember { mutableStateOf(false) }
    var databaseError by remember { mutableStateOf<String?>(null) }
    var initializationAttempt by remember { mutableIntStateOf(0) }
    var showWorkspaceDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showAssetDialog by remember { mutableStateOf(false) }
    var showLiabilityDialog by remember { mutableStateOf(false) }
    var showReceivableDialog by remember { mutableStateOf(false) }
    var selectedGoalMovement by remember { mutableStateOf<Pair<GoalOverviewRow, Boolean>?>(null) }
    var selectedLiabilityPayment by remember { mutableStateOf<LiabilityOverviewRow?>(null) }
    var selectedReceivableCollection by remember { mutableStateOf<ReceivableOverviewRow?>(null) }
    var selectedAssetForValue by remember { mutableStateOf<AssetEntity?>(null) }
    var selectedAccountForMinimum by remember { mutableStateOf<AccountBalanceRow?>(null) }

    LaunchedEffect(initializationAttempt) {
        databaseReady = false
        databaseError = null
        runCatching { repository.ensureDefaults() }
            .onSuccess { databaseReady = true }
            .onFailure { databaseError = it.message ?: "Database lokal tidak dapat dibuka." }
    }
    LaunchedEffect(workspaces) {
        if (workspaces.isNotEmpty() && workspaces.none { it.id == selectedWorkspaceId }) {
            selectedWorkspaceId = workspaces.first().id
        }
    }

    val workspaceId = selectedWorkspaceId
    val selectedWorkspace = workspaces.firstOrNull { it.id == workspaceId }
    val accounts by remember(workspaceId) { repository.accounts(workspaceId) }.collectAsState(initial = emptyList())
    val categories by remember(workspaceId) { repository.categories(workspaceId) }.collectAsState(initial = emptyList())
    val transactions by remember(workspaceId) { repository.transactions(workspaceId) }.collectAsState(initial = emptyList())
    val activeTransactionCount by remember(workspaceId) {
        repository.transactionCount(workspaceId)
    }.collectAsState(initial = 0)
    val budgets by remember(workspaceId) { repository.budgets(workspaceId) }.collectAsState(initial = emptyList())
    val goals by remember(workspaceId) { repository.goals(workspaceId) }.collectAsState(initial = emptyList())
    val assets by remember(workspaceId) { repository.assets(workspaceId) }.collectAsState(initial = emptyList())
    val liabilities by remember(workspaceId) { repository.liabilities(workspaceId) }.collectAsState(initial = emptyList())
    val receivables by remember(workspaceId) { repository.receivables(workspaceId) }.collectAsState(initial = emptyList())

    val dashboardRange = remember(dashboardPeriod) { DashboardPeriodResolver.bounds(dashboardPeriod) }
    val income by remember(workspaceId, dashboardRange.startAt, dashboardRange.endAt) {
        repository.income(workspaceId, dashboardRange.startAt, dashboardRange.endAt)
    }.collectAsState(initial = 0L)
    val expenses by remember(workspaceId, dashboardRange.startAt, dashboardRange.endAt) {
        repository.expenses(workspaceId, dashboardRange.startAt, dashboardRange.endAt)
    }.collectAsState(initial = 0L)

    val trendRange = remember(workspaceId) { com.surabi.duit.data.DashboardTrendResolver.bounds() }
    val monthlyCashflow by remember(workspaceId, trendRange.startAt, trendRange.endAt) {
        repository.monthlyCashflow(workspaceId, trendRange.startAt, trendRange.endAt)
    }.collectAsState(initial = emptyList())
    val trendPoints = remember(monthlyCashflow) {
        com.surabi.duit.data.DashboardTrendResolver.points(monthlyCashflow)
    }
    val netWorth = accounts.sumOf { it.balanceMinor } +
        goals.sumOf { it.savedMinor } +
        assets.sumOf { it.currentValueMinor } +
        receivables.sumOf { it.outstandingMinor } -
        liabilities.sumOf { it.outstandingMinor }

    CompositionLocalProvider(LocalDuitAmountsHidden provides amountsHidden) {
    Scaffold(
        topBar = {
            DuitAppHeader(
                workspaces = workspaces,
                selectedId = workspaceId,
                onWorkspaceSelected = { selectedWorkspaceId = it },
                onAddWorkspace = { showWorkspaceDialog = true },
                amountsHidden = amountsHidden,
                onToggleAmountsHidden = {
                    amountsHidden = !amountsHidden
                    preferences.edit().putBoolean("amounts_hidden", amountsHidden).apply()
                }
            )
        },
        bottomBar = {
            DuitBottomDock(
                selectedIndex = AppTab.entries.indexOf(selectedTab),
                onSelect = { index -> AppTab.entries.getOrNull(index)?.let { selectedTab = it } }
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    onClick = { showQuickActions = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Tambah") }
                )
                DropdownMenu(expanded = showQuickActions, onDismissRequest = { showQuickActions = false }) {
                    DropdownMenuItem(
                        text = { Text("Catat transaksi") },
                        leadingIcon = { Icon(Icons.Filled.ReceiptLong, contentDescription = null) },
                        onClick = { showQuickActions = false; showTransactionDialog = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Tambah akun") },
                        leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                        onClick = { showQuickActions = false; showAccountDialog = true; selectedTab = AppTab.ACCOUNTS }
                    )
                    DropdownMenuItem(
                        text = { Text("Buat anggaran") },
                        leadingIcon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
                        onClick = { showQuickActions = false; showBudgetDialog = true; selectedTab = AppTab.BUDGETS }
                    )
                    DropdownMenuItem(
                        text = { Text("Buat target tabungan") },
                        leadingIcon = { Icon(Icons.Filled.Savings, contentDescription = null) },
                        onClick = { showQuickActions = false; showGoalDialog = true; selectedTab = AppTab.BUDGETS }
                    )
                    DropdownMenuItem(
                        text = { Text("Catat aset") },
                        leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        onClick = { showQuickActions = false; showAssetDialog = true; selectedTab = AppTab.ACCOUNTS }
                    )
                    DropdownMenuItem(
                        text = { Text("Catat piutang") },
                        onClick = { showQuickActions = false; showReceivableDialog = true; selectedTab = AppTab.ACCOUNTS }
                    )
                    DropdownMenuItem(
                        text = { Text("Catat utang") },
                        onClick = { showQuickActions = false; showLiabilityDialog = true; selectedTab = AppTab.ACCOUNTS }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                !databaseReady && databaseError == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator()
                            Text("Menyiapkan data keuangan lokal…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                databaseError != null -> {
                    DuitEmptyState(
                        title = "Data lokal tidak dapat dibuka",
                        message = databaseError ?: "Terjadi kesalahan saat menyiapkan database.",
                        icon = Icons.Filled.Info,
                        action = {
                            OutlinedButton(onClick = { initializationAttempt++ }) { Text("Coba lagi") }
                        }
                    )
                }
                selectedWorkspace == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> when (selectedTab) {
                AppTab.DASHBOARD -> DuitDashboardScreen(
                    workspaceName = selectedWorkspace.name,
                    accounts = accounts,
                    transactions = transactions,
                    budgets = budgets,
                    goals = goals,
                    income = income,
                    expenses = expenses,
                    netWorth = netWorth,
                    liabilities = liabilities,
                    receivables = receivables,
                    trend = trendPoints,
                    period = dashboardPeriod,
                    onPeriodChange = { dashboardPeriod = it },
                    onAddTransaction = { showTransactionDialog = true },
                    onAddGoal = { showGoalDialog = true },
                    onOpenPlanning = { selectedTab = AppTab.BUDGETS },
                    onOpenWealth = { selectedTab = AppTab.ACCOUNTS },
                    onOpenTransactions = { selectedTab = AppTab.TRANSACTIONS },
                    widgets = dashboardWidgets,
                    onConfigureWidgets = { showDashboardWidgetSettings = true }
                )
                AppTab.TRANSACTIONS -> TransactionsScreen(
                    workspaceId = workspaceId,
                    refreshKey = activeTransactionCount,
                    repository = repository,
                    hasTransactions = activeTransactionCount > 0,
                    accounts = accounts,
                    categories = categories,
                    onAdd = { showTransactionDialog = true },
                    onExport = { filter ->
                        pendingExport = TransactionExportRequest(
                            workspaceId = workspaceId,
                            filter = filter,
                            accountNames = accounts.associate { it.id to it.name },
                            categoryNames = categories.associate { it.id to it.name }
                        )
                        val fileStamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                        csvExportLauncher.launch("Duit-Transaksi-$fileStamp.csv")
                    },
                    onVoid = { id, reason ->
                        scope.launch {
                            runCatching { repository.voidTransaction(workspaceId, id, reason) }
                                .onSuccess { snackbar.showSnackbar("Transaksi dibatalkan dan tercatat pada audit trail.") }
                                .onFailure { snackbar.showSnackbar(it.message ?: "Pembatalan gagal.") }
                        }
                    }
                )
                AppTab.BUDGETS -> PlanningScreen(
                    budgets = budgets,
                    goals = goals,
                    onAddBudget = { showBudgetDialog = true },
                    onAddGoal = { showGoalDialog = true },
                    onMoveGoal = { goal, isWithdrawal -> selectedGoalMovement = goal to isWithdrawal }
                )
                AppTab.ACCOUNTS -> WealthScreen(
                    accounts = accounts,
                    goals = goals,
                    assets = assets,
                    receivables = receivables,
                    liabilities = liabilities,
                    onAddAccount = { showAccountDialog = true },
                    onSetAccountMinimumBalance = { selectedAccountForMinimum = it },
                    onAddAsset = { showAssetDialog = true },
                    onAddReceivable = { showReceivableDialog = true },
                    onCollectReceivable = { selectedReceivableCollection = it },
                    onAddLiability = { showLiabilityDialog = true },
                    onPayLiability = { selectedLiabilityPayment = it },
                    onUpdateAsset = { selectedAssetForValue = it }
                )
                AppTab.GUIDE -> GuideScreen(onNavigate = { selectedTab = it })
                }
            }
        }
    }

    if (showWorkspaceDialog) {
        WorkspaceDialog(
            onDismiss = { showWorkspaceDialog = false },
            onSave = { name, type ->
                scope.launch {
                    runCatching { repository.createWorkspace(name, type) }
                        .onSuccess {
                            selectedWorkspaceId = it
                            showWorkspaceDialog = false
                            snackbar.showSnackbar("Workspace berhasil dibuat.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Workspace gagal dibuat.") }
                }
            }
        )
    }

    if (showTransactionDialog) {
        TransactionDialog(
            accounts = accounts,
            categories = categories,
            onDismiss = { showTransactionDialog = false },
            onSave = { type, amount, title, fromId, toId, categoryId, note ->
                scope.launch {
                    runCatching {
                        repository.createTransaction(workspaceId, type, amount, title, fromId, toId, categoryId, note)
                    }.onSuccess {
                        showTransactionDialog = false
                        snackbar.showSnackbar("Transaksi berhasil disimpan.")
                    }.onFailure { snackbar.showSnackbar(it.message ?: "Transaksi gagal disimpan.") }
                }
            }
        )
    }

    if (showAccountDialog) {
        AccountDialog(
            onDismiss = { showAccountDialog = false },
            onSave = { name, type, opening ->
                scope.launch {
                    runCatching { repository.createAccount(workspaceId, name, type, opening) }
                        .onSuccess {
                            showAccountDialog = false
                            snackbar.showSnackbar("Akun berhasil ditambahkan.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Akun gagal ditambahkan.") }
                }
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            categories = categories.filter { it.kind == "EXPENSE" },
            onDismiss = { showBudgetDialog = false },
            onSave = { name, limit, categoryId ->
                scope.launch {
                    runCatching { repository.createMonthlyBudget(workspaceId, name, limit, categoryId) }
                        .onSuccess {
                            showBudgetDialog = false
                            snackbar.showSnackbar("Anggaran bulanan berhasil dibuat.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Anggaran gagal dibuat.") }
                }
            }
        )
    }

    if (showGoalDialog) {
        SavingsGoalDialog(
            onDismiss = { showGoalDialog = false },
            onSave = { name, target, targetDate, priority ->
                scope.launch {
                    runCatching { repository.createSavingsGoal(workspaceId, name, target, targetDate, priority) }
                        .onSuccess {
                            showGoalDialog = false
                            snackbar.showSnackbar("Target tabungan berhasil dibuat.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Target gagal dibuat.") }
                }
            }
        )
    }

    selectedGoalMovement?.let { (goal, isWithdrawal) ->
        GoalMovementDialog(
            goals = goals,
            accounts = accounts,
            initialGoalId = goal.id,
            isWithdrawal = isWithdrawal,
            onDismiss = { selectedGoalMovement = null },
            onSave = { goalId, accountId, amount, note ->
                scope.launch {
                    runCatching { repository.moveGoalFunds(workspaceId, goalId, accountId, amount, isWithdrawal, note) }
                        .onSuccess {
                            selectedGoalMovement = null
                            snackbar.showSnackbar(if (isWithdrawal) "Dana target berhasil ditarik." else "Setoran target berhasil dicatat.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Perubahan target gagal.") }
                }
            }
        )
    }

    if (showAssetDialog) {
        AssetDialog(
            onDismiss = { showAssetDialog = false },
            onSave = { name, category, cost, value, notes ->
                scope.launch {
                    runCatching { repository.createAsset(workspaceId, name, category, cost, value, notes) }
                        .onSuccess {
                            showAssetDialog = false
                            snackbar.showSnackbar("Aset berhasil dicatat.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Aset gagal disimpan.") }
                }
            }
        )
    }

    if (showLiabilityDialog) {
        LiabilityDialog(
            accounts = accounts,
            onDismiss = { showLiabilityDialog = false },
            onSave = { name, counterparty, amount, dueAt, receivingAccount, notes ->
                scope.launch {
                    runCatching {
                        repository.createLiability(workspaceId, name, counterparty, amount, dueAt, receivingAccount, notes)
                    }.onSuccess {
                        showLiabilityDialog = false
                        snackbar.showSnackbar("Utang berhasil dicatat.")
                    }.onFailure { snackbar.showSnackbar(it.message ?: "Utang gagal dicatat.") }
                }
            }
        )
    }

    if (showReceivableDialog) {
        ReceivableDialog(
            onDismiss = { showReceivableDialog = false },
            onSave = { name, counterparty, amount, dueAt, notes ->
                scope.launch {
                    runCatching {
                        repository.createReceivable(workspaceId, name, counterparty, amount, dueAt, notes)
                    }.onSuccess {
                        showReceivableDialog = false
                        snackbar.showSnackbar("Piutang berhasil dicatat.")
                    }.onFailure { snackbar.showSnackbar(it.message ?: "Piutang gagal dicatat.") }
                }
            }
        )
    }

    selectedReceivableCollection?.let { receivable ->
        ReceivableCollectionDialog(
            receivable = receivable,
            accounts = accounts,
            onDismiss = { selectedReceivableCollection = null },
            onSave = { accountId, amount, note ->
                scope.launch {
                    runCatching {
                        repository.collectReceivable(workspaceId, receivable.id, accountId, amount, note)
                    }.onSuccess {
                        selectedReceivableCollection = null
                        snackbar.showSnackbar("Penerimaan piutang berhasil dicatat dan saldo akun diperbarui.")
                    }.onFailure { snackbar.showSnackbar(it.message ?: "Penerimaan piutang gagal.") }
                }
            }
        )
    }

    selectedAccountForMinimum?.let { account ->
        AccountMinimumBalanceDialog(
            account = account,
            onDismiss = { selectedAccountForMinimum = null },
            onSave = { minimumBalance ->
                scope.launch {
                    runCatching {
                        repository.setAccountMinimumBalance(account.workspaceId, account.id, minimumBalance)
                    }.onSuccess {
                        selectedAccountForMinimum = null
                        snackbar.showSnackbar(
                            if (minimumBalance == 0L) {
                                "Peringatan batas saldo minimum dinonaktifkan."
                            } else {
                                "Batas saldo minimum akun berhasil diperbarui."
                            }
                        )
                    }.onFailure {
                        snackbar.showSnackbar(it.message ?: "Batas saldo minimum gagal diperbarui.")
                    }
                }
            }
        )
    }

    selectedAssetForValue?.let { asset ->
        AssetValueDialog(
            asset = asset,
            onDismiss = { selectedAssetForValue = null },
            onSave = { currentValue ->
                scope.launch {
                    runCatching { repository.updateAssetValue(workspaceId, asset.id, currentValue) }
                        .onSuccess {
                            selectedAssetForValue = null
                            snackbar.showSnackbar("Nilai aset berhasil diperbarui.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Pembaruan nilai aset gagal.") }
                }
            }
        )
    }

    selectedLiabilityPayment?.let { liability ->
        LiabilityPaymentDialog(
            liability = liability,
            accounts = accounts,
            onDismiss = { selectedLiabilityPayment = null },
            onSave = { accountId, amount, note ->
                scope.launch {
                    runCatching { repository.payLiability(workspaceId, liability.id, accountId, amount, note) }
                        .onSuccess {
                            selectedLiabilityPayment = null
                            snackbar.showSnackbar("Pembayaran utang berhasil dicatat.")
                        }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Pembayaran utang gagal.") }
                }
            }
        )
    }
    if (showDashboardWidgetSettings) {
        DashboardWidgetSettingsDialog(
            initial = dashboardWidgets,
            onDismiss = { showDashboardWidgetSettings = false },
            onSave = { updated ->
                dashboardWidgets = updated
                preferences.edit()
                    .putBoolean("widget_cashflow", updated.cashflow)
                    .putBoolean("widget_trend", updated.trend)
                    .putBoolean("widget_accounts", updated.accounts)
                    .putBoolean("widget_planning", updated.planning)
                    .putBoolean("widget_alerts", updated.alerts)
                    .putBoolean("widget_due_dates", updated.dueDates)
                    .putBoolean("widget_activity", updated.activity)
                    .apply()
                showDashboardWidgetSettings = false
                scope.launch { snackbar.showSnackbar("Susunan dashboard disimpan di perangkat.") }
            }
        )
    }
    }
}

@Composable
private fun WorkspacePicker(
    workspaces: List<WorkspaceEntity>,
    selectedId: Long,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = workspaces.firstOrNull { it.id == selectedId }
    Box {
        FilledTonalButton(onClick = { expanded = true }) {
            Text(current?.name ?: "Pilih", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 108.dp))
            Icon(Icons.Filled.ExpandMore, contentDescription = "Buka daftar workspace")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            workspaces.forEach { workspace ->
                DropdownMenuItem(
                    text = { Text(workspace.name) },
                    onClick = {
                        onSelected(workspace.id)
                        expanded = false
                    }
                )
            }
            if (workspaces.isEmpty()) {
                DropdownMenuItem(text = { Text("Menyiapkan workspace…") }, onClick = { expanded = false })
            }
        }
    }
}

@Composable
private fun DashboardPeriodPicker(
    period: DashboardPeriod,
    onSelected: (DashboardPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DashboardPeriod.entries.forEach { option ->
            val label = when (option) {
                DashboardPeriod.THIS_MONTH -> "Bulan ini"
                DashboardPeriod.LAST_30_DAYS -> "30 hari"
                DashboardPeriod.THIS_YEAR -> "Tahun ini"
            }
            FilterChip(
                selected = period == option,
                onClick = { onSelected(option) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun TransactionsScreen(
    workspaceId: Long,
    refreshKey: Int,
    repository: FinanceRepository,
    hasTransactions: Boolean,
    accounts: List<AccountBalanceRow>,
    categories: List<CategoryEntity>,
    onAdd: () -> Unit,
    onExport: (TransactionSearchFilter) -> Unit,
    onVoid: (Long, String) -> Unit
) {
    val accountNames = remember(accounts) { accounts.associate { it.id to it.name } }
    val categoryNames = remember(categories) { categories.associate { it.id to it.name } }
    val scope = rememberCoroutineScope()
    var searchQuery by remember(workspaceId) { mutableStateOf("") }
    var appliedSearchQuery by remember(workspaceId) { mutableStateOf("") }
    var typeFilter by remember(workspaceId) { mutableStateOf("ALL") }
    var showAdvancedFilters by remember(workspaceId) { mutableStateOf(false) }
    var startDateText by remember(workspaceId) { mutableStateOf("") }
    var endDateText by remember(workspaceId) { mutableStateOf("") }
    var minAmountText by remember(workspaceId) { mutableStateOf("") }
    var maxAmountText by remember(workspaceId) { mutableStateOf("") }
    var accountFilterId by remember(workspaceId) { mutableLongStateOf(0L) }
    var categoryFilterId by remember(workspaceId) { mutableLongStateOf(0L) }
    var sortOrder by remember(workspaceId) { mutableStateOf("NEWEST") }
    var selectedToVoid by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedDetails by remember { mutableStateOf<TransactionEntity?>(null) }
    var voidReason by remember { mutableStateOf("") }
    var visible by remember(workspaceId) { mutableStateOf(emptyList<TransactionEntity>()) }
    var totalCount by remember(workspaceId) { mutableIntStateOf(0) }
    var isInitialLoading by remember(workspaceId) { mutableStateOf(true) }
    var isLoadingMore by remember(workspaceId) { mutableStateOf(false) }
    var pageError by remember(workspaceId) { mutableStateOf<String?>(null) }
    var retryToken by remember(workspaceId) { mutableIntStateOf(0) }
    var paginationJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(workspaceId, searchQuery) {
        delay(250)
        appliedSearchQuery = searchQuery
    }

    val dateBounds = remember(startDateText, endDateText) {
        TransactionDateRange.parse(startDateText, endDateText)
    }
    val minAmount = minAmountText.takeIf { it.isNotBlank() }?.toLongOrNull()
    val maxAmount = maxAmountText.takeIf { it.isNotBlank() }?.toLongOrNull()
    val filterError = when {
        dateBounds == null -> "Tanggal tidak valid atau tanggal awal melebihi tanggal akhir. Gunakan format dd-MM-yyyy."
        minAmountText.isNotBlank() && minAmount == null -> "Nominal minimum terlalu besar."
        maxAmountText.isNotBlank() && maxAmount == null -> "Nominal maksimum terlalu besar."
        minAmount != null && maxAmount != null && minAmount > maxAmount ->
            "Nominal minimum tidak boleh melebihi nominal maksimum."
        else -> null
    }
    val searchFilter = remember(
        appliedSearchQuery, typeFilter, dateBounds, accountFilterId, categoryFilterId,
        minAmount, maxAmount, sortOrder
    ) {
        TransactionSearchFilter(
            query = appliedSearchQuery,
            type = typeFilter,
            startAt = dateBounds?.startAt ?: Long.MIN_VALUE,
            endAt = dateBounds?.endAt ?: Long.MAX_VALUE,
            accountId = accountFilterId.takeIf { it != 0L },
            categoryId = categoryFilterId.takeIf { it != 0L },
            minAmount = minAmount,
            maxAmount = maxAmount,
            sortOrder = sortOrder
        )
    }
    val pageSize = 50

    LaunchedEffect(workspaceId, refreshKey, searchFilter, filterError, retryToken) {
        paginationJob?.cancel()
        visible = emptyList()
        totalCount = 0
        pageError = null
        isLoadingMore = false
        if (filterError == null) {
            isInitialLoading = true
            runCatching {
                val count = repository.countTransactions(workspaceId, searchFilter)
                val firstPage = repository.searchTransactionsPage(
                    workspaceId = workspaceId,
                    filter = searchFilter,
                    offset = 0,
                    limit = pageSize
                )
                count to firstPage
            }.onSuccess { result ->
                totalCount = result.first
                visible = result.second
            }.onFailure {
                pageError = it.message ?: "Riwayat transaksi tidak dapat dimuat."
            }
            isInitialLoading = false
        } else {
            isInitialLoading = false
        }
    }

    val groupedTransactions = remember(visible, sortOrder) {
        if (sortOrder == "AMOUNT_ASC" || sortOrder == "AMOUNT_DESC") {
            mapOf(Long.MIN_VALUE to visible)
        } else {
            visible.groupBy { transactionDayStart(it.occurredAt) }
        }
    }
    val activeFilterCount = listOf(
        typeFilter != "ALL",
        startDateText.isNotBlank(),
        endDateText.isNotBlank(),
        minAmountText.isNotBlank(),
        maxAmountText.isNotBlank(),
        accountFilterId != 0L,
        categoryFilterId != 0L,
        sortOrder != "NEWEST"
    ).count { it }

    val loadMore: () -> Unit = {
        if (!isLoadingMore && visible.size < totalCount && filterError == null) {
            val requestedOffset = visible.size
            paginationJob?.cancel()
            paginationJob = scope.launch {
                isLoadingMore = true
                pageError = null
                runCatching {
                    repository.searchTransactionsPage(
                        workspaceId = workspaceId,
                        filter = searchFilter,
                        offset = requestedOffset,
                        limit = pageSize
                    )
                }.onSuccess { page ->
                    visible = visible + page
                    if (page.isEmpty()) totalCount = visible.size
                }.onFailure {
                    pageError = it.message ?: "Halaman berikutnya gagal dimuat."
                }
                isLoadingMore = false
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF3F7F4))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("RIWAYAT KEUANGAN", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
            Text("Transaksi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Telusuri, saring, dan ekspor histori transaksi Anda.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onExport(searchFilter) },
                enabled = totalCount > 0 && filterError == null && !isInitialLoading,
                modifier = Modifier.weight(1f)
            ) { Text("Ekspor semua hasil") }
            Button(onClick = onAdd, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Tambah transaksi")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.take(100) },
            label = { Text("Cari keterangan, catatan, atau kategori") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pencarian transaksi") },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Hapus pencarian")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            listOf(
                "ALL" to "Semua",
                "INCOME" to "Masuk",
                "EXPENSE" to "Keluar",
                "TRANSFER" to "Transfer",
                "OTHER" to "Lainnya"
            ).forEach { (key, label) ->
                FilterChip(selected = typeFilter == key, onClick = { typeFilter = key }, label = { Text(label) })
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = { showAdvancedFilters = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ExpandMore, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text(if (activeFilterCount == 0) "Filter dan urutkan" else "Filter (" + activeFilterCount + ")")
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (isInitialLoading) "Memuat…" else totalCount.toString() + " cocok",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showAdvancedFilters) {
            AlertDialog(
                onDismissRequest = { showAdvancedFilters = false },
                title = { Text("Filter transaksi") },
                text = {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text("Rentang tanggal menggunakan kalender lokal perangkat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = startDateText,
                            onValueChange = { startDateText = it.take(10) },
                            label = { Text("Dari (dd-MM-yyyy)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = endDateText,
                            onValueChange = { endDateText = it.take(10) },
                            label = { Text("Sampai (dd-MM-yyyy)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = minAmountText,
                            onValueChange = { minAmountText = it.filter(Char::isDigit) },
                            label = { Text("Nominal minimum (IDR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = maxAmountText,
                            onValueChange = { maxAmountText = it.filter(Char::isDigit) },
                            label = { Text("Nominal maksimum (IDR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        IdDropdown(
                            label = "Akun",
                            selectedId = accountFilterId,
                            options = listOf(0L to "Semua akun") + accounts.map { it.id to it.name },
                            onSelected = { accountFilterId = it }
                        )
                        IdDropdown(
                            label = "Kategori",
                            selectedId = categoryFilterId,
                            options = listOf(0L to "Semua kategori") + categories.map { it.id to it.name },
                            onSelected = { categoryFilterId = it }
                        )
                        ChoiceDropdown(
                            label = "Urutkan",
                            selected = sortOrder,
                            options = listOf(
                                "NEWEST" to "Tanggal terbaru",
                                "OLDEST" to "Tanggal terlama",
                                "AMOUNT_ASC" to "Nominal terkecil",
                                "AMOUNT_DESC" to "Nominal terbesar"
                            ),
                            onSelected = { sortOrder = it }
                        )
                        if (filterError != null) {
                            Text(filterError, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAdvancedFilters = false }) { Text("Terapkan") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        startDateText = ""
                        endDateText = ""
                        minAmountText = ""
                        maxAmountText = ""
                        accountFilterId = 0L
                        categoryFilterId = 0L
                        sortOrder = "NEWEST"
                    }) { Text("Atur ulang") }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "transaction-list-summary") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Riwayat tersaring", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Text(visible.size.toString() + " dari " + totalCount + " dimuat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (filterError != null) {
                item(key = "filter-error") {
                    DuitEmptyState(title = "Filter belum valid", message = filterError, icon = Icons.Filled.Info)
                }
            } else if (isInitialLoading) {
                item(key = "transaction-loading") {
                    Column(Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text("Memuat riwayat dari penyimpanan lokal…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (pageError != null && visible.isEmpty()) {
                item(key = "transaction-load-error") {
                    DuitEmptyState(
                        title = "Riwayat gagal dimuat",
                        message = pageError ?: "Terjadi masalah saat membaca data lokal.",
                        icon = Icons.Filled.Info,
                        action = { Button(onClick = { retryToken++ }) { Text("Coba lagi") } }
                    )
                }
            } else if (visible.isEmpty()) {
                item(key = "transaction-empty") {
                    if (!hasTransactions) {
                        DuitEmptyState(
                            title = "Belum ada transaksi",
                            message = "Catat pemasukan, pengeluaran, atau transfer pertama Anda.",
                            icon = Icons.Filled.ReceiptLong,
                            action = { Button(onClick = onAdd) { Text("Catat transaksi") } }
                        )
                    } else {
                        DuitEmptyState(
                            title = "Tidak ada hasil yang cocok",
                            message = "Ubah kata pencarian atau filter untuk menemukan transaksi lain.",
                            icon = Icons.Filled.Search
                        )
                    }
                }
            } else {
                item(key = "transaction-group-hint") {
                    Text(
                        if (sortOrder == "AMOUNT_ASC" || sortOrder == "AMOUNT_DESC")
                            "Diurutkan berdasarkan nominal" else "Dikelompokkan menurut tanggal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                groupedTransactions.forEach { (day, rows) ->
                    item(key = "transaction-day-" + day) {
                        Text(
                            if (day == Long.MIN_VALUE) "Urutan nominal" else transactionDayLabel(day),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 7.dp, bottom = 1.dp)
                        )
                    }
                    items(rows, key = { it.id }) { item ->
                        DuitTransactionTile(
                            item = item,
                            accountName = accountNames[item.fromAccountId],
                            destinationName = item.toAccountId?.let { accountNames[it] },
                            categoryName = item.categoryId?.let { categoryNames[it] },
                            onClick = { selectedDetails = item },
                            onVoid = { selectedToVoid = item }
                        )
                    }
                }
                item(key = "transaction-page-footer") {
                    when {
                        pageError != null -> {
                            Column(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(pageError ?: "Gagal memuat halaman berikutnya.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = loadMore) { Text("Ulangi pemuatan") }
                            }
                        }
                        isLoadingMore -> {
                            Row(Modifier.fillMaxWidth().padding(18.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Memuat transaksi berikutnya…")
                            }
                        }
                        visible.size < totalCount -> {
                            OutlinedButton(onClick = loadMore, modifier = Modifier.fillMaxWidth()) {
                                Text("Muat 50 transaksi berikutnya")
                            }
                        }
                        else -> {
                            Text(
                                "Semua " + totalCount + " transaksi yang cocok sudah ditampilkan.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    selectedDetails?.let { item ->
        TransactionDetailsDialog(
            item = item,
            accountName = accountNames[item.fromAccountId],
            destinationName = item.toAccountId?.let { accountNames[it] },
            categoryName = item.categoryId?.let { categoryNames[it] },
            onDismiss = { selectedDetails = null },
            onVoid = {
                selectedDetails = null
                selectedToVoid = item
            }
        )
    }

    selectedToVoid?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedToVoid = null; voidReason = "" },
            title = { Text("Batalkan transaksi?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transaksi \"" + item.title + "\" akan dikecualikan dari saldo dan laporan. Catatan pembatalan disimpan di audit trail.")
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it.take(300) },
                        label = { Text("Alasan pembatalan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = voidReason.isNotBlank(), onClick = {
                    onVoid(item.id, voidReason)
                    selectedToVoid = null
                    voidReason = ""
                }) { Text("Batalkan transaksi") }
            },
            dismissButton = { TextButton(onClick = { selectedToVoid = null; voidReason = "" }) { Text("Kembali") } }
        )
    }
}

@Composable
private fun TransactionCard(
    item: TransactionEntity,
    accountName: String? = null,
    destinationName: String? = null,
    categoryName: String? = null,
    onClick: (() -> Unit)? = null,
    onVoid: (() -> Unit)? = null
) {
    val isCredit = item.type in setOf("INCOME", "GOAL_WITHDRAWAL", "LIABILITY_RECEIVED", "RECEIVABLE_COLLECTION")
    val isDebit = item.type in setOf("EXPENSE", "GOAL_CONTRIBUTION", "LIABILITY_PAYMENT")
    val isTransfer = item.type == "TRANSFER"
    val accent = when {
        isCredit -> MaterialTheme.colorScheme.primary
        isDebit -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(42.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isTransfer) Icons.Filled.SwapHoriz else if (isCredit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = transactionTypeLabel(item.type),
                    tint = accent
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, fontWeight = FontWeight.SemiBold)
                Text(
                    listOfNotNull(
                        categoryName,
                        accountName,
                        if (isTransfer) "→ " + (destinationName ?: "akun tujuan") else null
                    ).joinToString(" • ").ifBlank { transactionTypeLabel(item.type) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date(item.occurredAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (isCredit) "+" else if (isTransfer) "↔ " else "− ") + formatRupiah(item.amountMinor),
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (onVoid != null) {
                    IconButton(onClick = onVoid) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Batalkan transaksi", tint = MaterialTheme.colorScheme.error)
                    }
                } else if (onClick != null) {
                    Text("Detail", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailsDialog(
    item: TransactionEntity,
    accountName: String?,
    destinationName: String?,
    categoryName: String?,
    onDismiss: () -> Unit,
    onVoid: () -> Unit
) {
    val dateText = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm", Locale("id", "ID")).format(Date(item.occurredAt))
    val credit = item.type in setOf("INCOME", "GOAL_WITHDRAWAL", "LIABILITY_RECEIVED", "RECEIVABLE_COLLECTION")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail transaksi") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    (if (credit) "+ " else if (item.type == "TRANSFER") "↔ " else "− ") + formatRupiah(item.amountMinor),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (credit) MaterialTheme.colorScheme.primary else if (item.type == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
                HorizontalDivider()
                TransactionDetailLine("Jenis", transactionTypeLabel(item.type))
                TransactionDetailLine("Tanggal", dateText)
                TransactionDetailLine("Akun sumber", accountName ?: "Tidak tersedia")
                if (item.toAccountId != null) TransactionDetailLine("Akun tujuan", destinationName ?: "Tidak tersedia")
                if (categoryName != null) TransactionDetailLine("Kategori", categoryName)
                TransactionDetailLine("ID transaksi", item.id.toString())
                if (item.note.isNotBlank()) {
                    Text("Catatan", fontWeight = FontWeight.SemiBold)
                    Text(item.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Riwayat transaksi tidak dihapus. Pembatalan menggunakan status void dan dicatat pada audit trail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
        dismissButton = {
            TextButton(onClick = onVoid) { Text("Batalkan transaksi", color = MaterialTheme.colorScheme.error) }
        }
    )
}

@Composable
private fun TransactionDetailLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.85f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.15f))
    }
}


@Composable
private fun AccountsScreen(accounts: List<AccountBalanceRow>, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Dompet dan rekening", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Saldo dihitung dari saldo awal dan histori transaksi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Tambah")
            }
        }
        if (accounts.isEmpty()) {
            EmptyHint("Belum ada akun", "Tambahkan dompet atau rekening untuk mulai mencatat transaksi.")
        } else {
            accounts.forEach { account ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(account.name, fontWeight = FontWeight.SemiBold)
                            Text(accountTypeLabel(account.type), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatRupiah(account.balanceMinor), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetsScreen(budgets: List<BudgetOverviewRow>, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Anggaran", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Batas pengeluaran bulan berjalan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Buat")
            }
        }
        if (budgets.isEmpty()) {
            EmptyHint("Belum ada anggaran", "Buat anggaran untuk memantau batas dan realisasi pengeluaran.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(budgets, key = { it.id }) { BudgetProgressCard(it) }
            }
        }
    }
}

@Composable
private fun BudgetProgressCard(budget: BudgetOverviewRow) {
    val ratio = if (budget.limitMinor > 0) budget.spentMinor.toFloat() / budget.limitMinor.toFloat() else 0f
    val remaining = budget.limitMinor - budget.spentMinor
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(budget.name, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatRupiah(budget.spentMinor), fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("dari " + formatRupiah(budget.limitMinor), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (ratio >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                if (remaining >= 0) "Sisa " + formatRupiah(remaining) else "Melebihi batas " + formatRupiah(-remaining),
                color = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private data class GuideEntry(
    val category: String,
    val title: String,
    val body: String,
    val destination: AppTab,
    val action: String
)

@Composable
private fun GuideScreen(onNavigate: (AppTab) -> Unit) {
    var query by remember { mutableStateOf("") }
    val entries = remember {
        listOf(
            GuideEntry("Mulai", "Pisahkan ruang keuangan",
                "Buat workspace Pribadi, Keluarga, Bisnis, Perusahaan, atau Proyek. Transaksi, akun, dan anggaran disimpan per workspace.",
                AppTab.DASHBOARD, "Buka Ringkasan"),
            GuideEntry("Akun", "Tambahkan dompet dan rekening",
                "Tambahkan saldo awal akun. Saldo berikutnya dihitung dari transaksi masuk, keluar, dan transfer yang sudah tersimpan.",
                AppTab.ACCOUNTS, "Buka Aset"),
            GuideEntry("Transaksi", "Catat pemasukan dan pengeluaran",
                "Pilih jenis transaksi, nominal, keterangan, akun, serta kategori yang sesuai. Simpan setelah memeriksa rincian.",
                AppTab.TRANSACTIONS, "Buka Transaksi"),
            GuideEntry("Transaksi", "Pahami transfer antar-akun",
                "Transfer mengurangi akun sumber dan menambah akun tujuan. Transfer tidak dianggap pendapatan atau biaya.",
                AppTab.TRANSACTIONS, "Buka Transaksi"),
            GuideEntry("Transaksi", "Cari dan ekspor transaksi",
                "Gunakan pencarian dan filter tanggal, akun, kategori, nominal, serta jenis. CSV mengekspor hasil yang sedang tampil, maksimal 500 transaksi; bukan backup database.",
                AppTab.TRANSACTIONS, "Buka Transaksi"),
            GuideEntry("Perencanaan", "Buat anggaran",
                "Anggaran membandingkan batas dengan transaksi pengeluaran yang cocok. Realisasi dan sisa dihitung dari transaksi tersimpan.",
                AppTab.BUDGETS, "Buka Rencana"),
            GuideEntry("Perencanaan", "Kelola target tabungan",
                "Setoran memindahkan saldo dari akun ke dana target. Penarikan mengembalikannya. Progres diturunkan dari riwayat pergerakan dana.",
                AppTab.BUDGETS, "Buka Rencana"),
            GuideEntry("Aset", "Catat nilai aset non-tunai",
                "Catat kendaraan, properti, tanah, emas, elektronik, atau peralatan. Jangan catat ulang saldo rekening/dompet di modul aset.",
                AppTab.ACCOUNTS, "Buka Aset"),
            GuideEntry("Aset", "Kelola utang dan piutang",
                "Pembayaran utang mengurangi saldo akun dan kewajiban. Piutang menambah saldo akun hanya ketika penerimaan benar-benar dicatat.",
                AppTab.ACCOUNTS, "Buka Aset"),
            GuideEntry("Keamanan", "Penyimpanan offline dan cadangan",
                "Data inti disimpan secara lokal dengan Room/SQLite. Backup dan restore database penuh terenkripsi belum tersedia. Ekspor CSV bukan pengganti backup.",
                AppTab.GUIDE, "Tetap di Panduan")
        )
    }
    val filtered = remember(query, entries) {
        val term = query.trim()
        if (term.isEmpty()) entries else entries.filter {
            it.title.contains(term, ignoreCase = true) ||
                it.body.contains(term, ignoreCase = true) ||
                it.category.contains(term, ignoreCase = true)
        }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF3F7F4)).verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        DuitPageHeader(
            title = "Pusat bantuan",
            subtitle = "Belajar mengelola uang dengan alur yang jelas"
        )
        DuitFeatureBanner(
            kicker = "DUIT HELP CENTER",
            value = "Temukan cara menggunakan setiap fitur",
            supporting = "Panduan, istilah, dan jawaban berdasarkan kemampuan aplikasi saat ini"
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it.take(100) },
            label = { Text("Cari topik, contoh, atau istilah") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari panduan") },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Hapus pencarian")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (filtered.isEmpty()) {
            DuitEmptyState(
                title = "Panduan tidak ditemukan",
                message = "Coba kata lain seperti transaksi, anggaran, target, akun, utang, atau CSV.",
                icon = Icons.Filled.Search
            )
        } else {
            filtered.groupBy { it.category }.forEach { (category, topics) ->
                DuitSectionHeader(category, "${topics.size} topik bantuan")
                topics.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(entry.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(entry.body, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onNavigate(entry.destination) }) {
                            Text("Buka", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        DuitSectionHeader("Pertanyaan yang sering muncul")
        GuideCard(
            "Mengapa saldo berbeda dari pemasukan dikurangi pengeluaran?",
            "Saldo akun juga dipengaruhi saldo awal, transfer, setoran/penarikan target, pencairan pinjaman, pembayaran utang, dan penerimaan piutang. Beberapa aliran tersebut bukan pendapatan atau biaya."
        )
        GuideCard(
            "Apakah ekspor CSV mencadangkan seluruh aplikasi?",
            "Tidak. CSV hanya menyalin hasil transaksi yang sedang tampil, maksimal 500 baris. Cadangan penuh yang mencakup database dan lampiran belum tersedia."
        )
        GuideCard(
            "Apakah semua fitur enterprise sudah tersedia?",
            "Belum. Akuntansi double-entry, buku besar, rekonsiliasi, impor spreadsheet, ERP, inventaris, OCR, enkripsi database, multi-user, dan backup/restore masih menjadi pekerjaan lanjutan."
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GuideCard(title: String, body: String) {
    DuitPanel {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyHint(title: String, body: String) {
    DuitEmptyState(title = title, message = body)
}

@Composable
private fun DashboardWidgetSettingsDialog(
    initial: DashboardWidgetSettings,
    onDismiss: () -> Unit,
    onSave: (DashboardWidgetSettings) -> Unit
) {
    var draft by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur dashboard") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 430.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "Pilih ringkasan yang ingin ditampilkan. Pengaturan hanya tersimpan di perangkat ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DashboardWidgetToggle("Pemasukan dan pengeluaran", draft.cashflow) {
                    draft = draft.copy(cashflow = it)
                }
                DashboardWidgetToggle("Grafik tren arus kas", draft.trend) {
                    draft = draft.copy(trend = it)
                }
                DashboardWidgetToggle("Dompet dan rekening", draft.accounts) {
                    draft = draft.copy(accounts = it)
                }
                DashboardWidgetToggle("Anggaran dan target tabungan", draft.planning) {
                    draft = draft.copy(planning = it)
                }
                DashboardWidgetToggle("Peringatan keuangan", draft.alerts) {
                    draft = draft.copy(alerts = it)
                }
                DashboardWidgetToggle("Jatuh tempo", draft.dueDates) {
                    draft = draft.copy(dueDates = it)
                }
                DashboardWidgetToggle("Aktivitas terbaru", draft.activity) {
                    draft = draft.copy(activity = it)
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(draft) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun DashboardWidgetToggle(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Checkbox(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun WorkspaceDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PERSONAL") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workspace baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama workspace") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ChoiceDropdown(
                    label = "Jenis",
                    selected = type,
                    options = listOf(
                        "PERSONAL" to "Pribadi",
                        "FAMILY" to "Keluarga",
                        "BUSINESS" to "Bisnis",
                        "COMPANY" to "Perusahaan",
                        "PROJECT" to "Proyek",
                        "OTHER" to "Lainnya"
                    ),
                    onSelected = { type = it }
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, type) }) { Text("Buat") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun TransactionDialog(
    accounts: List<AccountBalanceRow>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, Long, Long?, Long?, String) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var fromId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var toId by remember(accounts) { mutableLongStateOf(accounts.getOrNull(1)?.id ?: 0L) }
    var categoryId by remember(categories, type) {
        mutableLongStateOf(categories.firstOrNull { it.kind == type }?.id ?: 0L)
    }
    val visibleCategories = categories.filter { it.kind == type }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat transaksi") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = type == "EXPENSE", onClick = { type = "EXPENSE" }, label = { Text("Pengeluaran") })
                    FilterChip(selected = type == "INCOME", onClick = { type = "INCOME" }, label = { Text("Pemasukan") })
                    FilterChip(selected = type == "TRANSFER", onClick = { type = "TRANSFER" }, label = { Text("Transfer") })
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Nominal (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Keterangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (accounts.isEmpty()) {
                    Text("Tambahkan akun terlebih dahulu.", color = MaterialTheme.colorScheme.error)
                } else {
                    IdDropdown(
                        label = "Akun sumber",
                        selectedId = fromId,
                        options = accounts.map { it.id to it.name },
                        onSelected = { fromId = it }
                    )
                    if (type == "TRANSFER") {
                        IdDropdown(
                            label = "Akun tujuan",
                            selectedId = toId,
                            options = accounts.filter { it.id != fromId }.map { it.id to it.name },
                            onSelected = { toId = it }
                        )
                    } else {
                        IdDropdown(
                            label = "Kategori",
                            selectedId = categoryId,
                            options = visibleCategories.map { it.id to it.name },
                            onSelected = { categoryId = it }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                Text(
                    "Tanggal transaksi otomatis menggunakan waktu perangkat saat ini. Nominal IDR disimpan sebagai bilangan bulat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = accounts.isNotEmpty() && amountText.toLongOrNull()?.let { it > 0L } == true &&
                    title.isNotBlank() && (type != "TRANSFER" || (toId != 0L && toId != fromId)),
                onClick = {
                    onSave(
                        type,
                        amountText.toLong(),
                        title,
                        fromId,
                        if (type == "TRANSFER") toId else null,
                        if (type == "TRANSFER" || categoryId == 0L) null else categoryId,
                        note
                    )
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AccountMinimumBalanceDialog(
    account: AccountBalanceRow,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var minimumText by remember(account.id, account.minimumBalanceMinor) {
        mutableStateOf(account.minimumBalanceMinor.toString())
    }
    val parsedMinimum = MinimumBalanceParser.parse(minimumText)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Batas saldo minimum") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Akun: ${account.name}", fontWeight = FontWeight.SemiBold)
                Text("Saldo saat ini: ${formatRupiah(account.balanceMinor)}")
                OutlinedTextField(
                    value = minimumText,
                    onValueChange = { minimumText = it.filter { char -> char in '0'..'9' } },
                    label = { Text("Batas minimum (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Peringatan muncul ketika saldo berada di bawah nilai ini. Isi 0 untuk menonaktifkan peringatan. Pengaturan ini tidak mengubah saldo atau transaksi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (minimumText.isNotBlank() && parsedMinimum == null) {
                    Text(
                        "Nominal terlalu besar atau tidak valid.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsedMinimum != null,
                onClick = { parsedMinimum?.let(onSave) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AccountDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("CASH") }
    var opening by remember { mutableStateOf("0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah akun") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama akun") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ChoiceDropdown(
                    label = "Jenis akun",
                    selected = type,
                    options = listOf(
                        "CASH" to "Tunai",
                        "BANK" to "Rekening bank",
                        "SAVINGS" to "Tabungan",
                        "EWALLET" to "E-wallet",
                        "CREDIT_CARD" to "Kartu kredit",
                        "INVESTMENT" to "Investasi",
                        "OTHER" to "Lainnya"
                    ),
                    onSelected = { type = it }
                )
                OutlinedTextField(
                    value = opening,
                    onValueChange = { opening = it.filter { char -> char.isDigit() } },
                    label = { Text("Saldo awal (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && opening.toLongOrNull() != null,
                onClick = { onSave(name, type, opening.toLong()) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun BudgetDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Long, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var categoryId by remember { mutableLongStateOf(0L) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anggaran bulanan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama anggaran") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it.filter { char -> char.isDigit() } },
                    label = { Text("Batas anggaran (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val categoryOptions = listOf(0L to "Semua pengeluaran") + categories.map { it.id to it.name }
                IdDropdown(
                    label = "Kategori (opsional)",
                    selectedId = categoryId,
                    options = categoryOptions,
                    onSelected = { categoryId = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && limit.toLongOrNull()?.let { it > 0L } == true,
                onClick = { onSave(name, limit.toLong(), categoryId.takeIf { it != 0L }) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun IdDropdown(
    label: String,
    selectedId: Long,
    options: List<Pair<Long, String>>,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selectedId }?.second ?: "Pilih"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label + ": " + selectedName, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.second) },
                    onClick = {
                        onSelected(item.first)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ChoiceDropdown(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selected }?.second ?: "Pilih"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label + ": " + selectedName, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.second) },
                    onClick = {
                        onSelected(item.first)
                        expanded = false
                    }
                )
            }
        }
    }
}


private fun transactionDayStart(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun transactionDayLabel(dayStart: Long): String =
    SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date(dayStart))

private fun currentMonthBounds(): Pair<Long, Long> {
    val start = Calendar.getInstance()
    start.set(Calendar.DAY_OF_MONTH, 1)
    start.set(Calendar.HOUR_OF_DAY, 0)
    start.set(Calendar.MINUTE, 0)
    start.set(Calendar.SECOND, 0)
    start.set(Calendar.MILLISECOND, 0)
    val startMillis = start.timeInMillis
    start.add(Calendar.MONTH, 1)
    start.add(Calendar.MILLISECOND, -1)
    return startMillis to start.timeInMillis
}

@Composable
private fun formatRupiah(value: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.currency = Currency.getInstance("IDR")
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return duitDisplayAmount(formatter.format(value))
}

private fun transactionTypeLabel(type: String): String = when (type) {
    "INCOME" -> "Pemasukan"
    "EXPENSE" -> "Pengeluaran"
    "TRANSFER" -> "Transfer"
    "GOAL_CONTRIBUTION" -> "Setoran target"
    "GOAL_WITHDRAWAL" -> "Penarikan target"
    "LIABILITY_PAYMENT" -> "Pembayaran utang"
    "LIABILITY_RECEIVED" -> "Pencairan pinjaman"
    "RECEIVABLE_COLLECTION" -> "Penerimaan piutang"
    else -> "Transaksi"
}

private fun accountTypeLabel(type: String): String = when (type) {
    "CASH" -> "Tunai"
    "BANK" -> "Rekening bank"
    "SAVINGS" -> "Tabungan"
    "EWALLET" -> "E-wallet"
    "CREDIT_CARD" -> "Kartu kredit"
    "INVESTMENT" -> "Investasi"
    else -> "Akun lainnya"
}
