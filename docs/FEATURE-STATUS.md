# Status Implementasi Fitur Duit

Versi source saat ini: **1.1.0**. Repositori dikembangkan sebagai aplikasi Android lokal/offline. Database Room saat ini versi 3.

## Penyempurnaan UI/UX dan privasi — 1.1.0
- Widget dashboard dapat dinyalakan/dimatikan per bagian: arus kas, tren, rekening, rencana, peringatan, jatuh tempo, dan aktivitas. Preferensi disimpan lokal.
- Tombol penyamaran nominal di header menyembunyikan nilai uang pada dashboard, transaksi, perencanaan, aset, piutang, dan kewajiban. Mode ini tidak mengubah data dan bukan pengganti penguncian atau enkripsi aplikasi.
- Navigasi bawah memiliki semantik item terpilih untuk pembaca layar; header dipadatkan agar pemilih workspace dan kontrol tidak mudah bertabrakan pada layar sempit.
- Daftar transaksi menggunakan pagination 50 baris; ekspor CSV membaca seluruh hasil per batch.
- Query ringkasan transaksi dashboard hanya mengambil 20 baris terbaru, sementara query jumlah transaksi terpisah digunakan untuk kondisi data kosong.

## Sudah memiliki alur kerja nyata

### Dashboard, workspace, dan akun
- Dashboard workspace dengan saldo akun, pemasukan/pengeluaran periode terpilih (bulan ini, 30 hari terakhir, atau tahun ini), grafik agregat bulanan enam bulan, arus kas, anggaran, estimasi kekayaan bersih, dan peringatan keuangan berbasis aturan lokal.
- Workspace terpisah untuk Pribadi, Keluarga, Bisnis, Perusahaan, Proyek, dan Lainnya.
- Banyak akun/dompet per workspace, dengan saldo dihitung dari saldo awal dan histori transaksi.
- Batas saldo minimum dapat diatur per akun; nilai 0 menonaktifkan peringatan. Perubahan dibatasi pada akun di workspace yang sama dan dicatat pada audit log.
- Akun dapat berupa tunai, bank, tabungan, e-wallet, kartu kredit, investasi, atau lainnya.

### Transaksi dan kontrol data
- Pemasukan, pengeluaran, dan transfer antar-akun.
- Pencarian langsung di SQLite berdasarkan keterangan, catatan, kategori, dan jenis transaksi.
- Histori dimuat secara bertahap 50 baris per halaman dengan total hasil terhitung di SQLite; tidak memuat semua riwayat ke memori dan tidak memiliki batas 500 baris pada layar.
- Pencarian dan filter diterapkan pada database sebelum pagination; pemuatan ulang dilakukan saat data transaksi aktif berubah.
- Filter berdasarkan jenis, rentang tanggal inklusif, akun sumber/tujuan, kategori, nominal minimum/maksimum, dan pengurutan tanggal atau nominal.
- Validasi rentang tanggal menolak tanggal kalender tidak valid, teks tambahan, dan rentang awal yang melebihi tanggal akhir.
- Filter divalidasi pada repository dan query dijalankan pada database; pencarian tidak memuat seluruh histori transaksi ke memori.
- Ekspor CSV mencakup seluruh hasil pencarian/filter dengan pembacaan dan penulisan per batch 500 baris melalui Android Storage Access Framework; UTF-8 BOM dan perlindungan formula spreadsheet tetap aktif.
- Pembatalan transaksi (void) dengan alasan yang tersimpan pada audit log; catatan transaksi tidak dihapus.
- Validasi akun/kategori harus berasal dari workspace yang sama.
- Semua insert yang terkait dilakukan dalam transaksi database Room.

### Anggaran dan tujuan
- Anggaran bulanan umum atau kategori pengeluaran.
- Target tabungan dengan nilai tujuan, prioritas, dan tanggal target opsional yang disimpan ke database.
- Setoran target mengurangi saldo akun yang dipilih; penarikan target menambahnya kembali.
- Nilai target tersimpan berdasarkan transaksi setoran/penarikan, tidak disimpan sebagai angka yang dapat menyimpang dari histori.

### Aset, piutang, dan kewajiban
- Pencatatan aset non-tunai, kategori aset, harga perolehan, dan nilai terkini.
- Pencatatan piutang/tagihan per workspace beserta pihak yang berutang, nominal awal, jatuh tempo opsional, dan catatan.
- Penerimaan piutang sebagian atau penuh ke akun yang dipilih; validasi mencegah penerimaan melebihi sisa piutang.
- Sisa piutang dihitung dari seluruh penerimaan yang belum dibatalkan; pembatalan penerimaan memulihkan saldo akun dan sisa tagihan.
- Piutang yang belum tertagih ikut menambah estimasi kekayaan bersih, sedangkan pencatatan piutang tidak menambah saldo tunai/rekening.
- Pembaruan nilai aset dengan catatan perubahan pada audit log.
- Pencatatan utang dengan pemberi pinjaman, nominal awal, tanggal jatuh tempo opsional, dan catatan.
- Pencairan pinjaman dapat dikreditkan ke satu akun saat utang dibuat; gunakan hanya bila saldo pencairan belum pernah dicatat.
- Pembayaran utang mengurangi saldo akun dan sisa utang secara atomik.
- Kekayaan bersih terdata = saldo akun + dana target + nilai aset non-tunai − sisa utang.

### Peringatan keuangan dashboard
- Mesin aturan lokal mengevaluasi saldo negatif, saldo di bawah batas minimum akun, realisasi anggaran (peringatan mulai 80% dan kritis saat terlampaui), arus kas negatif sesuai periode terpilih, serta utang/piutang yang sudah melewati jatuh tempo.
- Peringatan dihitung dari data workspace aktif; tidak mengubah transaksi atau saldo.
- Peringatan dibatasi pada lima item teratas agar dashboard tetap ringkas.

### Database dan kualitas
- Room/SQLite lokal, foreign key, index, serta operasi transaksi atomik.
- Database versi 3 dengan migrasi eksplisit versi 1→2 dan 2→3; tidak memakai destructive migration.
- Audit log untuk penciptaan transaksi, target, aset, kewajiban, pembaruan nilai, perubahan batas saldo minimum akun, dan pembatalan transaksi.
- Nilai IDR menggunakan Long (rupiah bulat) dan input nominal divalidasi tanpa konversi floating-point.
- CI menjalankan unit test dan build APK Release. Unit test mencakup parsing nominal, aturan peringatan keuangan, batas periode dashboard, validasi nominal batas saldo minimum, serta escaping dan perlindungan formula pada CSV.


## Peningkatan UI/UX yang sudah terhubung ke data — 0.8.0

- Design system global dengan palet semantik, surface konsisten, dan shape 8/12/16/20/24 dp.
- Header workspace, bottom navigation, dan tindakan cepat global.
- Dashboard memakai query tanggal aktual dengan periode bulan ini, 30 hari terakhir, dan tahun berjalan.
- Dashboard menampilkan rekening, kekayaan bersih terdata, arus kas, anggaran, target, peringatan, dan jatuh tempo dari database.
- Daftar transaksi dikelompokkan per tanggal; baris dapat membuka detail transaksi dan catatan.
- Perencanaan memiliki ringkasan total target/realisasi dan detail tanggal target/periode anggaran.
- Modul aset dipisah dengan chip kategori; nilai aset dapat diperbarui, serta batas saldo minimum akun tetap dapat diatur.
- Panduan pencarian topik dan pintasan navigasi ke halaman fitur.
- Unit test tambahan memeriksa batas periode dashboard lokal dan akhir hari inklusif.
- Tidak ada perubahan skema database atau rumus saldo/kekayaan bersih pada pembaruan UI ini.

## Penyegaran visual UI/UX — 0.9.0

- Palet hijau-biru dibuat lebih tegas dengan background abu-abu kebiruan yang sangat muda, surface putih, serta status merah/oranye/biru/hijau yang konsisten.
- Tipografi diperkuat untuk judul, saldo, nilai metrik, dan label; komponen mengikuti radius 12–26 dp dan ritme spacing 4/8 dp.
- Dashboard memiliki kartu saldo utama bergradasi, ringkasan kekayaan bersih, kartu pemasukan/pengeluaran beraksen, dan dua progress bar arus kas yang memakai nominal aktual pada periode terpilih.
- Pemilih periode ditampilkan sebagai chip yang terlihat langsung; rincian akun memakai ikon, warna lembut, dan batas kartu yang konsisten.
- Modul perencanaan dan kekayaan bersih menggunakan komponen hero yang sama; kartu target, anggaran, aset, piutang, utang, transaksi, dan empty state diperkuat dengan border/elevasi/tint.
- Logo launcher dan identitas aplikasi disegarkan tanpa mengubah nama paket.
- Tidak ada perubahan skema Room, query saldo, atau rumus kekayaan bersih. Tidak ada data dummy.
- Grafik tren waktu dan pengaturan widget belum diimplementasikan karena membutuhkan query seri waktu serta preferensi persisten; tidak disimulasikan dengan data buatan.

## Perombakan UI/UX struktural — 1.0.0

- Header aplikasi dibuat sebagai brand/workspace bar tersendiri, dengan kontrol workspace berbentuk selector dan aksi tambah workspace.
- Navigasi bawah menjadi bottom dock khusus lima modul dengan indikator aktif di bagian atas ikon dan permukaan menu terpilih.
- Dashboard bukan lagi tumpukan komponen lama. Susunan baru berisi sapaan/tanggal, pemilih periode, hero saldo+kekayaan bersih, pemasukan/pengeluaran, grafik tren enam bulan, akses cepat, rekening, perencanaan, peringatan, jatuh tempo, dan aktivitas.
- Grafik bulanan menggunakan agregasi SQL pada tabel transaksi yang difilter per workspace, periode enam bulan, dan isVoided=0. Hanya baris agregat bulan (maksimal enam) yang dikirim ke UI; tidak mengambil seluruh histori dan tidak menggunakan batas 200 transaksi pada daftar.
- Tile transaksi dirombak dengan ikon jenis transaksi, nominal yang menonjol, akun, kategori, catatan, serta waktu. Query, pencarian, filter, detail, pembatalan/void, audit log, dan ekspor tetap mempertahankan alur sebelumnya.
- Perencanaan dan aset diberi latar halaman khusus, banner finansial kontras tinggi, serta kartu progres yang menampilkan angka utama lebih jelas.
- Tidak ada perubahan entity/schema Room, version database, migrasi, saldo, transfer, rumus cashflow, atau formula kekayaan bersih. Query tren hanya agregasi baca.

## Temuan audit UI/UX dan status penanganan

**Keparahan tinggi — sudah ditangani pada 1.1.0**
- Batas 500 hasil pada layar riwayat transaksi dihilangkan dengan query SQLite berhalaman 50 baris dan query jumlah hasil terpisah.
- Ekspor CSV kini mengambil seluruh hasil yang cocok dengan pencarian/filter secara bertahap per batch 500 baris; tidak lagi hanya mengekspor baris yang tampil di layar.

**Keparahan sedang — sudah ditangani sebagian**
- Filter lanjutan dipindahkan ke dialog agar tidak memakan ruang vertikal layar riwayat; pencarian, filter tanggal, akun, kategori, jenis, nominal, dan urutan tetap dijalankan di database.
- Pengguna dapat menyamarkan nominal seluruh halaman keuangan dan mengatur tujuh widget dashboard; kedua preferensi disimpan lokal.
- Query ringkasan transaksi dashboard diperkecil ke 20 entri terbaru, sementara total transaksi dihitung terpisah.
- Navigasi bawah menandai tab aktif secara visual dan semantik; header dipadatkan untuk layar sempit.

**Keparahan yang masih terbuka**
- Modul transaksi umum belum memiliki edit transaksi, duplikasi/template, lampiran, dan impor. Pembatalan transaksi tetap melalui void dan audit trail.
- Backup/restore database penuh, PIN/biometrik, penguncian aplikasi, enkripsi database, dan mekanisme pemulihan data belum tersedia.
- Belum ada UI automation menyeluruh, uji aksesibilitas otomatis, uji font sistem maksimum, uji beragam ukuran layar, maupun uji instalasi pada perangkat Android fisik.
- Modularisasi lebih lanjut masih diperlukan; MainActivity tetap mengatur orkestrasi data dan dialog. Pemisahan use-case/state management ke lapisan tersendiri belum selesai.

## Aturan akuntansi penting pada versi 1.1.0
1. Transfer antar-akun tidak mengubah pemasukan/pengeluaran.
2. Setoran target mengurangi akun sumber, tetapi menjadi dana target; karena itu tidak dicatat sebagai biaya.
3. Penarikan target menambah akun sumber dan mengurangi dana target; bukan pendapatan.
4. Pembayaran pokok utang mengurangi akun dan kewajiban; bukan seluruhnya biaya.
5. Pencairan pinjaman menambah akun dan kewajiban; bukan pendapatan.
6. Nilai aset non-tunai tidak boleh mencakup rekening/dompet yang sudah dicatat pada akun. Hal ini mencegah penghitungan ganda.
7. Untuk pinjaman yang saldonya sudah tercatat, jangan memilih akun penerima saat membuat data utang.
8. Mencatat piutang/tagihan tidak otomatis menambah saldo akun; hanya penerimaan yang benar-benar diterima yang menambah akun.
9. Penerimaan piutang mengurangi sisa piutang, bukan pendapatan baru di laporan kas; pendapatan penjualan awal belum dibuat otomatis oleh modul piutang dasar.

## Belum tersedia — jangan dianggap aktif
- Transfer transaksi lintas workspace dengan prosedur kontrol dan konsolidasi multi-workspace.
- Edit/hapus piutang, pembagian satu piutang ke beberapa invoice, riwayat perubahan rinci, dan pengingat jatuh tempo piutang.
- Edit transaksi umum, riwayat versi detail, dan reversal jurnal double-entry.
- Rekonsiliasi rekening dan penyesuaian saldo formal.
- Lampiran struk/dokumen, transaksi berulang/terjadwal/massal, template, dan impor CSV/spreadsheet.
- Ekspor PDF/Excel/DOCX, laporan profesional, serta ekspor seluruh histori dengan pagination.
- Target dengan jadwal kontribusi otomatis dan notifikasi pengingat.
- Penyusutan, jadwal pemeliharaan, penjualan/penghapusan aset melalui jurnal.
- Integrasi piutang dengan invoice/penjualan kredit dan jurnal pendapatan; analisis umur piutang, bunga/denda terstruktur, jadwal cicilan otomatis, serta laporan penagihan lanjutan.
- Chart of Accounts, jurnal double-entry, buku besar, neraca saldo, neraca, laba rugi, dan laporan perubahan ekuitas.
- ERP: perusahaan/cabang/departemen/cost center/proyek terstruktur; penjualan, pembelian, invoice, pelanggan, pemasok, produk, persediaan, gudang, dan HPP.
- Investasi dengan harga pasar otomatis, dividen, obligasi, dan portofolio.
- Pajak, invoice/kwitansi profesional, arsip dokumen, ekspor PDF/Excel/DOCX, serta impor CSV/spreadsheet.
- Perbandingan periode/workspace, analisis prediktif, rekomendasi cerdas, OCR, dan asisten bahasa sehari-hari. Grafik tren agregat enam bulan tersedia; grafik prediktif atau perbandingan multi-workspace belum tersedia.
- Pengguna multi-peran, hak akses, approval berjenjang.
- PIN/biometrik, enkripsi database, pengamanan lampiran, backup/restore terenkripsi beserta checksum.
- Pengaturan lengkap, tema gelap, mode pemula/mahir, multi-mata uang.

## Gerbang penerimaan
- Alur UI berakhir dengan data tersimpan, bukan sekadar halaman tampilan.
- Validasi input dan workspace diterapkan.
- Perhitungan diperiksa dengan kasus normal, batas, dan kegagalan.
- Tidak menghapus histori keuangan secara diam-diam.
- Tes unit/database/UI sesuai risiko modul lulus.
- Build Debug/Release lulus.
- Instalasi, upgrade database, dan penggunaan offline perlu diuji pada perangkat Android fisik sebelum dinyatakan siap rilis publik.
