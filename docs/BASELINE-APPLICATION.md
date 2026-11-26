# Landasan Dasar Resmi Pengembangan Duit

Dokumen ini menetapkan **Duit 1.5.0** sebagai baseline resmi untuk seluruh pekerjaan pembuatan, pemeliharaan, perbaikan, dan pengembangan berikutnya pada aplikasi Duit.

Mulai sekarang, setiap perubahan aplikasi harus berangkat dari source code baseline ini, bukan dari mockup, source lama, atau implementasi terpisah yang menghilangkan kemampuan yang sudah ada.

## 1. Identitas baseline

| Properti | Nilai |
|---|---|
| Nama aplikasi | Duit – Manajemen Keuangan |
| Versi dasar | 1.5.0 |
| Android versionCode | 15 |
| Application ID | com.surabi.duit |
| Namespace | com.surabi.duit |
| Minimum Android | API 23 |
| Compile / Target SDK | 36 / 36 |
| Bahasa dan runtime | Kotlin, JVM 17 |
| UI | Jetpack Compose / Material 3 |
| Database lokal | Room di atas SQLite |
| Versi skema Room | 5 |
| Model operasi | Offline-first, data inti lokal |
| Varian distribusi pengembangan | Release APK |
| Nama file APK | Duit-<versi>-Release.apk atau artefak bernama Duit-<versi>.apk |

Baseline source ditetapkan pada commit c892e9f5d328da39c83a2dacaf068701d75b668b di branch main, yang memuat integrasi UI backup/restore dan dokumentasi versi 1.5.0. Jika branch main berkembang setelah penetapan ini, gunakan baseline dan riwayat commit ini sebagai titik asal, lalu lanjutkan dari HEAD terbaru yang masih merupakan turunan langsungnya.

## 2. Kemampuan yang wajib dipertahankan

Pengembangan berikutnya wajib mempertahankan kemampuan yang sudah terdapat pada baseline, kecuali pengguna secara eksplisit meminta perubahan/pergantian dan dampaknya telah dianalisis.

### Keuangan dan workspace
- Workspace untuk memisahkan data pribadi, keluarga, bisnis, perusahaan, atau proyek.
- Banyak akun/dompet per workspace, saldo awal, saldo minimum, dan pengarsipan akun.
- Kategori pemasukan dan pengeluaran.
- Transaksi pemasukan, pengeluaran, transfer, pergerakan target tabungan, pembayaran/pencairan kewajiban, dan penerimaan piutang sesuai dukungan source.
- Validasi workspace dan relasi akun/kategori sebelum transaksi disimpan.
- Pembatalan transaksi dengan alasan dan audit trail, bukan menghilangkan jejak historis.
- Duplikasi transaksi dasar tanpa memodifikasi transaksi sumber.
- Anggaran bulanan, target tabungan, aset, utang, dan piutang.
- Template transaksi berulang dengan jadwal dan kontrol aktif/nonaktif.

### Dashboard dan pengalaman pengguna
- Dashboard ringkasan periode, arus kas, akun, kekayaan bersih, rencana, peringatan, jatuh tempo, dan aktivitas.
- Navigasi utama lima bagian: Ringkas, Transaksi, Rencana, Aset, dan Panduan.
- Histori transaksi menggunakan Paging 3; filter, pencarian, detail, dan ekspor CSV.
- Ekspor CSV mengikuti filter dan ditulis bertahap; CSV bukan pengganti backup penuh.
- Pengaturan widget dashboard dan penyamaran nominal lokal.
- Tema, ikon, identitas aplikasi, serta layout responsif yang telah ada.

### Backup dan restore versi 1.5.0
- Backup database penuh dengan ekstensi .duitbackup.
- Cakupan 11 tabel: workspaces, accounts, categories, transactions, budgets, savings_goals, assets, liabilities, receivables, audit_logs, recurring_templates.
- Backup membuat snapshot konsisten dan mempertahankan nilai SQLite bertipe.
- Restore memeriksa manifest, versi database, struktur tabel/kolom, integritas arsip, dan foreign key.
- Restore meminta konfirmasi eksplisit dan mengganti data dalam satu transaksi Room; kegagalan harus membatalkan transaksi.
- Restore baseline ini mensyaratkan skema database kompatibel. Migrasi backup lintas versi skema belum disediakan.
- Arsip backup baseline belum dienkripsi dengan kata sandi. Jangan menggambarkannya sebagai backup terenkripsi.

## 3. Aturan wajib untuk setiap update

1. **Mulai dari baseline.** Baca source dan dokumentasi terkini pada repositori, pahami alur yang terdampak, kemudian ubah source yang benar-benar dipakai aplikasi.
2. **Jangan regresi.** Fitur, data, layar, query, validasi, serta perilaku yang sudah berfungsi tidak boleh hilang hanya karena penambahan fitur lain.
3. **Jaga integritas keuangan.** Jangan mengubah rumus saldo, transfer, arus kas, kekayaan bersih, anggaran, atau perlakuan transaksi tanpa analisis akuntansi, tes, dan penjelasan dampaknya.
4. **Jaga integritas data.** Jangan melakukan destructive migration, menghapus database pengguna, mengganti ID historis, atau mengubah relasi secara diam-diam.
5. **Migrasi Room harus eksplisit dan berurutan.** Setiap perubahan skema harus menambahkan migrasi dari versi sebelumnya, mempertahankan data lama, dan diuji terhadap skema asal yang relevan. Naikkan versi Room hanya bila memang diperlukan.
6. **Backup/restore adalah jalur keselamatan data.** Setiap update skema harus mengevaluasi kompatibilitas backup lama. Jangan merusak backup versi sebelumnya; bila dukungan lintas skema ditambahkan, implementasikan migrasi arsip secara eksplisit dan uji round-trip.
7. **Tidak ada fitur palsu.** Tombol, menu, status, atau indikator yang terlihat harus terhubung ke fungsi nyata, validasi nyata, dan penyimpanan yang benar. Jangan memakai data dummy untuk menyamarkan fungsi yang belum selesai.
8. **Offline-first.** Fitur keuangan inti harus tetap dapat dipakai tanpa koneksi internet kecuali ada kebutuhan online yang secara eksplisit dirancang.
9. **UI/UX konsisten.** Pertahankan identitas visual Duit, hierarchy yang jelas, layout responsif, state loading/kosong/error, keterbacaan, aksesibilitas, dan target sentuh yang memadai.
10. **Keamanan dan privasi.** Jangan menampilkan data sensitif di log atau pesan error. Jelaskan batas perlindungan yang benar; penyamaran nominal bukan enkripsi dan backup tanpa password bukan backup terenkripsi.
11. **Pengujian wajib.** Tambahkan/ubah unit test untuk policy, validasi, perhitungan, parsing, migrasi, dan skenario gagal yang terdampak. Jalankan :app:testDebugUnitTest dan :app:assembleRelease.
12. **Verifikasi artefak.** Pastikan Release APK berhasil dibangun, diberi nama produk yang benar (bukan APK debug), dan artefak yang diberikan berasal dari commit yang telah diverifikasi.
13. **Dokumentasi harus mengikuti source.** Perbarui README, status fitur, catatan versi, dan catatan migrasi. Tandai dengan jujur mana yang sudah terimplementasi, parsial, atau belum tersedia.
14. **Laporkan bukti.** Setiap update harus menyebut commit, file utama yang berubah, fitur yang benar-benar selesai, hasil tes/build, APK dan checksum bila tersedia, serta keterbatasan/pengujian yang belum dilakukan.

## 4. Prosedur perubahan database

Untuk setiap perubahan Room:
- Periksa seluruh entity, foreign key, index, query, DAO, repository, dan pemanggil UI yang terkait.
- Buat migrasi non-destruktif dan pastikan jalur migrasi dari versi skema yang didukung.
- Jangan menghapus atau menulis ulang tabel historis untuk memudahkan implementasi.
- Uji data setelah migrasi: jumlah baris, ID, relasi, nominal, waktu, status void, dan hasil query saldo.
- Perbarui format/reader backup bila struktur yang dicadangkan berubah.
- Pastikan restore yang tidak valid/terputus gagal secara atomik.

## 5. Definisi selesai

Suatu perubahan belum boleh dinyatakan selesai hanya karena source telah diubah atau commit telah dibuat. Status selesai membutuhkan:
- Implementasi nyata pada source aplikasi.
- Pemeriksaan alur utama dan kondisi error.
- Tes yang relevan lulus.
- Release APK berhasil dibangun dan penamaannya sesuai produk.
- Dokumentasi serta catatan keterbatasan diperbarui.
- Tidak ada klaim pengujian perangkat fisik jika pengujian tersebut tidak benar-benar dilakukan.

## 6. Urutan acuan

Prioritas sumber kebenaran:
1. Source code aktif pada branch kerja yang merupakan turunan baseline.
2. Skema Room, migrasi, unit test, dan kontrak repository.
3. Dokumen ini sebagai kebijakan baseline dan aturan non-regresi.
4. README dan dokumen status fitur sebagai ringkasan yang harus selalu diselaraskan dengan source.

**Keputusan dasar:** Duit 1.5.0 adalah titik awal resmi. Update selanjutnya bersifat inkremental dan kompatibel; jangan memulai ulang aplikasi atau membuang fungsi yang sudah bekerja.
