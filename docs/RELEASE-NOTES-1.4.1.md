# Duit 1.4.1 — Catatan Pembaruan

## Keandalan backup dan restore
- Menu Tambah menyediakan backup database lokal ke ZIP melalui Android Storage Access Framework.
- Restore memvalidasi daftar entri ZIP, manifest, ukuran, SHA-256, `PRAGMA integrity_check`, `PRAGMA foreign_key_check`, dan versi Room yang didukung sebelum database aktif diganti.
- Membuat salinan pengaman database aktif di direktori privat `noBackupFilesDir` sebelum pergantian. Jika tahap restore gagal, aplikasi mencoba rollback; bila rollback tidak berhasil, file pengaman dipertahankan dan lokasinya dilaporkan.
- Output dan input stream ditutup secara terkontrol. Pembacaan manifest dan database hasil ekstraksi dibatasi ukurannya.

## Perbaikan UI dan perhitungan
- Setelah restore sukses, `FinanceRepository` dan kolektor Flow UI diganti ke instance Room yang baru; pencarian transaksi berbasis Paging juga di-key pada repository.
- Kalkulator kekayaan bersih dipusatkan untuk dashboard dan halaman Aset; formula yang digunakan tidak berubah.
- Guard progres target mencegah pembagian dengan nol.
- Pusat bantuan, README, dan nama artefak build diperbarui.

## Kompatibilitas
- Database tetap Room versi 5; tidak ada migrasi database baru.
- Fitur duplikasi transaksi pada versi 1.4.0 dipertahankan.
- Tidak mengubah pencatatan pemasukan/pengeluaran, transfer, saldo, dan transaksi inti.

## Batasan
- Backup belum dienkripsi dan tidak mencakup file lampiran.
- Backup terjadwal, riwayat backup, restore selektif workspace, dan enkripsi database belum tersedia.
- Belum dilakukan pengujian install/restore pada perangkat Android fisik dalam proses ini.
- Fitur enterprise lain seperti akuntansi double-entry, ERP, persediaan, OCR, multi-user, dan role/permission tetap menjadi roadmap dan tidak diklaim selesai.

## Verifikasi
- GitHub Actions menjalankan `:app:testDebugUnitTest` dan `:app:assembleRelease`.
- APK release diberi nama produk `Duit-1.4.1.apk`; varian release saat ini masih menggunakan debug signing untuk pengujian internal, bukan private production keystore.
