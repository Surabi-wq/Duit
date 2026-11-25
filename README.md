# Duit — Pengelola Keuangan Offline

Duit adalah aplikasi Android native untuk pencatatan dan perencanaan keuangan lokal. Identitas visual menggunakan hijau, putih, dan biru. Data inti disimpan secara lokal melalui Room/SQLite; alur inti aplikasi tidak memerlukan koneksi internet.

## Versi source

- Versi: **0.9.0**
- Package: `com.surabi.duit`
- Minimum Android: API 23
- Compile/target SDK: 36
- Kotlin: 2.3.21
- Android Gradle Plugin: 8.13.2
- Penyimpanan: Room di atas SQLite
- Database Room: versi 3
- Nilai uang IDR: integer Rupiah (`Long`), tanpa floating-point untuk penyimpanan nominal

## Penyegaran visual UI/UX versi 0.9.0

- Hero saldo bergradasi hijau-biru, identitas merek dan ikon launcher yang diperbarui.
- Latar aplikasi dibuat lebih terang; kartu metrik memakai aksen warna semantik, batas halus, dan hierarki tipografi yang lebih tegas.
- Dashboard menampilkan pilihan periode sebagai chip, saldo akun, kekayaan bersih, pemasukan/pengeluaran, arus kas dengan dua indikator terpisah, saldo per akun, peringatan, anggaran, target, jatuh tempo, dan transaksi terbaru.
- Kartu transaksi, target, anggaran, aset, piutang, dan kewajiban menggunakan radius, border, elevasi, dan warna status yang konsisten.
- Tidak ada angka contoh yang ditambahkan. Visual ringkasan tetap berdasarkan data Room lokal. Grafik tren harian belum ditambahkan karena query historis terstruktur untuk rentang tersebut belum tersedia; tidak menggunakan data sintetis.

## Fitur yang sudah memiliki alur kerja nyata

## Pembaruan UI/UX versi 0.8.0

- Design system menyatukan palet hijau/biru, warna status, background, surface, dan radius komponen.
- Header menampilkan identitas Duit dan workspace aktif; navigasi bawah diberi indikator aktif yang konsisten.
- Tombol tindakan cepat global membuka alur yang benar-benar tersedia: transaksi, akun, anggaran, target, aset, piutang, dan utang.
- Dashboard memiliki pilihan periode bulan ini, 30 hari terakhir, dan tahun ini. Query pemasukan/pengeluaran mengikuti rentang tanggal lokal dan inklusif.
- Ringkasan dashboard menampilkan saldo akun, estimasi kekayaan bersih, arus kas, rincian akun, peringatan, anggaran, target, tanggal jatuh tempo yang tercatat, dan transaksi terbaru.
- Transaksi dikelompokkan per tanggal dan dapat dibuka untuk melihat detail. Pembatalan tetap memakai status void dan alasan audit; edit/hapus permanen tidak ditambahkan.
- Perencanaan menampilkan ringkasan target dan anggaran; kartu target menampilkan progres, kekurangan, dan tanggal target; kartu anggaran menampilkan periode dan persentase pemakaian.
- Modul Aset dipisahkan menjadi akun, aset non-tunai, piutang, dan utang. Nilai aset kini dapat diperbarui dari kartu aset.
- Panduan menjadi pusat bantuan yang dapat dicari dan berisi pintasan menuju modul terkait.
- Seluruh alur UI baru tetap membaca data Room/SQLite lokal; tidak ada data demo atau layanan eksternal baru.

Catatan cakupan: daftar transaksi masih dibatasi 500 hasil oleh query saat ini. Pagination/muat bertahap untuk histori ribuan transaksi belum selesai. Pengujian navigasi dan tampilan pada perangkat Android fisik serta ukuran font besar masih diperlukan.

### Workspace, akun, dan dashboard
- Workspace terpisah untuk Pribadi, Keluarga, Bisnis, Perusahaan, Proyek, dan Lainnya.
- Banyak akun per workspace: tunai, bank, tabungan, e-wallet, kartu kredit, investasi, dan lainnya.
- Batas saldo minimum dapat diatur per akun; perubahan tersimpan pada audit log dan tidak mengubah saldo maupun histori transaksi.
- Saldo akun dihitung dari saldo awal serta transaksi; bukan angka saldo manual yang dapat tertinggal dari histori.
- Dashboard workspace menampilkan saldo akun, pemasukan/pengeluaran berdasarkan periode terpilih (bulan ini, 30 hari terakhir, atau tahun ini), arus kas, anggaran, estimasi kekayaan bersih, aktivitas transaksi terbaru, dan peringatan berbasis aturan lokal.
- Peringatan mencakup saldo negatif, saldo di bawah batas minimum, anggaran hampir habis/terlampaui, arus kas negatif, serta utang/piutang yang melewati jatuh tempo.

### Transaksi
- Pemasukan, pengeluaran, transfer antar-akun, pergerakan dana target, pencairan/pembayaran utang, dan penerimaan piutang.
- Transfer mengurangi akun sumber dan menambah akun tujuan tanpa mengubah total pemasukan/pengeluaran.
- Validasi akun dan kategori memastikan relasi berada dalam workspace yang sama.
- Pencarian transaksi dijalankan langsung pada SQLite berdasarkan keterangan, catatan, kategori, dan jenis transaksi.
- Filter lanjutan mencakup rentang tanggal inklusif, akun sumber/tujuan, kategori, nominal minimum/maksimum, serta pengurutan berdasarkan tanggal atau nominal.
- Validasi tanggal menolak tanggal kalender yang tidak sah, teks tambahan, dan rentang tanggal terbalik.
- Hasil pencarian menampilkan maksimal 500 transaksi terbaru yang cocok; filter jenis mencakup transaksi khusus seperti setoran target, pembayaran utang, dan penerimaan piutang.
- Ekspor CSV menyimpan hasil pencarian dan filter yang sedang ditampilkan melalui pemilih lokasi simpan Android. Ekspor dibatasi maksimal 500 baris dan bukan pengganti backup database.
- Pembatalan transaksi menggunakan status void dengan alasan; histori tidak dihapus dan pembatalan dicatat pada audit log.

### Anggaran dan target tabungan
- Anggaran bulanan umum atau berdasarkan kategori pengeluaran.
- Target tabungan dengan nominal tujuan, prioritas, dan tanggal target opsional yang dapat ditentukan saat pembuatan.
- Setoran target mengurangi saldo akun yang dipilih; penarikan mengembalikan saldo akun.
- Dana target dihitung dari transaksi setoran/penarikan yang terkait, bukan saldo manual yang dapat tidak sinkron.

### Aset, utang, dan piutang
- Pencatatan aset non-tunai: kendaraan, properti, tanah, emas, elektronik, peralatan, dan lainnya.
- Pencatatan harga perolehan dan nilai terkini; perubahan nilai aset dicatat pada audit log.
- Pencatatan utang/pinjaman, pemberi pinjaman, nilai awal, dan jatuh tempo opsional.
- Pencairan pinjaman dapat menambah saldo akun ketika dana baru diterima dan belum pernah dicatat.
- Pembayaran pokok utang mengurangi saldo akun dan sisa kewajiban.
- Pencatatan piutang/tagihan per workspace, pihak yang berutang, nilai awal, jatuh tempo opsional, dan catatan.
- Penerimaan piutang dapat dicatat sebagian atau penuh ke akun yang dipilih; validasi mencegah penerimaan melebihi sisa piutang.
- Sisa piutang dihitung dari penerimaan yang belum dibatalkan. Pembatalan penerimaan memulihkan saldo akun dan sisa tagihan.

### Aturan pencatatan untuk mencegah penghitungan ganda
1. Transfer antar-akun bukan pemasukan atau pengeluaran.
2. Setoran target mengurangi akun sumber, tetapi menambah dana target; karena itu bukan biaya.
3. Penarikan target menambah akun sumber dan mengurangi dana target; bukan pendapatan.
4. Pembayaran pokok utang mengurangi akun dan kewajiban; bukan seluruhnya biaya.
5. Pencairan pinjaman menambah akun dan kewajiban; bukan pendapatan.
6. Nilai aset non-tunai tidak boleh mencakup rekening/dompet yang sudah dicatat pada modul akun.
7. Saat mencatat utang yang dananya sudah ada di saldo akun, jangan pilih akun penerima lagi.
8. Pencatatan piutang tidak otomatis menambah saldo tunai/rekening; saldo akun bertambah hanya saat penerimaan benar-benar dicatat.
9. Penerimaan piutang dasar mengurangi sisa tagihan. Pengakuan pendapatan dan pembuatan invoice/penjualan kredit yang terhubung belum tersedia.

## Fitur yang belum tersedia sebagai fungsi lengkap

Fitur berikut masih menjadi backlog dan tidak boleh dianggap aktif hanya karena ada istilah, komponen, atau rancangan antarmukanya:
- Rekonsiliasi rekening, penyesuaian saldo formal, edit transaksi umum, transaksi berulang/terjadwal/massal, template, lampiran struk, dan pembatalan melalui reversal jurnal double-entry.
- Impor CSV/spreadsheet; ekspor laporan PDF/Excel/DOCX; ekspor transaksi tanpa batas/pagination.
- Target dengan setoran terjadwal otomatis dan notifikasi.
- Riwayat nilai aset terstruktur, penyusutan, pemeliharaan, pembelian/penjualan/penghapusan aset yang terhubung ke jurnal.
- Jadwal cicilan, bunga/denda terstruktur, analisis umur piutang, dan penagihan lanjutan.
- Chart of Accounts, jurnal double-entry, buku besar, buku pembantu, neraca saldo, neraca, laba rugi, laporan arus kas akuntansi, perubahan ekuitas, serta periode akuntansi.
- ERP multi-perusahaan/cabang/departemen/cost center/proyek, penjualan, pembelian, invoice, pelanggan, pemasok, produk, persediaan, gudang, dan HPP.
- Portofolio investasi lengkap, harga pasar otomatis, dividen, obligasi, dan analisis alokasi.
- Pajak, invoice/kwitansi profesional, arsip dokumen, serta dokumen administrasi.
- Grafik lanjutan, perbandingan periode/workspace, analisis prediktif, simulasi, OCR lokal, dan asisten bahasa sehari-hari.
- Multi-user, role/permission, persetujuan berjenjang, PIN/biometrik, enkripsi database, pengamanan lampiran, dan backup/restore terenkripsi dengan checksum.
- Personalisasi lengkap, tema gelap/sistem, mode pemula/mahir, dan multi-mata uang.

Lihat [docs/FEATURE-STATUS.md](docs/FEATURE-STATUS.md) untuk status yang lebih terperinci dan gerbang penerimaan.

## Build

Memerlukan JDK 17, Android SDK Platform 36, Build Tools 36.0.0, dan Gradle 8.13.

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
gradle :app:assembleRelease
```

GitHub Actions menjalankan unit test, build Release, dan mengunggah artefak bernama **Duit-0.9.0.apk**.

## Signing dan distribusi

Varian Release saat ini menggunakan konfigurasi signing debug untuk pengujian internal. APK ini dapat dipasang untuk pengujian, tetapi **belum ditandatangani dengan kunci rilis privat** untuk distribusi komersial atau pembaruan jangka panjang. Sebelum distribusi, buat keystore rilis, simpan kredensialnya sebagai secret CI, dan konfigurasi signing release. Jangan pernah commit keystore atau password ke repository.

## Database dan kompatibilitas

Database Room versi 3 memiliki migrasi eksplisit dari versi 1 ke 2 dan versi 2 ke 3. Jangan gunakan destructive migration pada data pengguna. Perubahan skema berikutnya harus menambahkan migrasi dan pengujian upgrade, bukan menghapus database. Sebelum penggunaan produksi, uji upgrade dari APK versi sebelumnya pada perangkat uji dan siapkan backup/restore tervalidasi. Backup/restore database penuh belum tersedia pada 0.8.0.

## Ekspor CSV dan keamanan spreadsheet

Ekspor hanya mencakup hasil pencarian yang ditampilkan (maksimal 500 transaksi terbaru yang cocok). File menggunakan UTF-8 dengan BOM agar lebih mudah dibaca oleh aplikasi spreadsheet. Field teks yang diawali karakter formula umum diberi awalan apostrof sebagai perlindungan dasar dari formula injection. Tetap tinjau file sebelum membukanya dengan aplikasi spreadsheet yang tidak dikenal.

## Pengujian dan batas verifikasi

CI menjalankan `:app:testDebugUnitTest` dan `:app:assembleRelease`. Unit test mencakup parsing nominal Rupiah, aturan peringatan, validasi rentang tanggal transaksi, serta escaping dan perlindungan formula pada ekspor CSV. Build yang berhasil bukan bukti bahwa seluruh kebutuhan enterprise A–Z telah selesai. Pengujian instalasi, upgrade database, alur UI, ekspor melalui Storage Access Framework, kinerja dataset besar, dan penggunaan offline tetap perlu diverifikasi pada perangkat Android fisik.
