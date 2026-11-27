package com.surabi.duit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import com.surabi.duit.data.CategoryEntity
import com.surabi.duit.data.RecurringSchedulePolicy
import com.surabi.duit.data.RecurringTemplateEntity
import com.surabi.duit.ui.LocalDuitBalancesVisible
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun RecurringTemplateManagerDialog(
    templates: List<RecurringTemplateEntity>,
    accounts: List<AccountBalanceRow>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, Long, String, Long, Long?, Long?, String, String, Int, Long) -> Unit,
    onRecord: (Long) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    val accountNames = remember(accounts) { accounts.associate { it.id to it.name } }
    val categoryNames = remember(categories) { categories.associate { it.id to it.name } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Jadwal transaksi")
                Text("${templates.count { it.isActive }} aktif • tersimpan lokal per workspace",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showCreate = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = accounts.isNotEmpty()
                ) { Text("＋  Buat jadwal baru") }
                if (accounts.isEmpty()) {
                    DuitEmptyState(
                        title = "Belum ada akun",
                        message = "Tambahkan dompet atau rekening sebelum membuat jadwal transaksi."
                    )
                } else if (templates.isEmpty()) {
                    DuitEmptyState(
                        title = "Belum ada jadwal",
                        message = "Buat template tagihan, gaji, sewa, cicilan, atau transfer rutin. Anda tetap meninjau dan mencatat setiap transaksi ketika jatuh tempo."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 390.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(templates, key = { it.id }) { template ->
                            val due = template.nextDueAt <= System.currentTimeMillis()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (template.isActive) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(13.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(template.title, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                when (template.type) {
                                                    "INCOME" -> "Pemasukan"
                                                    "EXPENSE" -> "Pengeluaran"
                                                    else -> "Transfer"
                                                } + " • " + RecurringSchedulePolicy.cadenceLabel(template.cadence, template.intervalCount),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            recurringMoney(template.amountMinor),
                                            fontWeight = FontWeight.Bold,
                                            color = when (template.type) {
                                                "INCOME" -> MaterialTheme.colorScheme.primary
                                                "EXPENSE" -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.secondary
                                            }
                                        )
                                    }
                                    Text(
                                        "Jatuh tempo: ${recurringDate(template.nextDueAt)}" +
                                            if (!template.isActive) " • Dijeda"
                                            else if (due) " • Perlu dicatat" else " • Akan datang",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (template.isActive && due) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        buildString {
                                            append("Dari: ")
                                            append(accountNames[template.fromAccountId] ?: "Akun tidak tersedia")
                                            template.toAccountId?.let {
                                                append(" → ")
                                                append(accountNames[it] ?: "Tujuan tidak tersedia")
                                            }
                                            template.categoryId?.let { id ->
                                                categoryNames[id]?.let { name -> append(" • "); append(name) }
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(onClick = { onToggleActive(template.id, !template.isActive) }) {
                                            Text(if (template.isActive) "Jeda" else "Lanjutkan")
                                        }
                                        Spacer(Modifier.weight(1f))
                                        Button(
                                            enabled = template.isActive && due,
                                            onClick = { onRecord(template.id) }
                                        ) { Text("Catat jatuh tempo") }
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    "Pencatatan membuat transaksi aktual dan memperbarui jadwal secara atomik. Jadwal tidak memposting transaksi otomatis di background.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai") } }
    )

    if (showCreate) {
        RecurringTemplateDialog(
            accounts = accounts,
            categories = categories,
            onDismiss = { showCreate = false },
            onSave = { type, amount, title, fromId, toId, categoryId, note, cadence, interval, firstDue ->
                onCreate(type, amount, title, fromId, toId, categoryId, note, cadence, interval, firstDue)
                showCreate = false
            }
        )
    }
}

@Composable
private fun RecurringTemplateDialog(
    accounts: List<AccountBalanceRow>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, Long, Long?, Long?, String, String, Int, Long) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var fromId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var toId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull { it.id != fromId }?.id ?: 0L) }
    var categoryId by remember(categories, type) {
        mutableLongStateOf(categories.firstOrNull { it.kind == type }?.id ?: 0L)
    }
    var cadence by remember { mutableStateOf("MONTHLY") }
    var intervalText by remember { mutableStateOf("1") }
    var firstDueText by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID")).format(Date()))
    }
    val firstDueAt = remember(firstDueText) { RecurringSchedulePolicy.parseDate(firstDueText) }
    val interval = intervalText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat jadwal berulang") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 530.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = type == "EXPENSE", onClick = { type = "EXPENSE" }, label = { Text("Keluar") })
                    FilterChip(selected = type == "INCOME", onClick = { type = "INCOME" }, label = { Text("Masuk") })
                    FilterChip(selected = type == "TRANSFER", onClick = { type = "TRANSFER" }, label = { Text("Transfer") })
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(100) },
                    label = { Text("Nama jadwal / keterangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit).take(18) },
                    label = { Text("Nominal per kejadian (rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                RecurringLongDropdown(
                    label = "Akun sumber",
                    selectedId = fromId,
                    options = accounts.map { it.id to it.name }
                ) {
                    fromId = it
                    if (toId == it) toId = accounts.firstOrNull { account -> account.id != it }?.id ?: 0L
                }
                if (type == "TRANSFER") {
                    RecurringLongDropdown(
                        label = "Akun tujuan",
                        selectedId = toId,
                        options = accounts.filter { it.id != fromId }.map { it.id to it.name }
                    ) { toId = it }
                } else {
                    RecurringLongDropdown(
                        label = "Kategori",
                        selectedId = categoryId,
                        options = categories.filter { it.kind == type }.map { it.id to it.name }
                    ) { categoryId = it }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(500) },
                    label = { Text("Catatan (opsional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                RecurringChoiceDropdown(
                    label = "Frekuensi",
                    selected = cadence,
                    options = listOf(
                        "DAILY" to "Harian",
                        "WEEKLY" to "Mingguan",
                        "MONTHLY" to "Bulanan",
                        "YEARLY" to "Tahunan"
                    )
                ) { cadence = it }
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it.filter(Char::isDigit).take(2) },
                    label = { Text("Ulang setiap (1–36)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = firstDueText,
                    onValueChange = { firstDueText = it.take(10) },
                    label = { Text("Jatuh tempo pertama (dd-MM-yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Tanggal bulanan mengikuti tanggal acuan. Bila dimulai pada akhir bulan, jadwal tetap berada di akhir setiap bulan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = accounts.isNotEmpty() &&
                    amountText.toLongOrNull()?.let { it > 0L } == true &&
                    title.isNotBlank() &&
                    interval != null && interval in 1..36 &&
                    firstDueAt != null &&
                    (type != "TRANSFER" || (toId != 0L && toId != fromId)),
                onClick = {
                    onSave(
                        type,
                        amountText.toLong(),
                        title,
                        fromId,
                        if (type == "TRANSFER") toId else null,
                        if (type == "TRANSFER" || categoryId == 0L) null else categoryId,
                        note,
                        cadence,
                        interval!!,
                        firstDueAt!!
                    )
                }
            ) { Text("Simpan jadwal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun RecurringLongDropdown(
    label: String,
    selectedId: Long,
    options: List<Pair<Long, String>>,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.first == selectedId }?.second ?: "Pilih"
    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selected", modifier = Modifier.weight(1f), maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.second) }, onClick = {
                    onSelected(option.first)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun RecurringChoiceDropdown(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: "Pilih"
    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedLabel", modifier = Modifier.weight(1f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.second) }, onClick = {
                    onSelected(option.first)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun recurringMoney(value: Long): String {
    if (!LocalDuitBalancesVisible.current) return "••••••"
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(value)
}

private fun recurringDate(value: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(value))
