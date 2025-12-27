-- =====================================================
-- FIX COMPREHENSIVE: Duplicate Entry '0' for ALL Tables
-- Jalankan script ini SEKALI untuk fix semua masalah
-- =====================================================

USE u221955505_dbtpq;

-- =====================================================
-- 1. TABEL UJIAN
-- =====================================================
-- Hapus record dengan id_ujian = 0
DELETE FROM ujian WHERE id_ujian = 0;

-- Set AUTO_INCREMENT
ALTER TABLE ujian MODIFY id_ujian INT(5) NOT NULL AUTO_INCREMENT;
ALTER TABLE ujian AUTO_INCREMENT = 1;

-- =====================================================
-- 2. TABEL SISWA_JILID  
-- =====================================================
-- Hapus record dengan id = 0
DELETE FROM siswa_jilid WHERE id = 0;

-- Set AUTO_INCREMENT
ALTER TABLE siswa_jilid MODIFY id INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE siswa_jilid AUTO_INCREMENT = 1;

-- =====================================================
-- 3. TABEL KENAIKAN_JILID
-- =====================================================
-- Hapus record dengan id_kenaikan = 0
DELETE FROM kenaikan_jilid WHERE id_kenaikan = 0;

-- Set AUTO_INCREMENT
ALTER TABLE kenaikan_jilid MODIFY id_kenaikan INT(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE kenaikan_jilid AUTO_INCREMENT = 1;

-- =====================================================
-- 4. VERIFICATION
-- =====================================================
-- Cek bahwa tidak ada lagi record dengan id = 0
SELECT 'ujian' as tabel, COUNT(*) as count_id_0 FROM ujian WHERE id_ujian = 0
UNION ALL
SELECT 'siswa_jilid', COUNT(*) FROM siswa_jilid WHERE id = 0
UNION ALL
SELECT 'kenaikan_jilid', COUNT(*) FROM kenaikan_jilid WHERE id_kenaikan = 0;

-- Harusnya semua count_id_0 = 0

-- =====================================================
-- 5. CEK AUTO_INCREMENT STATUS
-- =====================================================
SELECT 
    TABLE_NAME,
    AUTO_INCREMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'u221955505_dbtpq'
  AND TABLE_NAME IN ('ujian', 'siswa_jilid', 'kenaikan_jilid');

-- Harusnya semua AUTO_INCREMENT >= 1

-- =====================================================
-- SELESAI! Sekarang coba aplikasi lagi
-- =====================================================
