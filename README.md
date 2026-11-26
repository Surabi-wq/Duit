# Duit — Pengelola Keuangan Offline

Duit adalah aplikasi Android native untuk pencatatan dan perencanaan keuangan lokal. Identitas visual menggunakan hijau, putih, dan biru. Data inti disimpan secara lokal melalui Room/SQLite; alur inti aplikasi tidak memerlukan koneksi internet.

## Landasan dasar resmi pengembangan

**Duit 1.5.0 ditetapkan sebagai baseline resmi** untuk seluruh pembuatan, perbaikan, dan update berikutnya. Pengembangan wajib dilanjutkan secara inkremental dari source ini, mempertahankan fitur dan data yang sudah berfungsi, menjaga rumus/validasi keuangan, serta memakai migrasi Room non-destruktif.

Dokumen acuan wajib: [Landasan Dasar Resmi Pengembangan Duit](docs/BASELINE-APPLICATION.md). Dokumen tersebut menetapkan kemampuan yang harus dipertahankan, prosedur perubahan database, standar pengujian, syarat Release APK, dan definisi selesai. Jangan memulai ulang aplikasi dari source lain atau menghilangkan fungsi baseline tanpa instruksi eksplisit.


## Versi source

- Versi: **1.5.0**
- Package: `com.surabi.duit`
- Minimum Android: API 23
- Compile/target SDK: 36
- Kotlin: 2.3.21
- Android Gradle Plugin: 8.13.2
- Penyimpanan: Room di atas SQLite
- Database Room: versi 5
- Nilai uang IDR: integer Rupiah (`Long`), tanpa floating-point untuk penyimpanan nominal

## Fitur baru versi 1.5.0 — backup dan restore database penuh

- Backup menyimpan snapshot konsisten dari seluruh tabel data Room dalam arsip ZIP khusus berekstensi .duitbackup, dengan nilai SQLite tetap bertipe (integer, float, teks, blob, atau null).
- Tercakup: workspace, akun, kategori, transaksi, anggaran, target tabungan, aset, utang, piutang, audit log, dan template transaksi berulang.
- Restore menampilkan metadata versi/tanggal serta jumlah tabel dan baris, memeriksa versi database dan kesesuaian seluruh nama tabel/kolom sebelum mengubah data, lalu meminta konfirmasi eksplisit.
- Penggantian data dilakukan dalam satu transaksi Room; kesalahan parsing, CRC ZIP, insert, atau foreign key membatalkan transaksi sehingga data sebelumnya tetap utuh.
- File backup belum dienkripsi dengan kata sandi. Simpan file pada lokasi privat/aman dan jangan membagikannya. Backup kompatibel dengan skema database yang sama; migrasi backup lintas versi skema belum disediakan.

## Fitur baru versi 1.4.0 — duplikasi transaksi

- Detail transaksi menyediakan tindakan duplikasi untuk pemasukan, pengeluaran, dan transfer.
- Form duplikasi memuat nominal, keterangan, catatan, akun sumber/tujuan, dan kategori yang masih valid pada workspace aktif.
- Salinan disimpan sebagai transaksi baru melalui validasi dan audit trail yang sama seperti pencatatan biasa; tanggalnya menggunakan waktu saat disimpan.
- Transaksi sumber tidak diedit, dihapus, atau ditimpa. Tidak ada perubahan skema Room maupun perhitungan saldo.

## Fitur baru versi 1.3.0 — jadwal transaksi berulang

- Template transaksi berulang tersimpan pada Room per workspace untuk pemasukan, pengeluaran, dan transfer; berisi nominal, akun, kategori, catatan, frekuensi harian/mingguan/bulanan/tahunan, interval, serta tanggal jatuh tempo pertama.
- Pengguna dapat menjeda/melanjutkan jadwal. Akun sumber/tujuan dan kategori divalidasi saat pembuatan maupun ketika transaksi dicatat.
- Pencatatan jatuh tempo dilakukan secara eksplisit oleh pengguna, bukan posting otomatis di background. Satu pencatatan membuat transaksi riil dan memajukan jadwal dalam satu transaksi Room.
- Compare-and-set pada tanggal jatuh tempo mencegah pencatatan ganda akibat ketukan cepat. Pembuatan, jeda, lanjut, dan occurrence dicatat pada audit log.
- Perhitungan bulanan mempertahankan tanggal acuan: 31 Januari → akhir Februari → 31 Maret, tanpa drift.
- Room v4→v5 menambah tabel template dan indeks secara non-destruktif; tabel transaksi lama dan rumus saldo tidak diubah.

## Penyempurnaan UI/UX versi 1.2.0

- Semua halaman utama memakai lebar konten maksimum yang terpusat pada layar besar agar dashboard, histori, rencana, aset, dan panduan tidak melebar berlebihan; layar ponsel tetap memakai lebar penuh yang tersedia.
- Filter histori menumpuk kolom tanggal dan nominal secara vertikal pada layar sempit, sementara layar yang lebih lebar tetap menampilkan pasangan kolom berdampingan.
- Komponen kartu memakai token bentuk dari tema, judul halaman dibatasi dengan ellipsis yang aman, dan target sentuh navigasi bawah dijaga minimal 48 dp.
- Pusat bantuan ditambah tutorial pencatatan transaksi, glosarium istilah saldo awal/arus kas/kekayaan bersih/realisasi/dana target/piutang-utang, serta FAQ tentang audit dan privasi.
- Tidak ada perubahan pada skema Room, migrasi, query saldo, rumus kekayaan bersih, ataupun aturan klasifikasi transaksi.

## Perbaikan lanjutan UI/UX dan performa versi 1.1.0

- Histori transaksi kini memakai AndroidX Paging 3 dan query Room PagingSource. Data dibaca bertahap dari SQLite (halaman 40 baris, pemuatan awal 60, cache maksimal 200 item) dengan indikator loading, error, dan retry; hasil pencarian tidak lagi dibatasi 500 baris.
- Ekspor CSV mengikuti filter yang aktif dan menulis histori yang cocok melalui batch 200 baris langsung ke penyimpanan yang dipilih. Seluruh hasil cocok diekspor tanpa mengumpulkan semua transaksi menjadi satu string besar di memori.
- Dashboard menyediakan pengaturan enam widget—tren, rekening, rencana, peringatan, jatuh tempo, dan aktivitas terbaru—yang disimpan lokal pada perangkat.
- Penyembunyian nominal global tersedia pada header, dipertahankan melalui preferensi lokal, dan menutupi nilai pada dashboard, transaksi, serta modul rencana/aset; teks detail peringatan juga disamarkan.
- Bottom navigation memiliki role dan status terpilih untuk pembaca layar.
- Database ditingkatkan dari Room v3 ke v4 hanya dengan menambahkan indeks gabungan untuk histori transaksi. Migrasi 1→2, 2→3, dan 3→4 tetap eksplisit; data transaksi pengguna tidak dihapus dan rumus saldo/kekayaan bersih tidak diubah.

## Perombakan pengalaman aplikasi versi 1.0.0

- Struktur aplikasi diganti dengan header workspace khusus, identitas merek baru, serta bottom dock lima menu dengan indikator aktif sendiri; bukan sekadar perubahan warna Material.
- Dashboard dirancang ulang dari awal: sapaan kontekstual dan tanggal, filter periode, saldo tersedia dan kekayaan bersih dalam satu hero, kartu uang masuk/keluar, tren bulanan, akses cepat, ringkasan rekening, rencana, peringatan, jatuh tempo, dan aktivitas terbaru.
- Grafik enam bulan menggunakan agregasi SUM pada Room/SQLite per bulan dan workspace. Histori tidak ditarik ke memori dan bukan berasal dari batas 200 transaksi pada daftar dashboard.
- Transaksi memakai hierarki halaman dan tile aktivitas baru yang menampilkan nominal, akun, kategori, waktu, dan catatan; pencarian, filter, detail, void/audit, dan ekspor CSV tetap menggunakan alur lama.
- Perencanaan memakai hero target tabungan, ringkasan jumlah sasaran/anggaran, dan kartu progres dengan nominal terkumpul, kekurangan, persentase, tanggal target, setor/tarik.
- Aset/kewajiban memakai banner kekayaan bersih dan latar modul yang berbeda dari halaman lama.
- Database tetap Room versi 3. Query tren adalah agregasi baca-saja; tidak ada migrasi, tidak ada perubahan rumus saldo/kekayaan bersih, dan tidak ada data demo.

## Penyegaran visual UI/UX versi 0.9.0

- Hero saldo bergradasi hijau-biru, identitas merek dan ikon launcher yang diperbarui.
- Latar aplikasi dibuat lebih terang; kartu metrik memakai aksen warna semantik, batas halus, dan hierarki tipografi yang lebih tegas.
- Dashboard menampilkan pilihan periode sebagai chip, saldo akun, kekayaan bersih, pemasukan/pengeluaran, arus kas dengan dua indikator terpisah, saldo per akun, peringatan, anggaran, target, jatuh tempo, dan transaksi terbaru.
- Kartu transaksi, target, anggaran, aset, piutang, dan kewajiban menggunakan radius, border, elevasi, dan warna status yang konsisten.
- Tidak ada angka contoh yang ditambahkan. Visual ringkasan tetap berdasarkan data Room lokal. Grafik tren enam bulan sekarang dibuat dari agregasi bulanan Room/SQLite, bukan dari 200 transaksi terbaru atau angka sintetis.

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

Catatan cakupan: histori transaksi dan ekspor CSV sekarang memakai pemuatan/penulisan bertahap. Pengujian perangkat langsung dan pengujian performa pada basis data sangat besar tetap diperlukan. Pengujian navigasi dan tampilan pada perangkat Android fisik serta ukuran font besar masih diperlukan.

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
- Filter transaksi mencakup jenis khusus seperti setoran target, pembayaran utang, dan penerimaan piutang. Pada versi 1.1.0, tampilan histori dimuat secara bertahap melalui Paging 3.
- Ekspor CSV menyimpan seluruh hasil pencarian/filter aktif secara streaming ke lokasi pilihan Android. Ekspor CSV bukan pengganti backup database.
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
- Impor CSV/spreadsheet; ekspor laporan PDF/Excel/DOCX.
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

GitHub Actions menjalankan unit test, build Release, dan mengunggah artefak bernama **Duit-1.3.0.apk**.

## Signing dan distribusi

Varian Release saat ini menggunakan konfigurasi signing debug untuk pengujian internal. APK ini dapat dipasang untuk pengujian, tetapi **belum ditandatangani dengan kunci rilis privat** untuk distribusi komersial atau pembaruan jangka panjang. Sebelum distribusi, buat keystore rilis, simpan kredensialnya sebagai secret CI, dan konfigurasi signing release. Jangan pernah commit keystore atau password ke repository.

## Database dan kompatibilitas

Database Room versi 4 memiliki migrasi eksplisit dari versi 1 ke 2, versi 2 ke 3, dan versi 3 ke 4. Migrasi 3→4 hanya menambahkan indeks gabungan untuk pencarian/paging transaksi. Jangan gunakan destructive migration pada data pengguna. Perubahan skema berikutnya harus menambahkan migrasi dan pengujian upgrade, bukan menghapus database. Sebelum penggunaan produksi, uji upgrade dari APK versi sebelumnya pada perangkat uji dan siapkan backup/restore tervalidasi. Backup/restore database penuh belum tersedia pada versi 1.1.0.

## Ekspor CSV dan keamanan spreadsheet

Ekspor pada versi 1.1.0 memuat seluruh hasil filter aktif secara streaming dalam batch database; bukan dibatasi 500 transaksi. File menggunakan UTF-8 dengan BOM agar lebih mudah dibaca oleh aplikasi spreadsheet. Field teks yang diawali karakter formula umum diberi awalan apostrof sebagai perlindungan dasar dari formula injection. Tetap tinjau file sebelum membukanya dengan aplikasi spreadsheet yang tidak dikenal.

## Pengujian dan batas verifikasi

CI menjalankan `:app:testDebugUnitTest` dan `:app:assembleRelease`. Unit test mencakup parsing nominal Rupiah, aturan peringatan, validasi rentang tanggal transaksi, serta escaping dan perlindungan formula pada ekspor CSV. Build yang berhasil bukan bukti bahwa seluruh kebutuhan enterprise A–Z telah selesai. Pengujian instalasi, upgrade database, alur UI, ekspor melalui Storage Access Framework, kinerja dataset besar, dan penggunaan offline tetap perlu diverifikasi pada perangkat Android fisik.
