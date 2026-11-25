package com.surabi.duit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.surabi.duit.data.AccountBalanceRow
import com.surabi.duit.data.AssetEntity
import com.surabi.duit.data.BudgetOverviewRow
import com.surabi.duit.data.GoalOverviewRow
import com.surabi.duit.data.LiabilityOverviewRow
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
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perencanaan keuangan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = section == "GOALS", onClick = { section = "GOALS" }, label = { Text("Target tabungan") })
            FilterChip(selected = section == "BUDGETS", onClick = { section = "BUDGETS" }, label = { Text("Anggaran") })
        }
        if (section == "GOALS") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Target aktif", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Button(onClick = onAddGoal) { IconWithLabelAdd("Buat target") }
            }
            if (goals.isEmpty()) {
                EmptyFinanceCard(
                    title = "Belum ada target tabungan",
                    body = "Buat target dana darurat, pendidikan, perjalanan, atau kebutuhan lain. Setoran target akan memindahkan saldo dari akun ke dana target."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    goals.forEach { goal ->
                        GoalProgressCard(
                            goal = goal,
                            onContribute = { onMoveGoal(goal, false) },
                            onWithdraw = { onMoveGoal(goal, true) }
                        )
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Anggaran periode berjalan", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Button(onClick = onAddBudget) { IconWithLabelAdd("Buat") }
            }
            if (budgets.isEmpty()) {
                EmptyFinanceCard("Belum ada anggaran", "Buat batas pengeluaran untuk memantau realisasi dan sisa dana.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    budgets.forEach { budget -> BudgetProgressCardAdvanced(budget) }
                }
            }
        }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(Icons.Filled.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(goal.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("Prioritas ${goal.priority}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(financeRupiah(goal.savedMinor), fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Target ${financeRupiah(goal.targetMinor)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (remaining == 0L) "Target tercapai" else "Kurang ${financeRupiah(remaining)}",
                    color = if (remaining == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.weight(1f))
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(budget.name, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(financeRupiah(budget.spentMinor), fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("dari ${financeRupiah(budget.limitMinor)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = if (ratio >= 1.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                if (remaining >= 0L) "Sisa ${financeRupiah(remaining)}" else "Melebihi batas ${financeRupiah(-remaining)}",
                color = if (remaining >= 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun WealthScreen(
    accounts: List<AccountBalanceRow>,
    goals: List<GoalOverviewRow>,
    assets: List<AssetEntity>,
    liabilities: List<LiabilityOverviewRow>,
    onAddAccount: () -> Unit,
    onAddAsset: () -> Unit,
    onAddLiability: () -> Unit,
    onPayLiability: (LiabilityOverviewRow) -> Unit,
    onUpdateAsset: (AssetEntity) -> Unit
) {
    val accountsTotal = accounts.sumOf { it.balanceMinor }
    val reservedGoals = goals.sumOf { it.savedMinor }
    val assetsTotal = assets.sumOf { it.currentValueMinor }
    val debtTotal = liabilities.sumOf { it.outstandingMinor }
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aset dan kewajiban", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Ringkasan kekayaan terdata", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(financeRupiah(accountsTotal + reservedGoals + assetsTotal - debtTotal), style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Saldo akun + dana target + nilai aset non-tunai − sisa utang", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Dompet dan rekening", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Button(onClick = onAddAccount) { Text("Tambah") }
        }
        if (accounts.isEmpty()) EmptyFinanceCard("Belum ada akun", "Tambahkan dompet atau rekening untuk mencatat saldo.")
        accounts.forEach { account ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(account.name, fontWeight = FontWeight.SemiBold)
                        Text(account.type.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(financeRupiah(account.balanceMinor), fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(Icons.Filled.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Aset non-tunai", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Button(onClick = onAddAsset) { Text("Tambah") }
        }
        if (assets.isEmpty()) EmptyFinanceCard("Belum ada aset", "Catat kendaraan, properti, emas, elektronik, atau peralatan. Jangan masukkan lagi saldo bank/dompet yang sudah tercatat sebagai akun.")
        assets.forEach { asset ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(asset.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(financeRupiah(asset.currentValueMinor), fontWeight = FontWeight.Bold)
                    }
                    Text("${asset.category.replace('_', ' ')} • Harga perolehan ${financeRupiah(asset.acquisitionCostMinor)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(Icons.Filled.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Utang dan kewajiban", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Button(onClick = onAddLiability) { Text("Tambah") }
        }
        if (liabilities.isEmpty()) EmptyFinanceCard("Tidak ada utang tercatat", "Catat pinjaman atau kewajiban untuk menghitung kekayaan bersih.")
        liabilities.forEach { debt ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(debt.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(financeRupiah(debt.outstandingMinor), color = if (debt.outstandingMinor == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold)
                    }
                    Text("Kepada ${debt.counterparty} • Awal ${financeRupiah(debt.originalAmountMinor)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    debt.dueAt?.let { due ->
                        Text("Jatuh tempo: " + financeDate(due), style = MaterialTheme.typography.labelSmall,
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
fun SavingsGoalDialog(onDismiss: () -> Unit, onSave: (String, Long, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(3) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat target tabungan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama target") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Target (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Prioritas", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    (1..5).forEach { value ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value.toString()) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && amount.toLongOrNull()?.let { it > 0L } == true,
                onClick = { onSave(name, amount.toLong(), priority) }) { Text("Simpan") }
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
    if (input.isBlank()) return null
    return runCatching {
        java.text.SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")).apply {
            isLenient = false
        }.parse(input.trim())?.time
    }.getOrNull()
}

private fun financeDate(value: Long): String =
    java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(java.util.Date(value))

private fun financeRupiah(value: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(value)
}
