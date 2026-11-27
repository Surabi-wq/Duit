# Audit dan penyempurnaan UI/UX Duit 1.2.0

Tanggal pemeriksaan: 26 November 2026  
Basis audit: branch `main`, commit `b3581b95c21c6e8517b24f86eff9cda7db8139c7`  
Cabang implementasi: `ux/duit-v1.2.0-complete-polish`

## Ringkasan hasil audit

Aplikasi sudah memiliki fondasi native Android berbasis Kotlin/Jetpack Compose dan Room/SQLite. Perubahan versi ini berfokus pada konsistensi tampilan, adaptasi lebar layar, dan pemahaman pengguna; database dan perhitungan keuangan tidak diganti.

| Area | Temuan awal | Tindakan pada 1.2.0 |
|---|---|---|
| Konten layar lebar | Kolom halaman utama menggunakan lebar penuh sehingga kartu dan baris informasi terlalu melebar pada tablet | Menambahkan `DuitResponsiveColumn` dengan lebar maksimum 760 dp dan posisi terpusat |
| Filter histori di ponsel | Dua input tanggal dan dua input nominal selalu berdampingan | Mengubah pasangan input menjadi susunan vertikal saat lebar konten di bawah 430 dp |
| Design system | Radius komponen umum memakai beberapa angka langsung meskipun token shape tersedia | Kartu metrik, hero, empty state, dan panel memakai shape token MaterialTheme |
| Navigasi | Dock sudah memiliki semantik tab, tetapi ukuran sentuh tidak dinyatakan sebagai batas minimum | Menetapkan tinggi minimum 48 dp pada item navigasi |
| Judul halaman | Judul header panjang berpotensi mengambil ruang berlebih di samping tombol | Membatasi dua baris dan menggunakan ellipsis |
| Panduan | Panduan dan FAQ tersedia, tetapi glosarium istilah inti belum terkelompok | Menambahkan tutorial awal, glosarium, serta FAQ tentang pembatalan/audit dan privasi |

## Komponen dan file yang disentuh

- `DuitComponents.kt`: komponen kolom responsif, shape token, dan header yang aman terhadap teks panjang.
- `DuitExperience.kt`: lebar dashboard untuk layar besar dan batas minimum target sentuh navigasi.
- `AdvancedFinanceScreens.kt`: lebar konten halaman Rencana dan Aset.
- `MainActivity.kt`: lebar halaman Transaksi/Panduan, filter adaptif ponsel, tutorial, glosarium, dan FAQ.
- `app/build.gradle.kts`: versi aplikasi menjadi 1.2.0 / versionCode 12.
- `.github/workflows/android.yml`: nama artefak rilis menjadi `Duit-1.2.0.apk`.
- `README.md`: catatan perubahan versi dan nama artefak.

## Bagian yang sengaja dipertahankan

- Room/SQLite tetap digunakan; tidak ada perubahan skema atau migrasi database.
- Nominal tetap disimpan sebagai integer Rupiah `Long`.
- Query saldo akun, pemasukan, pengeluaran, arus kas, target, piutang, utang, dan kekayaan bersih tidak diubah.
- Transfer tetap dikecualikan dari pendapatan dan biaya.
- Histori transaksi tetap memakai AndroidX Paging 3; dashboard hanya membaca jendela 20 transaksi terbaru.
- Ekspor CSV tetap menulis hasil filter secara bertahap.
- Pembatalan transaksi tetap mempertahankan audit trail, bukan menghapus catatan permanen.

## Batasan yang belum diselesaikan

1. Edit transaksi, duplikasi, impor spreadsheet, reversal jurnal, dan rekonsiliasi formal belum tersedia sebagai alur penuh.
2. Backup/restore database penuh, enkripsi database, dan penguncian aplikasi PIN/biometrik belum tersedia. Penyembunyian nominal hanya perlindungan visual.
3. Akuntansi double-entry, buku besar/laporan akuntansi, multi-user, ERP, serta persetujuan berjenjang masih merupakan pekerjaan lanjutan.
4. Pengujian pembaca layar, font sistem besar, rotasi, dan dataset ribuan transaksi pada perangkat Android fisik tidak dapat disimpulkan hanya dari unit test atau build CI.

## Gerbang verifikasi

- [ ] `:app:testDebugUnitTest` berhasil pada GitHub Actions.
- [ ] `:app:assembleRelease` berhasil pada GitHub Actions.
- [ ] Artefak bernama `Duit-1.2.0.apk` berhasil diunggah.
- [ ] SHA-256 artefak dicatat dari log CI.
- [ ] Pengujian perangkat fisik dan skenario upgrade database tetap dilakukan sebelum distribusi.
