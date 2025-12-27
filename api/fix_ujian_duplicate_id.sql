-- Fix Duplicate Entry '0' for key 'PRIMARY' di tabel UJIAN
-- Jalankan script ini di phpMyAdmin

USE u221955505_dbtpq;

-- 1. Cek apakah ada record dengan id_ujian = 0
SELECT * FROM ujian WHERE id_ujian = 0;

-- 2. Hapus record dengan id_ujian = 0 (jika ada)
DELETE FROM ujian WHERE id_ujian = 0;

-- 3. Reset AUTO_INCREMENT untuk tabel ujian
ALTER TABLE ujian AUTO_INCREMENT = 1;

-- 4. Cek struktur tabel ujian (pastikan id_ujian adalah AUTO_INCREMENT)
SHOW CREATE TABLE ujian;

-- 5. Jika id_ujian BUKAN AUTO_INCREMENT, jalankan ini:
-- ALTER TABLE ujian MODIFY id_ujian INT(5) NOT NULL AUTO_INCREMENT;

-- Selesai! Sekarang coba insert ujian lagi
