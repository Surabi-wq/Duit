# Duit 1.3.1 — Catatan Pembaruan

## Perbaikan keandalan dan integritas
- Restore menutup instance Room lama dan UI mengganti FinanceRepository serta kolektor Flow ke instance database baru setelah pemulihan berhasil.
- Validasi arsip backup dibatasi ukuran database dan manifest, memeriksa daftar entri dan properti manifest, checksum SHA-256, SQLite integrity_check, foreign_key_check, dan rentang user_version yang didukung.
- Restore membuat salinan pengaman sebelum mengganti database. Jika proses gagal, file sidecar WAL/SHM dibuang dan salinan database lama diverifikasi serta dibuka kembali. Salinan pengaman disimpan di noBackupFilesDir agar tidak ikut terhapus saat cache dibersihkan.
- Output stream ekspor backup ditutup dengan benar saat alur pembuatan gagal atau berhasil.

## Konsistensi keuangan dan UI
- Dashboard dan layar Aset menggunakan NetWorthCalculator yang sama.
- Unit test menguji dana target yang telah dialokasikan, pengurangan kewajiban, dan data kosong.
- Progres target memiliki pelindung pembagian nol untuk database/arsip yang berisi target tidak valid.
- Panduan backup/restore di pusat bantuan diselaraskan dengan kemampuan sebenarnya.

## Kompatibilitas dan batasan
- Room tetap versi 5; tidak ada migrasi skema baru pada pembaruan ini.
- Rumus saldo transaksi dan definisi kekayaan bersih tidak diubah.
- Arsip belum dienkripsi dan belum mencakup lampiran. Backup terjadwal, riwayat backup, dan restore selektif workspace belum tersedia.
- CI memverifikasi unit test dan build Release. Uji instalasi pada perangkat Android fisik, restore lintas perangkat/versi, dan uji beban database besar masih diperlukan.
