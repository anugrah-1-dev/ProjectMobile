-- =====================================================
-- DATABASE SCHEMA: Sistem Kenaikan Jilid TPQ
-- Database: u221955505_db_posylike
-- =====================================================

-- -----------------------------------------------------
-- Tabel: jilid
-- Deskripsi: Menyimpan data 7 tingkatan jilid pembelajaran
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jilid` (
  `id_jilid` INT(11) NOT NULL AUTO_INCREMENT,
  `nama_jilid` VARCHAR(50) NOT NULL,
  `urutan` INT(11) NOT NULL COMMENT 'Urutan level: 1-7',
  `deskripsi` TEXT DEFAULT NULL,
  `passing_grade` DECIMAL(5,2) DEFAULT 70.00 COMMENT 'Nilai minimum untuk lulus (%)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_jilid`),
  UNIQUE KEY `unique_urutan` (`urutan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insert data 7 jilid
INSERT INTO `jilid` (`id_jilid`, `nama_jilid`, `urutan`, `deskripsi`, `passing_grade`) VALUES
(1, 'Jilid 1', 1, 'Pembelajaran dasar huruf Hijaiyah dan tanda baca', 70.00),
(2, 'Jilid 2', 2, 'Pembelajaran lanjutan huruf Hijaiyah dan tajwid dasar', 70.00),
(3, 'Jilid 3', 3, 'Pembelajaran tajwid tingkat menengah', 70.00),
(4, 'Jilid 4', 4, 'Pembelajaran tajwid lanjutan dan bacaan panjang', 70.00),
(5, 'Jilid 5', 5, 'Pembelajaran hukum bacaan nun sukun dan tanwin', 70.00),
(6, 'Jilid 6', 6, 'Pembelajaran mad dan waqaf', 70.00),
(7, 'Al-Qur\'an', 7, 'Pembelajaran membaca Al-Qur\'an lengkap', 70.00);

-- -----------------------------------------------------
-- Tabel: siswa_jilid
-- Deskripsi: Tracking jilid siswa saat ini
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `siswa_jilid` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `no_induk` VARCHAR(20) NOT NULL,
  `nama_siswa` VARCHAR(100) DEFAULT NULL,
  `id_jilid_sekarang` INT(11) NOT NULL DEFAULT 1 COMMENT 'Jilid yang sedang dipelajari',
  `tanggal_mulai` DATE DEFAULT NULL COMMENT 'Tanggal mulai di jilid ini',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_no_induk` (`no_induk`),
  KEY `fk_siswa_jilid_jilid` (`id_jilid_sekarang`),
  CONSTRAINT `fk_siswa_jilid_jilid` FOREIGN KEY (`id_jilid_sekarang`) REFERENCES `jilid` (`id_jilid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------
-- Tabel: kenaikan_jilid
-- Deskripsi: Riwayat kenaikan jilid siswa
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `kenaikan_jilid` (
  `id_kenaikan` INT(11) NOT NULL AUTO_INCREMENT,
  `no_induk` VARCHAR(20) NOT NULL,
  `id_jilid_lama` INT(11) NOT NULL COMMENT 'Jilid sebelumnya',
  `id_jilid_baru` INT(11) NOT NULL COMMENT 'Jilid yang baru',
  `id_ujian` INT(5) DEFAULT NULL COMMENT 'Referensi ke tabel ujian',
  `nilai_ujian` DECIMAL(5,2) DEFAULT NULL COMMENT 'Nilai ujian yang membuat naik jilid',
  `persentase_ujian` DECIMAL(5,2) DEFAULT NULL COMMENT 'Persentase nilai ujian',
  `tanggal_naik` DATE NOT NULL,
  `keterangan` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_kenaikan`),
  KEY `fk_kenaikan_no_induk` (`no_induk`),
  KEY `fk_kenaikan_jilid_lama` (`id_jilid_lama`),
  KEY `fk_kenaikan_jilid_baru` (`id_jilid_baru`),
  KEY `fk_kenaikan_ujian` (`id_ujian`),
  CONSTRAINT `fk_kenaikan_jilid_lama` FOREIGN KEY (`id_jilid_lama`) REFERENCES `jilid` (`id_jilid`),
  CONSTRAINT `fk_kenaikan_jilid_baru` FOREIGN KEY (`id_jilid_baru`) REFERENCES `jilid` (`id_jilid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------
-- Modifikasi Tabel: ujian (jika belum ada)
-- Deskripsi: Pastikan tabel ujian sesuai dengan struktur yang ada
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ujian` (
  `id_ujian` INT(5) NOT NULL AUTO_INCREMENT,
  `no_induk` VARCHAR(20) NOT NULL,
  `id_jilid` INT(11) NOT NULL,
  `nilai_total` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `persentase` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Persentase nilai (0-100)',
  `status` ENUM('pending','selesai','dibatalkan') NOT NULL DEFAULT 'pending',
  `Tanggal_ujian` VARCHAR(20) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_ujian`),
  KEY `idx_no_induk` (`no_induk`),
  KEY `idx_id_jilid` (`id_jilid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =====================================================
-- VIEWS (Opsional - untuk kemudahan query)
-- =====================================================

-- View untuk melihat progres siswa
CREATE OR REPLACE VIEW `view_progres_siswa` AS
SELECT 
  sj.no_induk,
  sj.nama_siswa,
  sj.id_jilid_sekarang,
  j.nama_jilid,
  j.urutan,
  sj.tanggal_mulai,
  COUNT(kj.id_kenaikan) as total_kenaikan
FROM siswa_jilid sj
LEFT JOIN jilid j ON sj.id_jilid_sekarang = j.id_jilid
LEFT JOIN kenaikan_jilid kj ON sj.no_induk = kj.no_induk
GROUP BY sj.no_induk, sj.nama_siswa, sj.id_jilid_sekarang, j.nama_jilid, j.urutan, sj.tanggal_mulai;

-- View untuk riwayat kenaikan jilid
CREATE OR REPLACE VIEW `view_riwayat_kenaikan` AS
SELECT 
  kj.id_kenaikan,
  kj.no_induk,
  sj.nama_siswa,
  jl.nama_jilid as jilid_lama,
  jb.nama_jilid as jilid_baru,
  kj.nilai_ujian,
  kj.persentase_ujian,
  kj.tanggal_naik,
  kj.keterangan
FROM kenaikan_jilid kj
LEFT JOIN siswa_jilid sj ON kj.no_induk = sj.no_induk
LEFT JOIN jilid jl ON kj.id_jilid_lama = jl.id_jilid
LEFT JOIN jilid jb ON kj.id_jilid_baru = jb.id_jilid
ORDER BY kj.tanggal_naik DESC;

-- =====================================================
-- END OF SCHEMA
-- =====================================================
