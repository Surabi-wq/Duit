# Duit — Pengelola Keuangan Offline

Duit adalah aplikasi Android native untuk pencatatan dan perencanaan keuangan lokal. Identitas visual menggunakan hijau, putih, dan biru. Data inti disimpan secara lokal melalui Room/SQLite; aplikasi tidak memerlukan koneksi internet untuk alur yang tersedia.

## Versi source

- Versi: **0.4.0**
- Package: `com.surabi.duit`
- Minimum Android: API 23
- Compile/target SDK: 36
- Kotlin: 2.3.21
- Android Gradle Plugin: 8.13.2
- Penyimpanan: Room di atas SQLite
- Nilai uang: integer Rupiah (`Long`), tanpa floating-point

## Fitur yang sudah tersedia

### Workspace, akun, dan dashboard
- Workspace terpisah untuk Pribadi, Keluarga, Bisnis, Perusahaan, Proyek, dan Lainnya.
- Banyak akun per workspace: tunai, bank, tabungan, e-wallet, kartu kredit, investasi, dan lainnya.
- Saldo akun dihitung dari saldo awal serta transaksi; bukan disimpan sebagai angka yang dapat tertinggal dari histori.
- Dashboard menampilkan saldo akun, pemasukan, pengeluaran, arus kas bulan berjalan, anggaran, estimasi kekayaan bersih, serta peringatan saldo minimum, anggaran, arus kas negatif, dan jatuh tempo utang/piutang.

### Transaksi
- Pemasukan, pengeluaran, dan transfer antar-akun.
- Validasi akun dan kategori harus berasal dari workspace yang sama.
- Transfer mengurangi akun sumber dan menambah akun tujuan tanpa mengubah total pemasukan/pengeluaran.
- Pencarian/filter pada 200 transaksi terbaru di workspace berdasarkan keterangan, catatan, kategori, dan jenis.
- Pembatalan transaksi dengan alasan; histori tidak dihapus, transaksi ditandai void dan alasan masuk ke audit log.

### Anggaran dan target tabungan
- Anggaran bulanan umum atau berdasarkan kategori pengeluaran.
- Target tabungan dengan nominal tujuan dan prioritas.
- Setoran target mengurangi saldo akun yang dipilih; penarikan mengembalikan saldo akun.
- Dana target dihitung dari transaksi setoran/penarikan yang terkait, bukan input saldo manual yang tidak tersinkron.

### Aset dan kewajiban
- Pencatatan aset non-tunai: kendaraan, properti, tanah, emas, elektronik, peralatan, dan lainnya.
- Harga perolehan dan nilai saat ini; nilai aset dapat diperbarui dengan pencatatan audit.
- Pencatatan utang/pinjaman, pemberi pinjaman, nilai awal, dan tanggal jatuh tempo opsional.
- Pencairan pinjaman dapat menambah saldo akun ketika dana baru diterima dan belum tercatat.
- Pembayaran pokok utang mengurangi saldo akun dan sisa kewajiban.
- Estimasi kekayaan bersih dihitung dari saldo akun + dana target + nilai aset non-tunai − sisa utang.

## Aturan pencatatan untuk mencegah penghitungan ganda

- Jangan masukkan saldo rekening/dompet sebagai aset non-tunai; rekening dan dompet sudah masuk melalui modul akun.
- Saat mencatat utang yang dananya sudah pernah dimasukkan ke saldo akun, jangan pilih akun penerima lagi.
- Pencairan pinjaman bukan pemasukan; pembayaran pokok utang bukan seluruhnya pengeluaran.
- Setoran target bukan pengeluaran; penarikan target bukan pemasukan.
- Versi ini belum memiliki alur pembelian/penjualan aset yang otomatis membuat jurnal dan memindahkan uang. Bila aset baru dibeli setelah aplikasi digunakan, transaksi pembelian dan pengakuan aset belum terintegrasi penuh; jangan menganggap laporan kekayaan sudah menjadi buku besar akuntansi.

## Status fitur lanjutan

Modul berikut **belum tersedia sebagai fungsi lengkap** dan tidak boleh dianggap aktif hanya karena masuk daftar kebutuhan produk:
- Rekonsiliasi akun, penyesuaian saldo terkontrol, edit transaksi umum, transaksi berulang/terjadwal/massal, template, lampiran struk, impor/ekspor.
- Target dengan jadwal setoran otomatis dan notifikasi.
- Riwayat nilai aset terstruktur, penyusutan, pemeliharaan, pembelian/penjualan/penghapusan aset yang terhubung dengan jurnal.
- Piutang, bunga/denda terstruktur, cicilan otomatis, pengingat jatuh tempo.
- Chart of Accounts, jurnal double-entry, buku besar, buku pembantu, neraca saldo, neraca, laba rugi, laporan arus kas akuntansi, periode akuntansi.
- ERP multi-perusahaan/cabang/departemen/cost center/proyek, penjualan, pembelian, invoice, pelanggan, pemasok, produk, stok, gudang, dan HPP.
- Manajemen portofolio investasi lengkap, harga pasar otomatis, pajak, invoice/kwitansi profesional, arsip dokumen, ekspor PDF/Excel/CSV/DOCX.
- Analisis tren/prediksi, grafik lanjutan, OCR lokal, asisten bahasa sehari-hari.
- Multi-user, role/permission, approval berjenjang, PIN, biometrik, enkripsi database, backup/restore terenkripsi, checksum, dan enkripsi lampiran.
- Personalisasi lengkap, mode pemula/mahir, multi-mata uang, konsolidasi enterprise.

Lihat [docs/FEATURE-STATUS.md](docs/FEATURE-STATUS.md) untuk matriks implementasi dan gerbang penerimaan.

## Build

Memerlukan JDK 17, Android SDK Platform 36, Build Tools 36.0.0, dan Gradle 8.13.

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
gradle :app:assembleRelease
```

CI di GitHub Actions menjalankan unit test, build Release, dan mengunggah artefak bernama `Duit-0.4.0.apk`.

## Signing dan distribusi

Varian Release saat ini menggunakan konfigurasi signing debug untuk pengujian internal. APK ini dapat dipasang untuk pengujian, tetapi **belum ditandatangani dengan kunci rilis privat** untuk distribusi komersial/pembaruan jangka panjang. Sebelum distribusi, buat keystore rilis, simpan kredensialnya sebagai secret CI, dan konfigurasi signing release. Jangan pernah commit keystore atau password ke repository.

## Database dan kompatibilitas

Database Room saat ini berada pada versi 2 dengan migrasi eksplisit dari versi 1. Jangan gunakan destructive migration pada data pengguna. Sebelum penggunaan produksi, jalankan tes upgrade database dari APK versi sebelumnya pada perangkat uji dan siapkan backup/restore tervalidasi. Fitur backup/restore belum tersedia pada 0.4.0.

## Pengujian yang telah dilakukan

CI versi 0.4.0 telah lulus `testDebugUnitTest` dan `assembleRelease`. Pengujian instalasi/upgrade dan alur UI pada perangkat Android fisik belum dilakukan dalam CI. Keberhasilan build bukan bukti bahwa seluruh kebutuhan enterprise A–Z telah selesai.
