# Status Implementasi Fitur Duit

Dokumen ini membedakan fitur yang sudah memiliki alur kerja aktual dari fitur yang masih berupa target pengembangan.

## Fondasi 0.1.0 — tersedia di source code
- Workspace terpisah dengan jenis Pribadi, Keluarga, Bisnis, Perusahaan, Proyek, dan Lainnya.
- Akun/dompet ganda per workspace, termasuk saldo awal.
- Pemasukan, pengeluaran, dan transfer antar-akun.
- Kategori transaksi dasar yang disiapkan lokal.
- Dashboard saldo akun, pemasukan, pengeluaran, dan arus kas bulan berjalan.
- Anggaran bulanan umum atau berbasis kategori pengeluaran.
- Daftar maksimal 200 transaksi terbaru per workspace.
- Penyimpanan lokal Room/SQLite dengan relasi foreign key dan index.
- Validasi nominal positif, tipe transaksi, akun sumber/tujuan, dan konsistensi workspace.
- Tes unit parsing nominal integer rupiah.
- Pipeline CI untuk unit test dan APK Release.

## Aturan perhitungan yang sudah ditetapkan
1. Pemasukan menambah saldo akun sumber.
2. Pengeluaran mengurangi saldo akun sumber.
3. Transfer mengurangi akun sumber dan menambah akun tujuan.
4. Transfer tidak masuk ke agregat pemasukan maupun pengeluaran.
5. Total saldo dashboard merupakan penjumlahan saldo seluruh akun aktif pada workspace terpilih.
6. Anggaran menghitung transaksi berjenis EXPENSE pada rentang tanggal anggaran. Anggaran kategori hanya menghitung kategori yang dipilih.
7. Nilai IDR disimpan sebagai Long (rupiah bulat). Perhitungan tidak menggunakan Float atau Double.

## Belum tersedia dan tidak boleh dianggap aktif
- Edit/hapus transaksi dengan reversal dan audit log.
- Penyesuaian saldo dan rekonsiliasi rekening.
- Lampiran foto/dokumen transaksi.
- Transaksi berulang, terjadwal, massal, impor, dan ekspor.
- Target tabungan.
- Aset, penyusutan, kewajiban, utang, dan piutang.
- Chart of Accounts, jurnal double-entry, buku besar, neraca saldo, neraca, laba rugi, dan arus kas akuntansi.
- Penjualan, pembelian, invoice, pelanggan, pemasok, inventori, dan gudang.
- Investasi, pajak, dokumen administrasi, dan multi-mata uang.
- Analisis prediktif, OCR lokal, serta asisten data.
- Pengguna/role/permission dan approval workflow.
- PIN, biometrik, enkripsi database, audit trail lengkap.
- Backup/restore terenkripsi, checksum, dan migrasi antarversi.

## Gerbang penerimaan sebelum fitur boleh ditandai selesai
- Alur UI bisa dijalankan sampai data tersimpan.
- Validasi input dan relasi database diterapkan.
- Perhitungan diverifikasi dengan kasus normal, batas, dan kasus gagal.
- Tidak menghilangkan atau mengubah histori secara diam-diam.
- Unit test dan integration test sesuai risiko modul lulus.
- Build Debug dan Release berhasil.
- Tidak ada menu/tombol palsu yang hanya menjadi pajangan.
