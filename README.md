# Duit — Pengelola Keuangan

Duit adalah aplikasi Android native untuk mengelola keuangan secara lokal dan offline. Identitas visual awal menggunakan palet hijau, putih, dan biru.

## Status pengembangan — versi 0.2.0

Repositori ini sebelumnya kosong. Proyek sedang dibangun dari dasar dan dikembangkan bertahap. Versi awal memprioritaskan fondasi data yang benar sebelum modul ERP yang lebih kompleks.

### Fungsi yang tersedia pada 0.2.0
- Dashboard per workspace dengan ringkasan saldo, pemasukan, dan pengeluaran periode berjalan.
- Workspace terpisah untuk ruang keuangan yang berbeda.
- Banyak akun/dompet di dalam workspace.
- Pencatatan pemasukan, pengeluaran, dan transfer antar-akun.
- Kategori pemasukan/pengeluaran bawaan.
- Daftar transaksi terbaru.
- Anggaran bulanan dan pemantauan realisasi.
- Penyimpanan lokal menggunakan Room di atas SQLite.
- Validasi jumlah transaksi dan kepemilikan akun terhadap workspace.
- Pencarian/filter transaksi terbaru serta pembatalan dengan alasan dan audit log.\n- Target tabungan, aset non-tunai, utang, pembayaran pokok, dan estimasi kekayaan bersih.\n- Migrasi Room v1→v2 tanpa destructive migration.\n- Build varian Release untuk pengujian distribusi APK.

### Belum dinyatakan selesai
Modul aset, utang-piutang, investasi, pajak, akuntansi double-entry, ERP, gudang, OCR, analisis prediktif, multi-user, persetujuan berjenjang, enkripsi backup, dan impor/ekspor dokumen merupakan tahapan lanjutan. Modul-modul tersebut tidak boleh dianggap aktif sebelum memiliki alur data, validasi, antarmuka, dan pengujian.

## Teknologi
- Kotlin dan Android SDK.
- Jetpack Compose + Material 3.
- Room / SQLite, foreign key, index, dan operasi database transaksional.
- Semua operasi inti berjalan tanpa koneksi internet.
- Mata uang awal: IDR. Nilai uang disimpan sebagai integer rupiah, bukan floating point.

## Menjalankan build
Gunakan Android Studio yang mendukung Android Gradle Plugin pada proyek ini, JDK 17, dan Android SDK Platform 36.

Perintah build: gradle :app:assembleDebug atau gradle :app:assembleRelease.

APK CI dibuat dari varian Release dan diberi nama Duit-0.2.0.apk. Sertifikat debug bawaan Gradle hanya untuk pengujian internal; sebelum distribusi komersial, buat signing key pribadi dan ganti konfigurasi signing release. Jangan membagikan keystore atau password ke repositori.

## Prinsip data
- Tidak ada saldo/transaksi keuangan contoh yang dimasukkan sebagai data pengguna.
- Saat pertama dijalankan aplikasi membuat workspace Pribadi, akun Dompet Utama dengan saldo awal Rp0, dan kategori bawaan.
- Transfer tidak dihitung sebagai pemasukan atau pengeluaran.
- Penghapusan workspace/akun tidak disediakan pada fondasi awal untuk mengurangi risiko kehilangan histori.
- Perubahan skema database di masa depan wajib memakai migration teruji. Jangan gunakan destructive migration untuk data pengguna.

## Roadmap ringkas
1. Fondasi ledger lokal: workspace, akun, transaksi, anggaran, dashboard, dan tes aturan nominal.
2. Aset, kewajiban, tujuan tabungan, dan rekonsiliasi.
3. Buku besar double-entry, chart of accounts, jurnal, dan laporan akuntansi.
4. Penjualan, pembelian, pelanggan, pemasok, persediaan, cabang, dan proyek.
5. Pajak, dokumen, impor/ekspor, analisis, OCR lokal, dan otomatisasi.
6. Keamanan, backup/restore terenkripsi, audit, performa, dan uji perangkat nyata.

Setiap tahap harus melewati build, unit test, pengujian database, pengujian alur UI, serta pemeriksaan integritas data sebelum dinyatakan selesai.
