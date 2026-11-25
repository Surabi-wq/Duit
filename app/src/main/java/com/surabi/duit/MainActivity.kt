package com.surabi.duit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.surabi.duit.data.TransactionDateRange
import com.surabi.duit.data.TransactionCsvExporter
import com.surabi.duit.data.WorkspaceEntity
import com.surabi.duit.ui.DuitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuitApp(repository: FinanceRepository) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val csv = pendingCsv
        pendingCsv = null
        if (uri != null && csv != null) {
            scope.launch {
                val writeResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("Lokasi penyimpanan tidak dapat dibuka.")
                        output.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                    }
                }
                writeResult
                    .onSuccess { snackbar.showSnackbar("Ekspor CSV berhasil disimpan.") }
                    .onFailure { snackbar.showSnackbar(it.message ?: "Ekspor CSV gagal disimpan.") }
            }
        }
    }
    val workspaces by repository.workspaces.collectAsState(initial = emptyList())
    var selectedWorkspaceId by remember { mutableLongStateOf(0L) }
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showQuickActions by remember { mutableStateOf(false) }
    var dashboardPeriod by remember { mutableStateOf(DashboardPeriod.THIS_MONTH) }
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

    LaunchedEffect(Unit) { repository.ensureDefaults() }
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
    val netWorth = accounts.sumOf { it.balanceMinor } +
        goals.sumOf { it.savedMinor } +
        assets.sumOf { it.currentValueMinor } +
        receivables.sumOf { it.outstandingMinor } -
        liabilities.sumOf { it.outstandingMinor }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_duit_logo),
                            contentDescription = "Logo Duit",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(34.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("Duit", fontWeight = FontWeight.Bold)
                            Text(
                                selectedWorkspace?.name ?: "Menyiapkan ruang keuangan…",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    WorkspacePicker(workspaces, workspaceId) { selectedWorkspaceId = it }
                    IconButton(onClick = { showWorkspaceDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Tambah workspace")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showQuickActions = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Filled.Add, contentDescription = "Buka tindakan cepat") }
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
            when (selectedTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    workspaceName = selectedWorkspace?.name ?: "Workspace",
                    accounts = accounts,
                    transactions = transactions,
                    budgets = budgets,
                    goals = goals,
                    income = income,
                    expenses = expenses,
                    netWorth = netWorth,
                    liabilities = liabilities,
                    receivables = receivables,
                    period = dashboardPeriod,
                    onPeriodChange = { dashboardPeriod = it },
                    onAddTransaction = { showTransactionDialog = true },
                    onAddGoal = { showGoalDialog = true },
                    onOpenPlanning = { selectedTab = AppTab.BUDGETS },
                    onOpenWealth = { selectedTab = AppTab.ACCOUNTS }
                )
                AppTab.TRANSACTIONS -> TransactionsScreen(
                    workspaceId = workspaceId,
                    repository = repository,
                    hasTransactions = transactions.isNotEmpty(),
                    accounts = accounts,
                    categories = categories,
                    onAdd = { showTransactionDialog = true },
                    onExport = { rows ->
                        pendingCsv = TransactionCsvExporter.toCsv(
                            transactions = rows,
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
            onSave = { name, target, priority ->
                scope.launch {
                    runCatching { repository.createSavingsGoal(workspaceId, name, target, null, priority) }
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
        TextButton(onClick = { expanded = true }) {
            Text(current?.name ?: "Pilih")
            Icon(Icons.Filled.ExpandMore, contentDescription = null)
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
private fun DashboardScreen(
    workspaceName: String,
    accounts: List<AccountBalanceRow>,
    transactions: List<TransactionEntity>,
    budgets: List<BudgetOverviewRow>,
    goals: List<GoalOverviewRow>,
    income: Long,
    expenses: Long,
    netWorth: Long,
    liabilities: List<LiabilityOverviewRow>,
    receivables: List<ReceivableOverviewRow>,
    period: DashboardPeriod,
    onPeriodChange: (DashboardPeriod) -> Unit,
    onAddTransaction: () -> Unit,
    onAddGoal: () -> Unit,
    onOpenPlanning: () -> Unit,
    onOpenWealth: () -> Unit
) {
    val totalBalance = accounts.sumOf { it.balanceMinor }
    val netCashflow = income - expenses
    val alerts = remember(accounts, budgets, liabilities, receivables, income, expenses) {
        FinanceAlertEngine.evaluate(accounts, budgets, liabilities, receivables, income, expenses, System.currentTimeMillis())
    }
    val dueRows = remember(liabilities, receivables) {
        (liabilities.filter { it.outstandingMinor > 0L && it.dueAt != null }.map {
            Triple("Utang", it.name, it.dueAt!!)
        } + receivables.filter { it.outstandingMinor > 0L && it.dueAt != null }.map {
            Triple("Piutang", it.name, it.dueAt!!)
        }).sortedBy { it.third }.take(4)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DuitPageHeader(
            title = "Ringkasan keuangan",
            subtitle = "$workspaceName • kondisi keuangan aktual",
            trailing = {
                ChoiceDropdown(
                    label = "Periode",
                    selected = period.name,
                    options = listOf(
                        DashboardPeriod.THIS_MONTH.name to "Bulan ini",
                        DashboardPeriod.LAST_30_DAYS.name to "30 hari",
                        DashboardPeriod.THIS_YEAR.name to "Tahun ini"
                    ),
                    onSelected = { value ->
                        runCatching { DashboardPeriod.valueOf(value) }.getOrNull()?.let(onPeriodChange)
                    }
                )
            }
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total saldo akun", color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.labelLarge)
                Text(
                    formatRupiah(totalBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${accounts.size} akun aktif", color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onOpenWealth) {
                        Text("Rincian akun", color = Color.White)
                    }
                }
            }
        }

        DuitPanel {
            DuitSectionHeader("Kekayaan bersih saat ini", "Estimasi dari data akun dan aset yang tercatat")
            Text(formatRupiah(netWorth), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Saldo akun + dana target + aset non-tunai + piutang − sisa utang",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DuitMetricCard(
                label = "Pemasukan • ${DashboardPeriodResolver.label(period)}",
                value = formatRupiah(income),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.primary
            )
            DuitMetricCard(
                label = "Pengeluaran • ${DashboardPeriodResolver.label(period)}",
                value = formatRupiah(expenses),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.error
            )
        }

        DuitPanel {
            DuitSectionHeader("Arus kas", "Berdasarkan transaksi pada periode terpilih")
            Text(
                formatRupiah(netCashflow),
                style = MaterialTheme.typography.headlineSmall,
                color = if (netCashflow >= 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            CashflowBar(income = income, expenses = expenses)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Masuk ${formatRupiah(income)}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                Text("Keluar ${formatRupiah(expenses)}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            }
            Text(
                "Transfer antar-akun tidak dihitung sebagai pemasukan atau pengeluaran.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (accounts.isNotEmpty()) {
            DuitSectionHeader("Rincian saldo akun", "Saldo tersedia pada workspace aktif") {
                TextButton(onClick = onOpenWealth) { Text("Lihat semua") }
            }
            accounts.take(3).forEach { account ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(account.name, fontWeight = FontWeight.SemiBold)
                            Text(account.type.replace('_', ' '), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatRupiah(account.balanceMinor), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (alerts.isNotEmpty()) {
            DuitPanel {
                DuitSectionHeader("Peringatan keuangan", "Pemeriksaan berbasis aturan dari data lokal")
                alerts.take(5).forEach { alert ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            imageVector = if (alert.severity == FinanceAlertSeverity.CRITICAL) Icons.Filled.Info else Icons.Filled.ExpandMore,
                            contentDescription = if (alert.severity == FinanceAlertSeverity.CRITICAL) "Peringatan kritis" else "Peringatan",
                            tint = if (alert.severity == FinanceAlertSeverity.CRITICAL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(alert.title, fontWeight = FontWeight.SemiBold)
                            Text(alert.message, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        DuitSectionHeader("Anggaran", "Realisasi dari pengeluaran yang tersimpan") {
            TextButton(onClick = onOpenPlanning) { Text("Kelola") }
        }
        if (budgets.isEmpty()) {
            DuitEmptyState(
                title = "Belum ada anggaran",
                message = "Buat batas pengeluaran untuk memantau realisasi dan dana yang tersisa.",
                icon = Icons.Filled.PieChart,
                action = { Button(onClick = onOpenPlanning) { Text("Buka perencanaan") } }
            )
        } else {
            budgets.take(3).forEach { BudgetProgressCard(it) }
        }

        DuitSectionHeader("Target tabungan", "Progres sasaran keuangan") {
            TextButton(onClick = onOpenPlanning) { Text("Lihat semua") }
        }
        if (goals.isEmpty()) {
            DuitEmptyState(
                title = "Belum ada target",
                message = "Tentukan sasaran untuk dana darurat, pendidikan, atau kebutuhan lain.",
                icon = Icons.Filled.Savings,
                action = { OutlinedButton(onClick = onAddGoal) { Text("Buat target") } }
            )
        } else {
            goals.take(3).forEach { goal ->
                val progress = if (goal.targetMinor > 0L) {
                    (goal.savedMinor.toDouble() / goal.targetMinor.toDouble()).coerceIn(0.0, 1.0).toFloat()
                } else 0f
                DuitPanel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(goal.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("${formatRupiah(goal.savedMinor)} terkumpul dari ${formatRupiah(goal.targetMinor)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (dueRows.isNotEmpty()) {
            DuitSectionHeader("Jatuh tempo yang tercatat", "Tanggal berasal dari data utang dan piutang")
            DuitPanel {
                dueRows.forEach { (kind, title, dueAt) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(kind, style = MaterialTheme.typography.labelMedium,
                            color = if (kind == "Utang") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Text(SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(dueAt)),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        DuitSectionHeader("Transaksi terbaru", "Aktivitas terkini pada workspace aktif")
        if (transactions.isEmpty()) {
            DuitEmptyState(
                title = "Belum ada transaksi",
                message = "Pemasukan, pengeluaran, dan transfer yang disimpan akan muncul di sini.",
                icon = Icons.Filled.ReceiptLong,
                action = { Button(onClick = onAddTransaction) { Text("Catat transaksi pertama") } }
            )
        } else {
            transactions.take(5).forEach { TransactionCard(it) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onAddTransaction, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Transaksi")
            }
            OutlinedButton(onClick = onAddGoal, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Savings, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Target")
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = accent)
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CashflowBar(income: Long, expenses: Long) {
    val total = (income + expenses).coerceAtLeast(1L).toFloat()
    Row(
        modifier = Modifier.fillMaxWidth().height(12.dp).background(
            MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(20.dp)
        )
    ) {
        Box(
            Modifier.weight((income.toFloat() / total).coerceAtLeast(0.001f))
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
        )
        Box(
            Modifier.weight((expenses.toFloat() / total).coerceAtLeast(0.001f))
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(20.dp))
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
            Spacer(Modifier.width(5.dp))
            Text("Masuk", style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(50)))
            Spacer(Modifier.width(5.dp))
            Text("Keluar", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TransactionsScreen(
    workspaceId: Long,
    repository: FinanceRepository,
    hasTransactions: Boolean,
    accounts: List<AccountBalanceRow>,
    categories: List<CategoryEntity>,
    onAdd: () -> Unit,
    onExport: (List<TransactionEntity>) -> Unit,
    onVoid: (Long, String) -> Unit
) {
    val accountNames = accounts.associate { it.id to it.name }
    val categoryNames = categories.associate { it.id to it.name }
    var searchQuery by remember(workspaceId) { mutableStateOf("") }
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
    var voidReason by remember { mutableStateOf("") }

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
    val visible by remember(
        workspaceId, searchQuery, typeFilter, dateBounds, accountFilterId,
        categoryFilterId, minAmount, maxAmount, sortOrder, filterError
    ) {
        if (filterError != null) {
            flowOf(emptyList())
        } else {
            repository.searchTransactions(
                workspaceId = workspaceId,
                query = searchQuery,
                type = typeFilter,
                startAt = dateBounds!!.startAt,
                endAt = dateBounds!!.endAt,
                accountId = accountFilterId.takeIf { it != 0L },
                categoryId = categoryFilterId.takeIf { it != 0L },
                minAmount = minAmount,
                maxAmount = maxAmount,
                sortOrder = sortOrder
            )
        }
    }.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Semua transaksi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Pencarian dan filter langsung pada database",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onExport(visible) },
                enabled = visible.isNotEmpty() && filterError == null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Ekspor CSV")
            }
            Button(onClick = onAdd, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Tambah")
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Cari keterangan, catatan, atau kategori") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
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
            FilterChip(
                selected = showAdvancedFilters,
                onClick = { showAdvancedFilters = !showAdvancedFilters },
                label = { Text(if (showAdvancedFilters) "Tutup filter lanjutan" else "Filter lanjutan") }
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${visible.size} hasil • maksimal 500",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showAdvancedFilters) {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 330.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it.take(10) },
                        label = { Text("Dari (dd-MM-yyyy)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it.take(10) },
                        label = { Text("Sampai (dd-MM-yyyy)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = { minAmountText = it.filter(Char::isDigit) },
                        label = { Text("Nominal minimum") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = { maxAmountText = it.filter(Char::isDigit) },
                        label = { Text("Nominal maksimum") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
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
                    Text(filterError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(
                    onClick = {
                        startDateText = ""
                        endDateText = ""
                        minAmountText = ""
                        maxAmountText = ""
                        accountFilterId = 0L
                        categoryFilterId = 0L
                        sortOrder = "NEWEST"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hapus filter lanjutan")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (filterError != null) {
            EmptyHint("Filter belum valid", filterError)
        } else if (visible.isEmpty()) {
            EmptyHint(
                title = if (!hasTransactions) "Belum ada transaksi" else "Transaksi tidak ditemukan",
                body = if (!hasTransactions) "Transaksi yang Anda simpan akan muncul di halaman ini."
                    else "Ubah kata pencarian atau filter transaksi."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visible, key = { it.id }) { item ->
                    TransactionCard(
                        item,
                        accountName = accountNames[item.fromAccountId],
                        destinationName = item.toAccountId?.let { accountNames[it] },
                        categoryName = item.categoryId?.let { categoryNames[it] },
                        onVoid = { selectedToVoid = item }
                    )
                }
            }
        }
    }

    selectedToVoid?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedToVoid = null; voidReason = "" },
            title = { Text("Batalkan transaksi?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transaksi \"${item.title}\" akan dikecualikan dari saldo dan laporan. Catatan pembatalan disimpan di audit trail.")
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it },
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
    Card(modifier = Modifier.fillMaxWidth()) {
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
                    contentDescription = null,
                    tint = accent
                )
            }
            Column(Modifier.weight(1f)) {
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
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(item.occurredAt)),
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
                }
            }
        }
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

@Composable
private fun GuideScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Panduan Duit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        GuideCard("1. Pisahkan keuangan", "Buat workspace untuk Pribadi, Keluarga, Bisnis, atau Proyek. Data transaksi dan anggaran tiap workspace tidak dicampur.")
        GuideCard("2. Tambahkan akun", "Buat dompet tunai, rekening bank, tabungan, atau e-wallet. Saldo awal menjadi titik awal perhitungan saldo. Dari kartu akun pada tab Aset, atur batas saldo minimum agar dashboard memberi peringatan saat saldo turun di bawah batas.")
        GuideCard("3. Catat transaksi", "Pilih pemasukan, pengeluaran, atau transfer. Transfer hanya memindahkan saldo antar-akun, bukan pendapatan atau biaya.")
        GuideCard("4. Pantau anggaran", "Buat batas pengeluaran bulanan. Realisasi anggaran dihitung dari transaksi pengeluaran yang tersimpan.")
        GuideCard("5. Saring transaksi", "Di halaman Transaksi, cari keterangan atau catatan, lalu gunakan Filter lanjutan untuk menyaring tanggal, akun, kategori, nominal minimum/maksimum, dan urutan hasil. Tanggal akhir mencakup seluruh hari yang dipilih.")
        GuideCard("6. Ekspor CSV", "Ekspor CSV menyimpan hasil pencarian dan filter yang sedang tampil, maksimal 500 transaksi terbaru. Ekspor CSV bukan backup penuh database.")
        GuideCard("Penyimpanan dan privasi", "Data inti disimpan lokal pada perangkat menggunakan Room/SQLite. Alur utama tidak memerlukan internet. Backup dan restore mandiri terenkripsi belum tersedia, sehingga pengguna perlu menjaga data perangkat dan tidak menganggap ekspor CSV sebagai cadangan penuh database.")
        GuideCard("Fitur tahap berikutnya", "Jurnal akuntansi double-entry, rekonsiliasi, transaksi berulang, lampiran struk, impor file, ekspor PDF/Excel/DOCX, ERP, persediaan, investasi lengkap, pajak, OCR, analisis prediktif, multi-pengguna, enkripsi database, dan backup/restore masih dalam pengembangan.")
    }
}

@Composable
private fun GuideCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyHint(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
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

private fun formatRupiah(value: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.currency = Currency.getInstance("IDR")
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return formatter.format(value)
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
