# Audit dan pembaruan UI/UX Duit 1.4.0

Tanggal: 26 November 2026  
Basis: branch `main`, parent `719d7d6f2f505e21cebc2c1a7794a3b935c5b62a`  
Ruang lingkup rilis: duplikasi aman transaksi umum.

## Temuan dan keputusan

| Area | Temuan | Tindakan |
|---|---|---|
| Detail transaksi | Detail menyediakan lihat dan pembatalan, tetapi tidak ada jalur menyalin transaksi | Menambah tindakan Duplikasi pada pemasukan, pengeluaran, dan transfer |
| Integritas data | Pergerakan dana target, pembayaran utang, dan penerimaan piutang memiliki semantik keuangan khusus | Jenis khusus tidak ditampilkan sebagai pilihan duplikasi transaksi umum |
| Konsistensi data | Salinan berisiko memakai akun/kategori yang tidak valid pada workspace aktif | Form memakai validasi repository yang sama sebelum insert |
| Histori | Duplikasi tidak boleh mengubah catatan sumber | Salinan dibuat sebagai ID baru dengan timestamp baru; sumber tetap utuh |

## Perubahan kode

- `MainActivity.kt`: tombol Duplikasi pada detail transaksi, penyalinan draft, dan prefill formulir.
- `TransactionDuplicationPolicy.kt`: allowlist jenis transaksi yang dapat disalin.
- `TransactionDuplicationPolicyTest.kt`: uji jenis standar dan penolakan jenis internal/tidak dikenal.
- `app/build.gradle.kts`: versi aplikasi 1.4.0 / versionCode 14.
- `.github/workflows/android.yml`: nama APK rilis `Duit-1.4.0.apk`.

## Batasan yang tetap terbuka

- Edit transaksi dengan mekanisme koreksi/void, impor transaksi, lampiran, rekonsiliasi formal, dan reversal jurnal belum diimplementasikan.
- Akuntansi double-entry, laporan akuntansi formal, backup/restore terenkripsi, autentikasi PIN/biometrik, dan enkripsi database tetap backlog.
- Duplikasi dibatasi ke pemasukan, pengeluaran, dan transfer. Pergerakan dana target, kewajiban, dan piutang tidak dapat disalin sebagai transaksi umum karena aturan saldo/utang/piutang yang spesifik.
- Pengujian perangkat fisik, TalkBack, ukuran font maksimum, dan berbagai ukuran layar perlu dilakukan pada perangkat Android.

## Verifikasi

- Unit test policy ditambahkan; hasil aktual mengikuti workflow GitHub Actions setelah commit.
- Perubahan tidak menyentuh entitas, versi Room, migrasi, query saldo, atau rumus kekayaan bersih.
- Build Release dan unit test CI menjadi gerbang penerimaan; APK hanya dinyatakan tersedia setelah artefak workflow sukses diverifikasi.
