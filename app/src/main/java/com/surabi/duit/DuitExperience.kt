package com.surabi.duit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surabi.duit.data.AccountBalanceRow
import com.surabi.duit.data.BudgetOverviewRow
import com.surabi.duit.data.DashboardPeriod
import com.surabi.duit.data.DashboardPeriodResolver
import com.surabi.duit.data.DashboardTrendPoint
import com.surabi.duit.data.FinanceAlertEngine
import com.surabi.duit.data.FinanceAlertSeverity
import com.surabi.duit.data.GoalOverviewRow
import com.surabi.duit.data.LiabilityOverviewRow
import com.surabi.duit.data.ReceivableOverviewRow
import com.surabi.duit.data.TransactionEntity
import com.surabi.duit.data.WorkspaceEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DuitInk = Color(0xFF172B25)
private val DuitMuted = Color(0xFF65766F)
private val DuitCanvas = Color(0xFFF3F7F4)
private val DuitGreen = Color(0xFF087C55)
private val DuitBlue = Color(0xFF2767C7)
private val DuitRed = Color(0xFFD34A50)
private val DuitAmber = Color(0xFFE59A32)

@Composable
internal fun DuitAppHeader(
    workspaces: List<WorkspaceEntity>,
    selectedId: Long,
    onWorkspaceSelected: (Long) -> Unit,
    onAddWorkspace: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = workspaces.firstOrNull { it.id == selectedId }
    Surface(color = Color.White, shadowElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(DuitGreen, Color(0xFF174F82)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Wallet, contentDescription = "Logo Duit", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("DUIT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold,
                    color = DuitInk, letterSpacing = 1.2.sp)
                Text("MANAJEMEN KEUANGAN", style = MaterialTheme.typography.labelSmall,
                    color = DuitMuted, maxLines = 1)
            }
            Box {
                FilledTonalButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("WORKSPACE", style = MaterialTheme.typography.labelSmall, color = DuitMuted)
                        Text(current?.name ?: "Pilih", style = MaterialTheme.typography.labelLarge,
                            color = DuitInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Pilih workspace", modifier = Modifier.size(17.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    workspaces.forEach { workspace ->
                        DropdownMenuItem(text = { Text(workspace.name) },
                            onClick = { onWorkspaceSelected(workspace.id); expanded = false })
                    }
                    if (workspaces.isEmpty()) {
                        DropdownMenuItem(text = { Text("Memuat workspace…") }, onClick = { expanded = false })
                    }
                }
            }
            IconButton(onClick = onAddWorkspace, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah workspace", tint = DuitGreen)
            }
        }
    }
}

@Composable
internal fun DuitBottomDock(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val navigation = listOf(
        "Ringkas" to Icons.Filled.Home,
        "Transaksi" to Icons.Filled.ReceiptLong,
        "Rencana" to Icons.Filled.PieChart,
        "Aset" to Icons.Filled.AccountBalanceWallet,
        "Panduan" to Icons.Filled.Info
    )
    Surface(color = Color.White, shadowElevation = 16.dp, tonalElevation = 1.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigation.forEachIndexed { index, item ->
                val selected = selectedIndex == index
                Column(
                    modifier = Modifier.weight(1f).height(58.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { onSelect(index) }
                        .padding(horizontal = 2.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.width(28.dp).height(3.dp)
                        .background(if (selected) DuitGreen else Color.Transparent, CircleShape))
                    Box(
                        Modifier.size(width = 45.dp, height = 29.dp)
                            .background(if (selected) Color(0xFFDFF4E8) else Color.Transparent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.second, contentDescription = item.first,
                            tint = if (selected) DuitGreen else Color(0xFF819087), modifier = Modifier.size(20.dp))
                    }
                    Text(item.first, style = MaterialTheme.typography.labelSmall,
                        color = if (selected) DuitGreen else Color(0xFF78877F),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun DuitDashboardScreen(
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
    trend: List<DashboardTrendPoint>,
    period: DashboardPeriod,
    onPeriodChange: (DashboardPeriod) -> Unit,
    onAddTransaction: () -> Unit,
    onAddGoal: () -> Unit,
    onOpenPlanning: () -> Unit,
    onOpenWealth: () -> Unit,
    onOpenTransactions: () -> Unit
) {
    val totalBalance = accounts.sumOf { it.balanceMinor }
    val alerts = remember(accounts, budgets, liabilities, receivables, income, expenses, period) {
        FinanceAlertEngine.evaluate(
            accounts, budgets, liabilities, receivables, income, expenses,
            System.currentTimeMillis(),
            periodLabel = DashboardPeriodResolver.label(period).lowercase(Locale("id", "ID"))
        )
    }
    val dueRows = remember(liabilities, receivables) {
        (liabilities.filter { it.outstandingMinor > 0L && it.dueAt != null }.map {
            Triple("Utang", it.name, it.dueAt!!)
        } + receivables.filter { it.outstandingMinor > 0L && it.dueAt != null }.map {
            Triple("Piutang", it.name, it.dueAt!!)
        }).sortedBy { it.third }.take(4)
    }
    val todayText = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date()) }
    val greeting = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    Column(
        Modifier.fillMaxSize().background(DuitCanvas).verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(greeting, style = MaterialTheme.typography.titleMedium, color = DuitMuted)
                Text("Ringkasan keuangan", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold, color = DuitInk)
                Text("$workspaceName  •  $todayText", style = MaterialTheme.typography.bodySmall, color = DuitMuted)
            }
            Box(Modifier.size(46.dp).background(Color(0xFFE1F2E8), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = DuitGreen, modifier = Modifier.size(23.dp))
            }
        }

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            DashboardPeriod.entries.forEach { option ->
                val label = when (option) {
                    DashboardPeriod.THIS_MONTH -> "Bulan ini"
                    DashboardPeriod.LAST_30_DAYS -> "30 hari terakhir"
                    DashboardPeriod.THIS_YEAR -> "Tahun ini"
                }
                FilterChip(selected = period == option, onClick = { onPeriodChange(option) }, label = { Text(label) })
            }
        }

        DuitAccountHero(totalBalance, netWorth, accounts.size, onOpenWealth)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DuitCashMetric("Uang masuk", duitMoney(income), Icons.Filled.ArrowDownward, DuitGreen,
                DashboardPeriodResolver.label(period), Modifier.weight(1f))
            DuitCashMetric("Uang keluar", duitMoney(expenses), Icons.Filled.ArrowUpward, DuitRed,
                DashboardPeriodResolver.label(period), Modifier.weight(1f))
        }

        DuitMonthlyTrendCard(points = trend)

        Text("Akses cepat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DuitInk)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            DuitQuickAction("Transaksi", Icons.Filled.Add, Color(0xFFE1F3E9), DuitGreen, onAddTransaction, Modifier.weight(1f))
            DuitQuickAction("Rencana", Icons.Filled.PieChart, Color(0xFFE7EFFF), DuitBlue, onOpenPlanning, Modifier.weight(1f))
            DuitQuickAction("Aset", Icons.Filled.Wallet, Color(0xFFFFF0D9), Color(0xFFB96E0C), onOpenWealth, Modifier.weight(1f))
        }

        DuitDashboardSectionHeader("Dompet dan rekening", "Saldo tiap akun", "Semua akun", onOpenWealth)
        if (accounts.isEmpty()) {
            DuitEmptyState(title = "Belum ada dompet atau rekening",
                message = "Tambahkan akun pertama agar saldo dan aktivitas keuangan dapat dirangkum.")
        } else {
            accounts.take(3).forEach { DuitAccountRow(it) }
        }

        DuitDashboardSectionHeader("Rencana berjalan", "Anggaran dan sasaran tabungan", "Kelola", onOpenPlanning)
        if (budgets.isEmpty() && goals.isEmpty()) {
            DuitEmptyState(title = "Belum ada rencana keuangan",
                message = "Buat anggaran pengeluaran atau target tabungan untuk mulai mengukur progres.",
                action = { Button(onClick = onAddGoal) { Text("Buat target pertama") } })
        } else {
            budgets.take(2).forEach { DuitBudgetSnapshot(it) }
            goals.take(2).forEach { DuitGoalSnapshot(it) }
        }

        if (alerts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                DuitDashboardSectionHeader("Perlu perhatian", "Peringatan berbasis catatan keuangan lokal", null, null)
                alerts.take(3).forEach { alert ->
                    val isCritical = alert.severity == FinanceAlertSeverity.CRITICAL
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                        .background(if (isCritical) Color(0xFFFFEEEE) else Color(0xFFFFF3DF)).padding(13.dp),
                        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Info, contentDescription = "Peringatan", tint = if (isCritical) DuitRed else DuitAmber)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(alert.title, fontWeight = FontWeight.Bold, color = DuitInk)
                            Text(alert.message, style = MaterialTheme.typography.bodySmall, color = DuitMuted)
                        }
                    }
                }
            }
        }

        if (dueRows.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                DuitDashboardSectionHeader("Jatuh tempo mendatang", "Data dari utang dan piutang aktif", null, null)
                dueRows.forEach { (kind, name, dueAt) ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(38.dp).background(if (kind == "Utang") Color(0xFFFFE6E7) else Color(0xFFE5F0FF),
                            RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null,
                                tint = if (kind == "Utang") DuitRed else DuitBlue)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold, color = DuitInk)
                            Text(kind, style = MaterialTheme.typography.labelSmall, color = DuitMuted)
                        }
                        Text(SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(Date(dueAt)),
                            style = MaterialTheme.typography.labelMedium, color = DuitMuted)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Aktivitas terbaru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DuitInk)
                Text("Transaksi tersimpan terakhir", style = MaterialTheme.typography.bodySmall, color = DuitMuted)
            }
            TextButton(onClick = onOpenTransactions) { Text("Lihat semua") }
        }
        if (transactions.isEmpty()) {
            DuitEmptyState(title = "Belum ada aktivitas", message = "Transaksi yang dicatat akan muncul dalam daftar ini.",
                action = { Button(onClick = onAddTransaction) { Text("Catat transaksi") } })
        } else {
            transactions.take(4).forEach { DuitTransactionTile(item = it) }
        }
        Spacer(Modifier.height(82.dp))
    }
}

@Composable
internal fun DuitFeatureBanner(
    kicker: String,
    value: String,
    supporting: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF073F32), Color(0xFF087C55), Color(0xFF245F91))))
    ) {
        Box(Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 16.dp)
            .size(100.dp).background(Color.White.copy(alpha = 0.07f), CircleShape))
        Column(Modifier.fillMaxWidth().padding(19.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(kicker, color = Color(0xFFD5F2E4), style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(supporting, color = Color(0xFFD5F2E4), style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f))
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction) {
                        Text(actionLabel, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuitAccountHero(totalBalance: Long, netWorth: Long, accountCount: Int, onDetails: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(Color(0xFF073F32), Color(0xFF087C55), Color(0xFF1C6892)))
        )) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 16.dp).size(108.dp)
                .background(Color.White.copy(alpha = 0.07f), CircleShape))
            Column(Modifier.fillMaxWidth().padding(21.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL SALDO TERSEDIA", color = Color(0xFFD3F3E3),
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Filled.Wallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
                }
                Text(duitMoney(totalBalance), style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$accountCount akun aktif", color = Color(0xFFD3F3E3), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDetails) { Text("Rincian saldo  ›", color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.20f)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("KEKAYAAN BERSIH", color = Color(0xFFD3F3E3), style = MaterialTheme.typography.labelSmall)
                        Text(duitMoney(netWorth), color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Surface(color = Color.White.copy(alpha = 0.13f), shape = RoundedCornerShape(12.dp)) {
                        Text("ESTIMASI", color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuitCashMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    supporting: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.clip(RoundedCornerShape(19.dp)).background(Color.White).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(Modifier.size(29.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = DuitMuted, fontWeight = FontWeight.SemiBold)
        }
        Text(value, color = DuitInk, fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(supporting, style = MaterialTheme.typography.labelSmall, color = DuitMuted)
    }
}

@Composable
private fun DuitMonthlyTrendCard(points: List<DashboardTrendPoint>) {
    val totalIncome = points.sumOf { it.incomeMinor }
    val totalExpense = points.sumOf { it.expenseMinor }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Tren arus kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DuitInk)
                Text("Enam bulan terakhir • data tersimpan", style = MaterialTheme.typography.bodySmall, color = DuitMuted)
            }
            Box(Modifier.size(36.dp).background(Color(0xFFE8F0FF), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PieChart, contentDescription = null, tint = DuitBlue)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
            DuitLegendDot("Masuk", DuitGreen, totalIncome)
            DuitLegendDot("Keluar", DuitBlue, totalExpense)
        }
        Canvas(Modifier.fillMaxWidth().height(132.dp)) {
            val maxValue = (points.maxOfOrNull { maxOf(it.incomeMinor, it.expenseMinor) } ?: 0L).coerceAtLeast(1L).toDouble()
            val slot = size.width / points.size.coerceAtLeast(1)
            val barWidth = (slot * 0.25f).coerceAtMost(17.dp.toPx())
            val gap = 4.dp.toPx()
            val chartHeight = size.height * 0.82f
            val baseY = size.height * 0.92f
            for (line in 0..3) {
                val y = baseY - (chartHeight * line / 3f)
                drawLine(Color(0xFFE8EEE9), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            points.forEachIndexed { index, point ->
                val center = slot * (index + 0.5f)
                val inHeight = (point.incomeMinor / maxValue).toFloat() * chartHeight
                val outHeight = (point.expenseMinor / maxValue).toFloat() * chartHeight
                if (point.incomeMinor > 0L) {
                    drawRoundRect(DuitGreen, Offset(center - barWidth - gap / 2f, baseY - inHeight),
                        Size(barWidth, inHeight.coerceAtLeast(2.dp.toPx())), CornerRadius(6.dp.toPx(), 6.dp.toPx()))
                }
                if (point.expenseMinor > 0L) {
                    drawRoundRect(DuitBlue, Offset(center + gap / 2f, baseY - outHeight),
                        Size(barWidth, outHeight.coerceAtLeast(2.dp.toPx())), CornerRadius(6.dp.toPx(), 6.dp.toPx()))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { Text(it.label, style = MaterialTheme.typography.labelSmall, color = DuitMuted) }
        }
        if (points.all { it.incomeMinor == 0L && it.expenseMinor == 0L }) {
            Text("Belum ada pemasukan atau pengeluaran pada enam bulan ini.",
                style = MaterialTheme.typography.bodySmall, color = DuitMuted)
        }
    }
}

@Composable
private fun DuitLegendDot(label: String, color: Color, value: Long) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = DuitMuted)
        Text(duitCompactMoney(value), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DuitInk)
    }
}

@Composable
private fun DuitQuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).clickable(onClick = onClick)
        .padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.size(40.dp).background(tint, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = DuitInk, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DuitDashboardSectionHeader(title: String, subtitle: String, action: String?, onAction: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DuitInk)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DuitMuted)
        }
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun DuitAccountRow(account: AccountBalanceRow) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
        .padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(Modifier.size(38.dp).background(Color(0xFFE5F3EB), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = DuitGreen, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(account.name, fontWeight = FontWeight.SemiBold, color = DuitInk)
            Text(account.type.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = DuitMuted)
        }
        Text(duitMoney(account.balanceMinor), fontWeight = FontWeight.Bold, color = DuitInk,
            style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DuitBudgetSnapshot(budget: BudgetOverviewRow) {
    val ratio = if (budget.limitMinor > 0L) budget.spentMinor.toDouble() / budget.limitMinor else 0.0
    val remaining = budget.limitMinor - budget.spentMinor
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color.White).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(budget.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = DuitInk)
            Text("${(ratio * 100).toInt()}%", fontWeight = FontWeight.Bold,
                color = if (ratio >= 1.0) DuitRed else DuitGreen)
        }
        LinearProgressIndicator(progress = { ratio.coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = if (ratio >= 1.0) DuitRed else DuitGreen, trackColor = Color(0xFFE8EEE9))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Terpakai ${duitCompactMoney(budget.spentMinor)}", style = MaterialTheme.typography.labelSmall, color = DuitMuted)
            Spacer(Modifier.weight(1f))
            Text(if (remaining >= 0) "Sisa ${duitCompactMoney(remaining)}" else "Lewat batas ${duitCompactMoney(-remaining)}",
                style = MaterialTheme.typography.labelSmall, color = if (remaining >= 0) DuitGreen else DuitRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DuitGoalSnapshot(goal: GoalOverviewRow) {
    val progress = if (goal.targetMinor > 0L) (goal.savedMinor.toDouble() / goal.targetMinor).coerceIn(0.0, 1.0) else 0.0
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color.White).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(Modifier.size(39.dp).background(Color(0xFFE9F0FF), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PieChart, contentDescription = null, tint = DuitBlue)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(goal.name, fontWeight = FontWeight.SemiBold, color = DuitInk)
            Text("${(progress * 100).toInt()}% • ${duitCompactMoney(goal.savedMinor)} dari ${duitCompactMoney(goal.targetMinor)}",
                style = MaterialTheme.typography.labelSmall, color = DuitMuted)
            LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp),
                color = DuitBlue, trackColor = Color(0xFFE8EEE9))
        }
    }
}

@Composable
internal fun DuitTransactionTile(
    item: TransactionEntity,
    accountName: String? = null,
    destinationName: String? = null,
    categoryName: String? = null,
    onClick: (() -> Unit)? = null,
    onVoid: (() -> Unit)? = null
) {
    val isCredit = item.type in setOf("INCOME", "GOAL_WITHDRAWAL", "LIABILITY_RECEIVED", "RECEIVABLE_COLLECTION")
    val isTransfer = item.type == "TRANSFER"
    val accent = when { isCredit -> DuitGreen; isTransfer -> DuitBlue; else -> DuitRed }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color.White)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(Modifier.size(43.dp).background(accent.copy(alpha = 0.11f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(when { isTransfer -> Icons.Filled.SwapHoriz; isCredit -> Icons.Filled.ArrowDownward; else -> Icons.Filled.ArrowUpward },
                contentDescription = transactionTypeLabel(item.type), tint = accent)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(item.title, fontWeight = FontWeight.Bold, color = DuitInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOfNotNull(categoryName, accountName, if (isTransfer) "→ ${destinationName ?: "akun tujuan"}" else null)
                .joinToString(" · ").ifBlank { transactionTypeLabel(item.type) },
                style = MaterialTheme.typography.labelSmall, color = DuitMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.note.isNotBlank()) Text(item.note, style = MaterialTheme.typography.labelSmall,
                color = DuitMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(SimpleDateFormat("d MMM • HH:mm", Locale("id", "ID")).format(Date(item.occurredAt)),
                style = MaterialTheme.typography.labelSmall, color = DuitMuted)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text((if (isCredit) "+" else if (isTransfer) "↔ " else "− ") + duitMoney(item.amountMinor),
                color = accent, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (onVoid != null) {
                IconButton(onClick = onVoid, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Batalkan transaksi", tint = DuitRed, modifier = Modifier.size(18.dp))
                }
            } else if (onClick != null) {
                Text("Detail", color = DuitGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun duitMoney(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(value)

private fun duitCompactMoney(value: Long): String = when {
    value >= 1_000_000_000L -> "Rp ${"%.1f".format(Locale.US, value / 1_000_000_000.0)} M"
    value >= 1_000_000L -> "Rp ${"%.1f".format(Locale.US, value / 1_000_000.0)} jt"
    value >= 1_000L -> "Rp ${"%.0f".format(Locale.US, value / 1_000.0)} rb"
    else -> duitMoney(value)
}
