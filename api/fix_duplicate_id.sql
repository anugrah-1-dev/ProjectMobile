-- Fix untuk Duplicate Entry '0' for key 'PRIMARY'
-- Jalankan script ini di phpMyAdmin

USE u221955505_dbtpq;

-- 1. Hapus record dengan id = 0 (jika ada)
DELETE FROM siswa_jilid WHERE id = 0;

-- 2. Reset AUTO_INCREMENT
ALTER TABLE siswa_jilid AUTO_INCREMENT = 1;

-- 3. Cek apakah ada record dengan id = 0
SELECT * FROM siswa_jilid WHERE id = 0;

-- Seharusnya return empty (0 rows)
