package com.surabi.duit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.surabi.duit.ui.LocalDuitBalancesVisible
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.surabi.duit.data.AccountBalanceRow
import com.surabi.duit.data.AssetEntity
import com.surabi.duit.data.BudgetOverviewRow
import com.surabi.duit.data.GoalOverviewRow
import com.surabi.duit.data.LiabilityOverviewRow
import com.surabi.duit.data.ReceivableOverviewRow
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PlanningScreen(
    budgets: List<BudgetOverviewRow>,
    goals: List<GoalOverviewRow>,
    onAddBudget: () -> Unit,
    onAddGoal: () -> Unit,
    onMoveGoal: (GoalOverviewRow, Boolean) -> Unit
) {
    var section by remember { mutableStateOf("GOALS") }
    val savedTotal = goals.sumOf { it.savedMinor }
    val targetTotal = goals.sumOf { it.targetMinor }
    val remainingTotal = (targetTotal - savedTotal).coerceAtLeast(0L)
    val budgetTotal = budgets.sumOf { it.limitMinor }
    val spentTotal = budgets.sumOf { it.spentMinor }
    val remainingBudget = budgetTotal - spentTotal

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF3F7F4)).verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        DuitPageHeader(
            title = "Rencana keuangan",
            subtitle = "Atur tujuan, jaga anggaran, ukur kemajuan"
        )

        DuitFeatureBanner(
            kicker = "DANA TARGET TERKUMPUL",
            value = financeRupiah(savedTotal),
            supporting = "Sisa seluruh target ${financeRupiah(remainingTotal)}",
            actionLabel = "Buat target",
            onAction = onAddGoal
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DuitMetricCard(
                label = "Total sasaran",
                value = financeRupiah(targetTotal),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.secondary,
                supporting = "${goals.size} target aktif"
            )
            DuitMetricCard(
                label = if (remainingBudget >= 0L) "Sisa anggaran" else "Lebih anggaran",
                value = financeRupiah(kotlin.math.abs(remainingBudget)),
                modifier = Modifier.weight(1f),
                accent = if (remainingBudget < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                supporting = "${budgets.size} anggaran"
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = section == "GOALS",
                onClick = { section = "GOALS" },
                label = { Text("Target tabungan (${goals.size})") }
            )
            FilterChip(
                selected = section == "BUDGETS",
                onClick = { section = "BUDGETS" },
                label = { Text("Anggaran (${budgets.size})") }
            )
        }

        if (section == "GOALS") {
            DuitSectionHeader(
                title = "Target keuangan",
                subtitle = if (goals.isEmpty()) "Mulai dengan satu sasaran yang terukur" else
                    "Terkumpul ${financeRupiah(savedTotal)} dari ${financeRupiah(targetTotal)}",
                trailing = {
                    Button(onClick = onAddGoal) { IconWithLabelAdd("Buat target") }
                }
            )
            if (goals.isEmpty()) {
                DuitEmptyState(
                    title = "Belum ada target tabungan",
                    message = "Buat target dana darurat, pendidikan, perjalanan, atau kebutuhan lain. Setoran target memindahkan saldo dari akun ke dana target.",
                    icon = Icons.Filled.Savings,
                    action = { Button(onClick = onAddGoal) { IconWithLabelAdd("Buat target pertama") } }
                )
            } else {
                goals.forEach { goal ->
                    GoalProgressCard(
                        goal = goal,
                        onContribute = { onMoveGoal(goal, false) },
                        onWithdraw = { onMoveGoal(goal, true) }
                    )
                }
                DuitPanel {
                    Text("Total kekurangan dana", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(financeRupiah(remainingTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Jumlah sisa seluruh target aktif; kontribusi dicatat dari akun yang dipilih.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            DuitSectionHeader(
                title = "Anggaran pengeluaran",
                subtitle = if (budgets.isEmpty()) "Atur batas dan pantau realisasi" else {
                    if (remainingBudget >= 0L) "Sisa gabungan ${financeRupiah(remainingBudget)}"
                    else "Melebihi batas gabungan ${financeRupiah(-remainingBudget)}"
                },
                trailing = { Button(onClick = onAddBudget) { IconWithLabelAdd("Buat") } }
            )
            if (budgets.isEmpty()) {
                DuitEmptyState(
                    title = "Belum ada anggaran",
                    message = "Buat batas pengeluaran umum atau berdasarkan kategori agar realisasi dan sisa dana dapat dipantau.",
                    icon = Icons.Filled.PieChart,
                    action = { Button(onClick = onAddBudget) { IconWithLabelAdd("Buat anggaran pertama") } }
                )
            } else {
                budgets.forEach { budget -> BudgetProgressCardAdvanced(budget) }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun IconWithLabelAdd(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun GoalProgressCard(
    goal: GoalOverviewRow,
    onContribute: () -> Unit,
    onWithdraw: () -> Unit
) {
    val progress = (goal.savedMinor.toDouble() / goal.targetMinor.toDouble()).coerceIn(0.0, 1.0).toFloat()
    val remaining = (goal.targetMinor - goal.savedMinor).coerceAtLeast(0L)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(42.dp).background(Color(0xFFE5F3EB), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(Icons.Filled.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(goal.name, fontWeight = FontWeight.Bold)
                    Text("Prioritas ${goal.priority}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Text(financeRupiah(goal.savedMinor), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dana terkumpul", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("Target ${financeRupiah(goal.targetMinor)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (remaining == 0L) "Target tercapai" else "Kurang ${financeRupiah(remaining)}",
                    color = if (remaining == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            goal.targetDate?.let {
                Text("Tanggal target: ${financeDate(it)}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onContribute, modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.Icon(Icons.Filled.ArrowUpward, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Setor")
                }
                OutlinedButton(onClick = onWithdraw, modifier = Modifier.weight(1f), enabled = goal.savedMinor > 0L) {
                    androidx.compose.material3.Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tarik")
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressCardAdvanced(budget: BudgetOverviewRow) {
    val ratio = if (budget.limitMinor > 0L) budget.spentMinor.toDouble() / budget.limitMinor.toDouble() else 0.0
    val remaining = budget.limitMinor - budget.spentMinor
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(budget.name, fontWeight = FontWeight.Bold)
                    Text(
                        "${financeDate(budget.periodStart)} – ${financeDate(budget.periodEnd)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (ratio >= 1.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
            Text(financeRupiah(budget.spentMinor), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Terpakai dari ${financeRupiah(budget.limitMinor)}", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (ratio >= 1.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (remaining >= 0L) "Sisa ${financeRupiah(remaining)}" else "Melebihi batas ${financeRupiah(-remaining)}",
                    color = if (remaining >= 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Text("${(ratio * 100).toInt()}% terpakai", style = MaterialTheme.typography.labelSmall,
                    color = if (ratio >= 1.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WealthScreen(
    accounts: List<AccountBalanceRow>,
    goals: List<GoalOverviewRow>,
    assets: List<AssetEntity>,
    receivables: List<ReceivableOverviewRow>,
    liabilities: List<LiabilityOverviewRow>,
    onAddAccount: () -> Unit,
    onSetAccountMinimumBalance: (AccountBalanceRow) -> Unit,
    onAddAsset: () -> Unit,
    onAddReceivable: () -> Unit,
    onCollectReceivable: (ReceivableOverviewRow) -> Unit,
    onAddLiability: () -> Unit,
    onPayLiability: (LiabilityOverviewRow) -> Unit,
    onUpdateAsset: (AssetEntity) -> Unit
) {
    var section by remember { mutableStateOf("ACCOUNTS") }
    val accountTotal = accounts.sumOf { it.balanceMinor }
    val goalTotal = goals.sumOf { it.savedMinor }
    val assetTotal = assets.sumOf { it.currentValueMinor }
    val receivableTotal = receivables.sumOf { it.outstandingMinor }
    val debtTotal = liabilities.sumOf { it.outstandingMinor }
    val netWorth = accountTotal + goalTotal + assetTotal + receivableTotal - debtTotal

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF3F7F4)).verticalScroll(rememberScrollState())
            .padding(horizontal = 17.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        DuitPageHeader(
            title = "Aset dan kewajiban",
            subtitle = "Peta kekayaan dan seluruh komitmen keuangan"
        )
        DuitFeatureBanner(
            kicker = "KEKAYAAN BERSIH TERDATA",
            value = financeRupiah(netWorth),
            supporting = "Saldo akun + dana target + aset non-tunai + piutang − sisa utang"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DuitMetricCard("Saldo akun", financeRupiah(accountTotal), Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                DuitMetricCard("Dana target", financeRupiah(goalTotal), Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DuitMetricCard("Aset + piutang", financeRupiah(assetTotal + receivableTotal), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                DuitMetricCard("Sisa utang", financeRupiah(debtTotal), Modifier.weight(1f), MaterialTheme.colorScheme.error)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(selected = section == "ACCOUNTS", onClick = { section = "ACCOUNTS" }, label = { Text("Akun (${accounts.size})") })
            FilterChip(selected = section == "ASSETS", onClick = { section = "ASSETS" }, label = { Text("Aset (${assets.size})") })
            FilterChip(selected = section == "RECEIVABLES", onClick = { section = "RECEIVABLES" }, label = { Text("Piutang (${receivables.size})") })
            FilterChip(selected = section == "LIABILITIES", onClick = { section = "LIABILITIES" }, label = { Text("Utang (${liabilities.size})") })
        }

        when (section) {
            "ACCOUNTS" -> {
                DuitSectionHeader(
                    title = "Dompet dan rekening",
                    subtitle = "Saldo berasal dari saldo awal dan transaksi tersimpan",
                    trailing = { Button(onClick = onAddAccount) { Text("Tambah") } }
                )
                if (accounts.isEmpty()) {
                    DuitEmptyState(
                        title = "Belum ada akun",
                        message = "Tambahkan dompet tunai, rekening bank, tabungan, atau e-wallet.",
                        icon = Icons.Filled.AccountBalanceWallet,
                        action = { Button(onClick = onAddAccount) { Text("Tambah akun") } }
                    )
                }
                accounts.forEach { account ->
                    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(account.name, fontWeight = FontWeight.SemiBold)
                                Text(account.type.replace('_', ' '), style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (account.minimumBalanceMinor > 0L) "Batas minimum: ${financeRupiah(account.minimumBalanceMinor)}"
                                    else "Peringatan saldo minimum tidak aktif",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (account.minimumBalanceMinor > 0L && account.balanceMinor < account.minimumBalanceMinor) {
                                    Text("Saldo di bawah batas minimum", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(financeRupiah(account.balanceMinor), fontWeight = FontWeight.Bold)
                                TextButton(onClick = { onSetAccountMinimumBalance(account) }) { Text("Atur batas") }
                            }
                        }
                    }
                }
            }
            "ASSETS" -> {
                DuitSectionHeader(
                    title = "Aset non-tunai",
                    subtitle = "Kendaraan, properti, tanah, emas, elektronik, dan peralatan",
                    trailing = { Button(onClick = onAddAsset) { Text("Tambah") } }
                )
                if (assets.isEmpty()) {
                    DuitEmptyState(
                        title = "Belum ada aset non-tunai",
                        message = "Catat barang bernilai yang dimiliki. Jangan masukkan saldo rekening/dompet lagi karena sudah dihitung sebagai akun.",
                        icon = Icons.Filled.HomeWork,
                        action = { Button(onClick = onAddAsset) { Text("Catat aset") } }
                    )
                }
                assets.forEach { asset ->
                    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(asset.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text(financeRupiah(asset.currentValueMinor), fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "${asset.category.replace('_', ' ')} • Harga perolehan ${financeRupiah(asset.acquisitionCostMinor)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Nilai diperbarui: ${financeDate(asset.updatedAt)}",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = { onUpdateAsset(asset) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Perbarui nilai aset")
                            }
                        }
                    }
                }
            }
            "RECEIVABLES" -> {
                DuitSectionHeader(
                    title = "Piutang dan tagihan",
                    subtitle = "Saldo akun bertambah hanya saat pembayaran diterima",
                    trailing = { Button(onClick = onAddReceivable) { Text("Tambah") } }
                )
                if (receivables.isEmpty()) {
                    DuitEmptyState(
                        title = "Belum ada piutang",
                        message = "Catat tagihan pelanggan atau uang yang harus diterima.",
                        icon = Icons.Filled.TrendingUp,
                        action = { Button(onClick = onAddReceivable) { Text("Tambah piutang") } }
                    )
                }
                receivables.forEach { receivable ->
                    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(receivable.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text(financeRupiah(receivable.outstandingMinor), fontWeight = FontWeight.Bold,
                                    color = if (receivable.outstandingMinor == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                            }
                            Text("Pihak: ${receivable.counterparty} • Nilai awal ${financeRupiah(receivable.originalAmountMinor)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            receivable.dueAt?.let {
                                Text("Jatuh tempo: ${financeDate(it)}", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (receivable.outstandingMinor == 0L) {
                                Text("Lunas", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            } else {
                                OutlinedButton(onClick = { onCollectReceivable(receivable) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Catat penerimaan")
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                DuitSectionHeader(
                    title = "Utang dan kewajiban",
                    subtitle = "Pembayaran pokok mengurangi saldo akun dan sisa kewajiban",
                    trailing = { Button(onClick = onAddLiability) { Text("Tambah") } }
                )
                if (liabilities.isEmpty()) {
                    DuitEmptyState(
                        title = "Tidak ada utang tercatat",
                        message = "Catat pinjaman atau kewajiban yang masih harus dibayar.",
                        icon = Icons.Filled.Assessment,
                        action = { Button(onClick = onAddLiability) { Text("Tambah utang") } }
                    )
                }
                liabilities.forEach { debt ->
                    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(debt.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text(financeRupiah(debt.outstandingMinor), fontWeight = FontWeight.Bold,
                                    color = if (debt.outstandingMinor == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                            Text("Pemberi pinjaman: ${debt.counterparty} • Nilai awal ${financeRupiah(debt.originalAmountMinor)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            debt.dueAt?.let {
                                Text("Jatuh tempo: ${financeDate(it)}", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (debt.outstandingMinor == 0L) {
                                Text("Lunas", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            } else {
                                OutlinedButton(onClick = { onPayLiability(debt) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Catat pembayaran")
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun EmptyFinanceCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SavingsGoalDialog(onDismiss: () -> Unit, onSave: (String, Long, Long?, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var targetDateText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(3) }
    val parsedTargetDate = parseFinanceDate(targetDateText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat target tabungan") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    name, { name = it.take(80) },
                    label = { Text("Nama target") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    amount, { amount = it.filter(Char::isDigit) },
                    label = { Text("Target (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetDateText,
                    onValueChange = { targetDateText = it.take(10) },
                    label = { Text("Tanggal target (dd-MM-yyyy, opsional)") },
                    supportingText = {
                        if (targetDateText.isNotBlank() && parsedTargetDate == null) {
                            Text("Masukkan tanggal kalender yang valid, format dd-MM-yyyy.")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Prioritas", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    (1..5).forEach { value ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value.toString()) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && amount.toLongOrNull()?.let { it > 0L } == true &&
                    (targetDateText.isBlank() || parsedTargetDate != null),
                onClick = { onSave(name, amount.toLong(), parsedTargetDate, priority) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun GoalMovementDialog(
    goals: List<GoalOverviewRow>,
    accounts: List<AccountBalanceRow>,
    initialGoalId: Long,
    isWithdrawal: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Long, String) -> Unit
) {
    var goalId by remember(initialGoalId) { mutableLongStateOf(initialGoalId) }
    var accountId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isWithdrawal) "Tarik dana target" else "Setor dana target") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FinanceIdPicker("Target", goalId, goals.map { it.id to it.name }, { goalId = it })
                FinanceIdPicker("Akun", accountId, accounts.map { it.id to it.name }, { accountId = it })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Nominal (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
                Text("Setoran mengurangi saldo akun dan menambah dana target. Penarikan melakukan kebalikannya.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = goalId != 0L && accountId != 0L && amount.toLongOrNull()?.let { it > 0L } == true,
                onClick = { onSave(goalId, accountId, amount.toLong(), note) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun AssetDialog(onDismiss: () -> Unit, onSave: (String, String, Long, Long, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("VEHICLE") }
    var cost by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat aset non-tunai") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama aset") }, modifier = Modifier.fillMaxWidth())
                FinanceChoicePicker("Kategori", category, listOf(
                    "VEHICLE" to "Kendaraan", "PROPERTY" to "Properti", "LAND" to "Tanah",
                    "GOLD" to "Emas", "ELECTRONIC" to "Elektronik", "EQUIPMENT" to "Peralatan", "OTHER" to "Lainnya"
                ), { category = it })
                OutlinedTextField(cost, { cost = it.filter(Char::isDigit) }, label = { Text("Harga perolehan (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Nilai saat ini (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
                Text("Aset di sini untuk barang non-tunai. Saldo rekening dan dompet sudah dihitung dari menu akun, jangan dicatat ulang.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && cost.toLongOrNull() != null && value.toLongOrNull() != null,
                onClick = { onSave(name, category, cost.toLong(), value.toLong(), notes) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun AssetValueDialog(
    asset: AssetEntity,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var value by remember(asset.id) { mutableStateOf(asset.currentValueMinor.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Perbarui nilai aset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(asset.name, fontWeight = FontWeight.SemiBold)
                Text("Harga perolehan: ${financeRupiah(asset.acquisitionCostMinor)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value, { value = it.filter(Char::isDigit) },
                    label = { Text("Nilai saat ini (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = value.toLongOrNull() != null, onClick = { onSave(value.toLong()) }) { Text("Simpan nilai") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun LiabilityDialog(
    accounts: List<AccountBalanceRow>,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long?, Long?, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableLongStateOf(0L) }
    var dueDateText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val parsedDueDate = parseFinanceDate(dueDateText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat utang atau pinjaman") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama kewajiban") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(counterparty, { counterparty = it }, label = { Text("Pemberi pinjaman") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Nilai awal (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dueDateText, { dueDateText = it }, label = { Text("Jatuh tempo (dd-MM-yyyy, opsional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    supportingText = { if (dueDateText.isNotBlank() && parsedDueDate == null) Text("Gunakan format tanggal dd-MM-yyyy.") })
                FinanceIdPicker("Masukkan dana pinjaman ke akun (opsional)", accountId,
                    listOf(0L to "Tidak, dana sudah ada / tidak dicairkan") + accounts.map { it.id to it.name },
                    { accountId = it })
                OutlinedTextField(notes, { notes = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
                Text("Pilih akun penerima hanya bila pinjaman baru dicairkan dan saldonya belum tercatat. Ini mencegah saldo dihitung dua kali.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && counterparty.isNotBlank() &&
                amount.toLongOrNull()?.let { it > 0L } == true && (dueDateText.isBlank() || parsedDueDate != null),
                onClick = { onSave(name, counterparty, amount.toLong(), parsedDueDate, accountId.takeIf { it != 0L }, notes) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun LiabilityPaymentDialog(
    liability: LiabilityOverviewRow,
    accounts: List<AccountBalanceRow>,
    onDismiss: () -> Unit,
    onSave: (Long, Long, String) -> Unit
) {
    var accountId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bayar ${liability.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Sisa utang: ${financeRupiah(liability.outstandingMinor)}", fontWeight = FontWeight.SemiBold)
                FinanceIdPicker("Bayar dari akun", accountId, accounts.map { it.id to it.name }, { accountId = it })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Jumlah pembayaran (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = accountId != 0L && amount.toLongOrNull()?.let { it > 0L && it <= liability.outstandingMinor } == true,
                onClick = { onSave(accountId, amount.toLong(), note) }) { Text("Catat pembayaran") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun ReceivableDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long?, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val parsedDueDate = parseFinanceDate(dueDateText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat piutang atau tagihan") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama tagihan") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(counterparty, { counterparty = it }, label = { Text("Pelanggan / pihak yang berutang") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Nilai piutang (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dueDateText, { dueDateText = it }, label = { Text("Jatuh tempo (dd-MM-yyyy, opsional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    supportingText = { if (dueDateText.isNotBlank() && parsedDueDate == null) Text("Gunakan format tanggal dd-MM-yyyy.") })
                OutlinedTextField(notes, { notes = it }, label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
                Text("Mencatat piutang tidak menambah saldo rekening. Saldo baru bertambah saat pembayaran benar-benar diterima.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && counterparty.isNotBlank() &&
                    amount.toLongOrNull()?.let { it > 0L } == true &&
                    (dueDateText.isBlank() || parsedDueDate != null),
                onClick = { onSave(name, counterparty, amount.toLong(), parsedDueDate, notes) }
            ) { Text("Simpan piutang") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun ReceivableCollectionDialog(
    receivable: ReceivableOverviewRow,
    accounts: List<AccountBalanceRow>,
    onDismiss: () -> Unit,
    onSave: (Long, Long, String) -> Unit
) {
    var accountId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var amount by remember(receivable.id) { mutableStateOf(receivable.outstandingMinor.toString()) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terima pembayaran piutang") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(receivable.name, fontWeight = FontWeight.SemiBold)
                Text("Pihak: ${receivable.counterparty}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sisa tagihan: ${financeRupiah(receivable.outstandingMinor)}", fontWeight = FontWeight.SemiBold)
                FinanceIdPicker("Masuk ke akun", accountId, accounts.map { it.id to it.name }, { accountId = it })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) },
                    label = { Text("Nominal diterima (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                enabled = accountId != 0L &&
                    amount.toLongOrNull()?.let { it > 0L && it <= receivable.outstandingMinor } == true,
                onClick = { onSave(accountId, amount.toLong(), note) }
            ) { Text("Catat penerimaan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun FinanceIdPicker(label: String, selected: Long, options: List<Pair<Long, String>>, onSelected: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = options.firstOrNull { it.first == selected }?.second ?: "Pilih"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $current", modifier = Modifier.weight(1f))
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(item.second) }, onClick = {
                    onSelected(item.first)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun FinanceChoicePicker(label: String, selected: String, options: List<Pair<String, String>>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = options.firstOrNull { it.first == selected }?.second ?: "Pilih"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $current", modifier = Modifier.weight(1f))
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(item.second) }, onClick = {
                    onSelected(item.first)
                    expanded = false
                })
            }
        }
    }
}

private fun parseFinanceDate(input: String): Long? {
    val value = input.trim()
    if (!Regex("""\d{2}-\d{2}-\d{4}""").matches(value)) return null
    val formatter = java.text.SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")).apply {
        isLenient = false
    }
    val position = java.text.ParsePosition(0)
    val parsed = formatter.parse(value, position) ?: return null
    return parsed.time.takeIf { position.index == value.length && position.errorIndex < 0 }
}

private fun financeDate(value: Long): String =
    java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(java.util.Date(value))

@Composable
private fun financeRupiah(value: Long): String {
    if (!LocalDuitBalancesVisible.current) return "••••••"
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(value)
}
