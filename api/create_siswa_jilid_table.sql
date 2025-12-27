-- =====================================================
-- SCRIPT: Buat tabel siswa_jilid untuk tracking jilid santri
-- Database: u221955505_dbtpq
-- =====================================================

-- Buat tabel siswa_jilid jika belum ada
CREATE TABLE IF NOT EXISTS `siswa_jilid` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `no_induk` VARCHAR(20) NOT NULL COMMENT 'Referensi ke santri.no_induk',
  `id_jilid_sekarang` INT(11) NOT NULL DEFAULT 1 COMMENT 'Jilid yang sedang dipelajari',
  `tanggal_mulai` DATE DEFAULT NULL COMMENT 'Tanggal mulai di jilid ini',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_no_induk` (`no_induk`),
  KEY `fk_siswa_jilid_jilid` (`id_jilid_sekarang`),
  CONSTRAINT `fk_siswa_jilid_jilid` FOREIGN KEY (`id_jilid_sekarang`) REFERENCES `jilid` (`id_jilid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
COMMENT='Tracking jilid santri saat ini (join dengan tabel santri untuk nama)';

-- Tambahkan data awal dari tabel santri (opsional)
-- Uncomment jika ingin auto-populate semua santri dengan jilid 1 sebagai default
-- INSERT IGNORE INTO siswa_jilid (no_induk, id_jilid_sekarang, tanggal_mulai)
-- SELECT no_induk, 1 as id_jilid_sekarang, CURDATE() as tanggal_mulai 
-- FROM santri;

-- =====================================================
-- Selesai
-- =====================================================
