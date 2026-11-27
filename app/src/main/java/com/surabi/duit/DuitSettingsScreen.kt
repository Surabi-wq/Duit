package com.surabi.duit

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class DuitSettingGroup(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val detail: String
)

@Composable
internal fun DuitSettingsScreen(
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    balancesVisible: Boolean,
    onBalancesVisibleChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("duit-ui-preferences", Context.MODE_PRIVATE) }
    var query by remember { mutableStateOf("") }
    var expandedGroup by remember { mutableStateOf("Tampilan & personalisasi") }
    var compactMode by remember { mutableStateOf(prefs.getBoolean("compact_mode", true)) }
    var onboarding by remember { mutableStateOf(prefs.getBoolean("guided_onboarding", true)) }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("notification_sound", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("notification_vibration", true)) }
    var language by remember { mutableStateOf(prefs.getString("language", "Sistem") ?: "Sistem") }
    var languageMenu by remember { mutableStateOf(false) }

    val groups = remember {
        listOf(
            DuitSettingGroup("Profil & workspace", "Kelola identitas dan ruang keuangan aktif", Icons.Filled.Person,
                "Profil dan workspace dikelola melalui pemilih workspace pada bagian atas aplikasi. Setiap workspace menyimpan data keuangan secara terpisah."),
            DuitSettingGroup("Tampilan & personalisasi", "Tema, warna, kepadatan, dan preferensi layar", Icons.Filled.ColorLens,
                "Tema dapat mengikuti sistem atau dipilih manual. Preferensi tampilan disimpan secara lokal pada perangkat ini."),
            DuitSettingGroup("Bahasa, wilayah & mata uang", "Bahasa aplikasi dan format regional", Icons.Filled.Language,
                "Mata uang dasar aplikasi saat ini adalah Rupiah (IDR). Pemilihan bahasa di bawah ini menyimpan preferensi pengguna; terjemahan seluruh layar belum diaktifkan."),
            DuitSettingGroup("Preferensi keuangan", "Kebiasaan pencatatan dan tampilan nominal", Icons.Filled.CurrencyExchange,
                "Pencatatan transaksi, akun, anggaran, dan aset tetap tersedia melalui navigasi utama. Penyembunyian nominal dapat diaktifkan pada bagian Privasi."),
            DuitSettingGroup("Notifikasi", "Preferensi suara dan getaran", Icons.Filled.Notifications,
                "Kontrol suara dan getaran berikut menyimpan preferensi lokal. Pengiriman push dan penjadwalan notifikasi memerlukan implementasi layanan notifikasi terpisah."),
            DuitSettingGroup("Backup, impor & ekspor", "Lindungi dan pindahkan data keuangan", Icons.Filled.Backup,
                "Pembuatan dan pemulihan file backup tersedia di Pusat Bantuan. Pastikan file backup disimpan di lokasi aman."),
            DuitSettingGroup("Sinkronisasi & perangkat", "Status sinkronisasi dan perangkat terhubung", Icons.Filled.Sync,
                "Penyimpanan data inti saat ini berjalan secara lokal/offline. Sinkronisasi cloud atau perangkat belum dikonfigurasi."),
            DuitSettingGroup("Privasi & keamanan", "Kontrol privasi tampilan data", Icons.Filled.PrivacyTip,
                "Penyamaran nominal hanya menyembunyikan angka pada antarmuka. Ini bukan pengunci aplikasi dan bukan enkripsi database."),
            DuitSettingGroup("Dashboard & widget", "Atur ringkasan keuangan", Icons.Filled.Dashboard,
                "Pengaturan widget dashboard tersedia dari ikon pengaturan pada halaman Ringkas."),
            DuitSettingGroup("Penyimpanan & performa", "Informasi penyimpanan aplikasi", Icons.Filled.Storage,
                "Data keuangan disimpan di database lokal. Jangan hapus data aplikasi sebelum membuat backup."),
            DuitSettingGroup("Aksesibilitas", "Kenyamanan dan keterbacaan", Icons.Filled.AccessibilityNew,
                "Gunakan pengaturan ukuran teks perangkat dan pembaca layar Android. Dukungan kontras dan kontrol aksesibilitas khusus akan ditingkatkan bertahap."),
            DuitSettingGroup("Pusat bantuan & tentang", "Panduan, informasi versi, dan dukungan", Icons.Filled.HelpOutline,
                "Panduan penggunaan dan informasi fitur tersedia pada tab Panduan."),
            DuitSettingGroup("Pengaturan lanjutan", "Informasi teknis dan perilaku aplikasi", Icons.Filled.Settings,
                "Duit mengutamakan operasi offline dan tidak meminta izin sensitif yang tidak diperlukan untuk fungsi inti.")
        )
    }
    val filtered = groups.filter {
        query.isBlank() || it.title.contains(query, true) || it.description.contains(query, true) || it.detail.contains(query, true)
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("PENGATURAN DUIT", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Text("Pusat pengaturan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Personalisasi aplikasi dan kelola preferensi penggunaan.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Cari pengaturan…") }
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("DUIT • PERSONAL FINANCE", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text("Atur pengalaman keuangan Anda", style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text("Preferensi disimpan di perangkat ini kecuali dinyatakan lain.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            items(filtered, key = { it.title }) { group ->
                val expanded = expandedGroup == group.title
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(
                            Modifier.fillMaxWidth().clickable { expandedGroup = if (expanded) "" else group.title },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                                Icon(group.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(10.dp).size(21.dp))
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(group.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(group.description, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(if (expanded) "−" else "+", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        if (expanded) {
                            Text(group.detail, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            when (group.title) {
                                "Tampilan & personalisasi" -> {
                                    Text("Mode tema", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("system" to "Sistem", "light" to "Terang", "dark" to "Gelap").forEach { (value, label) ->
                                            TextButton(onClick = { onThemeModeChange(value) }) {
                                                Text(if (themeMode == value) "✓ $label" else label)
                                            }
                                        }
                                    }
                                    SettingSwitch("Kepadatan compact", compactMode) {
                                        compactMode = it; prefs.edit().putBoolean("compact_mode", it).apply()
                                    }
                                }
                                "Bahasa, wilayah & mata uang" -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Bahasa", modifier = Modifier.weight(1f))
                                        TextButton(onClick = { languageMenu = true }) { Text(language) }
                                        DropdownMenu(expanded = languageMenu, onDismissRequest = { languageMenu = false }) {
                                            listOf("Sistem", "Indonesia", "English").forEach { option ->
                                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                                    language = option; prefs.edit().putString("language", option).apply()
                                                    languageMenu = false
                                                })
                                            }
                                        }
                                    }
                                    Text("Mata uang dasar: IDR (Rupiah)", style = MaterialTheme.typography.bodySmall)
                                }
                                "Notifikasi" -> {
                                    SettingSwitch("Suara notifikasi", soundEnabled) {
                                        soundEnabled = it; prefs.edit().putBoolean("notification_sound", it).apply()
                                    }
                                    SettingSwitch("Getaran notifikasi", vibrationEnabled) {
                                        vibrationEnabled = it; prefs.edit().putBoolean("notification_vibration", it).apply()
                                    }
                                }
                                "Privasi & keamanan" -> SettingSwitch("Tampilkan nominal saldo", balancesVisible, onBalancesVisibleChange)
                                "Pusat bantuan & tentang" -> {
                                    Text("Versi aplikasi: 1.8.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    SettingSwitch("Panduan awal interaktif", onboarding) {
                                        onboarding = it; prefs.edit().putBoolean("guided_onboarding", it).apply()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (filtered.isEmpty()) item {
                Text("Tidak ada pengaturan yang cocok dengan pencarian.", modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { Spacer(Modifier.padding(bottom = 12.dp)) }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
