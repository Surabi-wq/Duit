# Status Implementasi Fitur Duit

Versi source saat ini: **0.9.0**. Repositori dikembangkan sebagai aplikasi Android lokal/offline. Database Room saat ini versi 3.

## Sudah memiliki alur kerja nyata

### Dashboard, workspace, dan akun
- Dashboard workspace dengan saldo akun, pemasukan/pengeluaran periode terpilih (bulan ini, 30 hari terakhir, atau tahun ini), arus kas, anggaran, estimasi kekayaan bersih, dan peringatan keuangan berbasis aturan lokal.
- Workspace terpisah untuk Pribadi, Keluarga, Bisnis, Perusahaan, Proyek, dan Lainnya.
- Banyak akun/dompet per workspace, dengan saldo dihitung dari saldo awal dan histori transaksi.
- Batas saldo minimum dapat diatur per akun; nilai 0 menonaktifkan peringatan. Perubahan dibatasi pada akun di workspace yang sama dan dicatat pada audit log.
- Akun dapat berupa tunai, bank, tabungan, e-wallet, kartu kredit, investasi, atau lainnya.

### Transaksi dan kontrol data
- Pemasukan, pengeluaran, dan transfer antar-akun.
- Pencarian langsung di SQLite berdasarkan keterangan, catatan, kategori, dan jenis transaksi; hasil dibatasi 500 transaksi terbaru yang cocok.
- Filter berdasarkan jenis, rentang tanggal inklusif, akun sumber/tujuan, kategori, nominal minimum/maksimum, dan pengurutan tanggal atau nominal.
- Validasi rentang tanggal menolak tanggal kalender tidak valid, teks tambahan, dan rentang awal yang melebihi tanggal akhir.
- Filter divalidasi pada repository dan query dijalankan pada database; pencarian tidak memuat seluruh histori transaksi ke memori.
- Ekspor CSV dari hasil pencarian dan filter yang sedang ditampilkan melalui Android Storage Access Framework; maksimal 500 baris, UTF-8 dengan BOM, serta escaping formula spreadsheet pada field teks.
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

## Temuan audit UI/UX dan status penanganan

**Keparahan tinggi**
- Riwayat transaksi hanya memuat maksimal 500 hasil. Pengelompokan tanggal dan detail telah ditambahkan, tetapi pagination/muat bertahap untuk ribuan transaksi masih belum ditangani.
- Aplikasi belum memiliki backup/restore database penuh tervalidasi; ekspor CSV tetap bukan cadangan penuh.

**Keparahan sedang**
- UI sebelumnya memiliki satu layar orkestrasi besar, tema yang hanya mendefinisikan warna, transaksi tanpa detail, panduan statis, dan dashboard tanpa pilihan periode. Design tokens, reusable components, detail transaksi, pencarian panduan, serta filter periode kini ditambahkan.
- Tampilan aset, anggaran, target, dan navigasi sebelumnya kurang terstruktur. Halaman kini dipisah berdasarkan kategori dan menampilkan ringkasan/progres.

**Keparahan yang masih terbuka**
- Modul transaksi umum belum memiliki edit, hapus dengan koreksi, duplikasi/template, lampiran, dan impor.
- Belum ada pengaturan widget dashboard, penyembunyian saldo global, PIN/biometrik, enkripsi database, maupun backup/restore.
- Belum ada pengujian visual/UI automation menyeluruh, pengujian aksesibilitas otomatis, uji font sistem maksimum, uji layar kecil/besar, dan uji instalasi pada perangkat Android fisik.
- Modularisasi arsitektur UI lebih lanjut masih diperlukan; MainActivity masih menjadi koordinator layar dan dialog yang besar.

## Aturan akuntansi penting pada versi 0.9.0
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
- Grafik lengkap, perbandingan periode/workspace, analisis prediktif, rekomendasi cerdas, OCR, dan asisten bahasa sehari-hari.
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
