# Duit 1.7.0 — Spesifikasi Final dan Rencana Implementasi

Status dokumen: disetujui sebagai spesifikasi produk; implementasi fitur belum dinyatakan selesai.
Target aplikasi: Duit 1.7.0 (versionCode 17)
Repository: Surabi-wq/Duit
Baseline source yang diaudit: main @ f3c6165e4c05cc92c6bacc6d004cf491f51c9c14
Nama APK target: Duit-1.7.0.apk

## 1. Prinsip implementasi

- Pertahankan applicationId com.surabi.duit dan kemampuan Duit 1.6.0.
- Offline-first: semua alur inti berjalan tanpa internet.
- Multi-profil dengan isolasi data ketat. Profil bukan sekadar filter UI.
- Semua mutasi finansial atomik, tervalidasi, tercatat di audit log, dan dapat direkonsiliasi.
- Tidak ada destructive migration, penghapusan histori diam-diam, atau data contoh yang menyamar sebagai data nyata.
- Fitur hanya dinyatakan selesai jika alur UI terhubung ke database/worker/API yang benar, diuji, dan memiliki state loading/error/empty.
- Integrasi eksternal dan cloud bersifat opsional serta dapat dinonaktifkan.

## 2. Spesifikasi produk yang disepakati

### Arsitektur dan profil
- Offline-first; sinkronisasi manual dan otomatis.
- Multi-profil: personal, keluarga, bisnis, proyek, dan kustom.
- Tidak ada kunci khusus per profil.
- Isolasi ketat untuk rekening, transaksi, anggaran, laporan, lampiran, backup, pencarian, ekspor, dan sinkronisasi.

### Transaksi dan keuangan
- Input manual, transaksi berulang advanced, impor file, dan otomatisasi lokal.
- Lampiran foto, PDF, dokumen, dan catatan.
- Validasi ketat, deteksi duplikasi, histori perubahan/audit.
- Anggaran enterprise dengan periode kustom, seluruh tingkat peringatan, proyeksi, dan rollover fleksibel.
- Tagihan enterprise; utang/piutang dengan pihak, bunga, tenor, cicilan, jatuh tempo, pembayaran parsial, dan histori.
- Pengingat dasar pada tanggal jatuh tempo; pembayaran penuh/parsial/cicilan/pelunasan awal/terjadwal.
- Laporan enterprise: arus kas, laba rugi, neraca pribadi, perbandingan periode dan profil.
- Analitik tren, anomali, pola transaksi, proyeksi arus kas, dan rekomendasi lokal.
- Ekspor PDF, Excel, JSON; jadwal laporan manual/harian/mingguan/bulanan/kustom.
- Aset enterprise termasuk aset likuid/tetap, apresiasi, depresiasi, dan integrasi neraca.
- Investasi enterprise: portofolio, dividen, biaya, valuasi historis, alokasi, performa.
- Tujuan keuangan: tabungan, dana darurat, pembelian aset, pendidikan, pensiun, kustom.
- Valuasi manual; histori nilai wajib dipertahankan.

### Data, keamanan, dan integrasi
- App lock: none; enkripsi basic.
- Backup enterprise: terjadwal, multi-versi, integritas, enkripsi, pemulihan selektif.
- Audit log semua aktivitas penting.
- Recovery advanced per versi/profil/kategori.
- Sinkronisasi cloud dan perangkat lokal, manual/otomatis; resolusi konflik enterprise.
- Impor CSV, Excel, JSON.
- Integrasi eksternal opsional dan dapat dikontrol.

### Regionalisasi dan UI
- Multi-mata uang per rekening/transaksi; mata uang utama IDR.
- Kurs manual dan historis; nilai asli tidak berubah saat kurs diperbarui.
- Bahasa Indonesia/Inggris; regional mengikuti perangkat.
- UI premium, tema terang/gelap, warna kustom, navigasi adaptif, dashboard widget penuh dan dapat disusun ulang.
- Notifikasi basic melalui in-app/push/suara/getaran; jadwal fleksibel, pusat notifikasi lengkap, privasi per kategori.
- Pencarian enterprise, filter lengkap, operasi massal, arsip enterprise.
- Kategori enterprise, bertingkat, tag/label/atribut, kategorisasi otomatis berbasis aturan, template personal/keluarga/bisnis/proyek/kustom.
- Aksesibilitas advanced; kompatibilitas Android luas; performa enterprise.
- Onboarding advanced; pusat bantuan offline lengkap.
- Widget Android lengkap dan shortcut/quick actions.
- Identitas visual baru (nama Duit dipertahankan), ikon premium.
- Pengujian enterprise; izin minimal; distribusi Release APK.

## 3. Audit baseline repository

Pada saat audit, branch main berada di commit f3c6165e4c05cc92c6bacc6d004cf491f51c9c14. Source adalah Kotlin, Jetpack Compose/Material 3, Room/SQLite, JVM 17, minSdk 23, compile/target SDK 36. Application ID adalah com.surabi.duit. Version source adalah 1.6.0, versionCode 16, dan schema Room 5.

Fondasi yang tersedia: workspace, rekening, kategori dasar, transaksi pemasukan/pengeluaran/transfer, anggaran, target tabungan, aset, kewajiban, piutang, audit log, transaksi berulang dasar, dashboard/peringatan, Paging 3, ekspor CSV, backup/restore database penuh, pengaturan rekening dan transfer langsung.

Kesenjangan yang dicatat dalam docs/FEATURE-STATUS.md dan dikonfirmasi melalui source:
- Workspace saat ini belum sama dengan model profil enterprise yang memiliki tipe, identitas, metadata, dan aturan isolasi eksplisit.
- Nominal saat ini memakai Long IDR; belum ada currency code, amount asli, kurs historis, dan konversi.
- Room schema 5; migrasi eksplisit diperlukan untuk setiap tabel/kolom baru.
- FinanceBackupManager mengunci DATABASE_VERSION ke 5 dan menolak skema yang tidak sama persis; perlu backup format versioning dan migrasi/compatibility strategy.
- Backup .duitbackup saat ini belum terenkripsi.
- Belum ada attachment repository, import CSV/Excel/JSON, edit transaksi umum, atau workflow massal lengkap.
- Belum ada laporan PDF/Excel, penjadwalan laporan, portofolio investasi lengkap, depresiasi/aset historis lengkap, atau model tagihan/cicilan enterprise.
- Belum ada cloud/device sync atau conflict resolver.
- Notifikasi Android, notification center, aturan privasi, dan quiet hours belum menjadi sistem lengkap.
- Manifest saat ini belum mendeklarasikan permission/receiver/service notifikasi atau widget; izin harus ditambah hanya sesuai kebutuhan fitur.
- Release build saat ini menggunakan debug signingConfig dan bukan kunci rilis produksi.
- CI menjalankan unit test dan assembleRelease, tetapi belum mencakup instrumentation/UI/accessibility/device tests.
- Icon launcher saat ini vector sederhana dan perlu redesain premium; label aplikasi adalah Duit.

## 4. Konflik dan keputusan arsitektural wajib

1. Profil vs workspace: buat ProfileEntity sebagai boundary identitas; migrasikan workspace lama menjadi profil kompatibel atau relasikan workspace ke profileId dengan pemetaan deterministik. Jangan menggabungkan data lintas profil.
2. Uang dan mata uang: simpan amountMinor + currencyCode pada transaksi/rekening. Kurs historis disimpan sebagai snapshot (rate, base, quote, effectiveAt, source=manual); konversi laporan memakai snapshot atau kurs pilihan yang jelas. Hindari Float/Double untuk aritmetika uang; gunakan integer minor units/BigDecimal terkontrol.
3. Akuntansi: transfer internal bukan income/expense; pokok utang bukan beban; pencairan pinjaman bukan pendapatan; nilai rekening tidak dihitung dua kali sebagai aset. Pisahkan cashflow dari P&L dan balance sheet.
4. Backup dan migrasi: buat format backup berversi terpisah dari schema Room; dukung validasi dan migrasi dari backup versi 1.5/1.6 sebelum restore. Selalu validasi penuh sebelum transaksi restore dan pertahankan rollback atomik.
5. Enkripsi tanpa app lock: kunci backup tidak boleh ditanam di source. Pilih kunci acak per backup dengan passphrase pengguna atau Android Keystore dengan alur recovery/ekspor kunci yang jelas. Tanpa kredensial yang dapat dipulihkan, backup terenkripsi dapat hilang aksesnya.
6. Sinkronisasi: butuh stable UUID lintas perangkat, change journal, tombstone, version/vector metadata, idempotency key, dan aturan konflik. Cloud memerlukan endpoint/backend, autentikasi dan biaya layanan; belum ada backend terkonfigurasi. Local device sync memerlukan transport yang disepakati (mis. file/QR/LAN); jangan mengklaim cloud sync aktif tanpa backend.
7. Background work: WorkManager bukan jam presisi; notifikasi jadwal harus menjelaskan batas Doze/battery optimization dan menggunakan exact alarm hanya bila benar-benar diperlukan dan diizinkan.
8. Widget: Android AppWidget/Glance memerlukan receiver/provider, update policy, deep links, dan data refresh; tampilkan nominal sensitif sesuai preferensi privasi.
9. Impor/ekspor: CSV/Excel/JSON perlu preview, mapping kolom, validasi, idempotensi, laporan baris gagal, dan rollback/batch transaction. PDF hanya untuk ekspor/laporan, bukan impor.
10. Kompatibilitas luas: minSdk 23 baseline, namun tiap library/format widget/notifikasi harus diperiksa terhadap API 23+. Target SDK 36 tetap dipertahankan kecuali ada alasan teknis.
11. Signing: debug signing yang dipakai CI tidak boleh disebut production-signed. APK CI dapat menjadi release variant untuk pengujian; distribusi produksi membutuhkan keystore privat yang tidak pernah disimpan di repository dan secrets GitHub.
12. Lingkup: semua spesifikasi tidak realistis untuk dianggap satu fitur kecil. Implementasi dipecah menjadi gelombang teruji di bawah versi target 1.7.0, dengan feature flags/vertical slices bila perlu; jangan menyatakan Enterprise complete sebelum semua acceptance criteria lulus.

## 5. Tahapan implementasi repository

### Tahap 0 — pengamanan baseline dan spesifikasi
- Simpan baseline commit dan dokumentasikan checksum/backup prosedur.
- Buat branch kerja feature/duit-1.7.0; jangan langsung menimpa main untuk perubahan skema besar.
- Tambahkan test fixture untuk database v5 dan backup .duitbackup 1.5/1.6.
- Tetapkan katalog use case, data ownership, migration map, acceptance criteria, serta matriks kompatibilitas.

### Tahap 1 — fondasi data dan migrasi
- Modularisasi repository/use-case/state; pisahkan UI dari operasi database.
- ProfileEntity dan relasi isolasi; migrasi workspace lama tanpa kehilangan data.
- Currency, exchange-rate history, attachment metadata, category hierarchy, archive metadata, recurring execution ledger, notification preferences, report schedules, valuation history.
- Room migrations eksplisit v5→v6+; migration tests dari seluruh versi baseline.
- Backup format v2 dengan checksum, enkripsi, multi-version dan kompatibilitas restore.

### Tahap 2 — transaksi dan klasifikasi
- Edit/reversal transaksi aman, lampiran, impor CSV/Excel/JSON, deteksi duplikasi.
- Kategori bertingkat, aturan kategorisasi, template, operasi massal, pencarian lintas data.
- Recurring scheduler idempotent dengan history, retry dan audit.

### Tahap 3 — planning, liabilities, assets, investments
- Anggaran custom, rollover, proyeksi dan alert.
- Bills, cicilan, bunga/biaya, pembayaran parsial/awal, rekonsiliasi transaksi.
- Asset valuation history, depreciation/appreciation, portfolio/dividend/fees/performance.
- Goal projections dan neraca terintegrasi.

### Tahap 4 — laporan, analitik, regionalisasi
- Cashflow/P&L/balance sheet dan perbandingan profil/periode.
- PDF/Excel/JSON exports, report scheduler.
- Multi-currency conversion dengan kurs historis.
- Analitik/anomali/proyeksi lokal dan rekomendasi yang tidak mengubah data otomatis.

### Tahap 5 — notifikasi, sinkronisasi, integrasi
- Notification channels, center, privacy, quiet hours, WorkManager scheduling.
- Sync engine dengan UUID, journal, idempotency, conflict resolver.
- Local device transport terlebih dahulu; cloud hanya setelah backend/auth/retention/security disiapkan.
- Integrasi eksternal opt-in.

### Tahap 6 — UI/UX, onboarding, widgets
- Redesign premium dan launcher icon, tema/light-dark, bahasa ID/EN.
- Dashboard layout editor, widget Android, quick actions.
- Onboarding, offline help center, accessibility audit, adaptive navigation.

### Tahap 7 — quality gates dan release
- Unit + DAO/instrumentation + migration + backup/restore + sync conflict + import/export tests.
- UI automation, accessibility checks, performance/memory, API 23/modern Android matrix.
- Regression test saldo, transfer, utang, aset, multi-currency dan isolasi profil.
- CI Release APK bernama Duit-1.7.0.apk; upload artifact, SHA-256, release notes.
- Gunakan keystore produksi hanya melalui GitHub Actions secrets bila user menyediakan/menyiapkan signing credentials. Tanpa itu, labeli build sebagai CI-signed/testing.
- Verifikasi instalasi/upgrade dan smoke test pada perangkat Android fisik sebelum menyatakan rilis publik siap.

## 6. Gerbang penerimaan

- Semua fitur yang ditandai selesai memiliki UI → use case → repository/DAO → persistence yang berfungsi.
- Isolasi profil dites negatif (tidak ada query/export/backup/sync lintas profil).
- Migrasi database v5 dan restore backup lama lulus tanpa kehilangan data.
- Saldo rekening, transfer, utang/piutang, target, aset dan laporan direkonsiliasi.
- Import bersifat preview-first, idempotent, dan dapat melaporkan baris gagal.
- Backup terenkripsi terverifikasi; kunci tidak hardcoded.
- Sinkronisasi tidak menggandakan transaksi dan memiliki resolusi konflik yang dapat diaudit.
- Unit, integration, migration, UI, accessibility, performance, dan release build lulus.
- Nama file artifact tepat Duit-1.7.0.apk; applicationId tetap com.surabi.duit.

## 7. Batas klaim

Dokumen ini adalah spesifikasi dan rencana kerja yang telah disimpan. Dokumen ini tidak menyatakan bahwa fitur 1.7.0 telah diimplementasikan, diuji, atau dirilis. Status implementasi harus diperbarui berdasarkan commit, CI run, artifact, dan hasil uji nyata.
