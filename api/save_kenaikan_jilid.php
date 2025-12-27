<?php
/**
 * API Endpoint: Save Kenaikan Jilid
 * Method: POST
 * Body: JSON {no_induk, id_jilid_lama, id_jilid_baru, id_ujian, nilai_ujian, persentase_ujian}
 * Deskripsi: Menyimpan data kenaikan jilid siswa
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Koneksi database
require_once 'config.php';

// Ambil data JSON dari request body
$json_data = file_get_contents('php://input');
$data = json_decode($json_data, true);

// Validasi input
if (!isset($data['no_induk']) || !isset($data['id_jilid_baru'])) {
    http_response_code(400);
    echo json_encode(array(
        'success' => false,
        'message' => 'Parameter no_induk dan id_jilid_baru wajib diisi'
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$no_induk = trim($data['no_induk']);
$id_jilid_lama = isset($data['id_jilid_lama']) ? (int)$data['id_jilid_lama'] : null;
$id_jilid_baru = (int)$data['id_jilid_baru'];
$id_ujian = isset($data['id_ujian']) ? (int)$data['id_ujian'] : null;
$nilai_ujian = isset($data['nilai_ujian']) ? (float)$data['nilai_ujian'] : null;
$persentase_ujian = isset($data['persentase_ujian']) ? (float)$data['persentase_ujian'] : null;
$tanggal_naik = isset($data['tanggal_naik']) ? $data['tanggal_naik'] : date('Y-m-d');

try {
    // Mulai transaction
    $conn->begin_transaction();
    
    // Jika id_jilid_lama tidak diberikan, ambil dari database
    if ($id_jilid_lama === null) {
        $query_jilid_lama = "SELECT id_jilid_sekarang FROM siswa_jilid WHERE no_induk = ?";
        $stmt_check = $conn->prepare($query_jilid_lama);
        $stmt_check->bind_param("s", $no_induk);
        $stmt_check->execute();
        $result_check = $stmt_check->get_result();
        
        if ($result_check->num_rows > 0) {
            $row_check = $result_check->fetch_assoc();
            $id_jilid_lama = (int)$row_check['id_jilid_sekarang'];
        } else {
            $id_jilid_lama = 1; // Default jilid 1 untuk siswa baru
        }
        $stmt_check->close();
    }
    
    // Validasi: pastikan jilid baru lebih tinggi dari jilid lama
    if ($id_jilid_baru <= $id_jilid_lama) {
        throw new Exception("Jilid baru harus lebih tinggi dari jilid sekarang");
    }
    
    // Validasi: jilid maksimal adalah 7 (Al-Qur'an)
    if ($id_jilid_baru > 7) {
        throw new Exception("Jilid maksimal adalah 7 (Al-Qur'an)");
    }
    
    // Insert ke tabel kenaikan_jilid
    $keterangan = "Naik jilid dengan nilai " . number_format($persentase_ujian, 2) . "%";
    
    $sql_kenaikan = "INSERT INTO kenaikan_jilid 
                     (no_induk, id_jilid_lama, id_jilid_baru, id_ujian, nilai_ujian, persentase_ujian, tanggal_naik, keterangan) 
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    $stmt_kenaikan = $conn->prepare($sql_kenaikan);
    $stmt_kenaikan->bind_param("siiiddss", 
        $no_induk, 
        $id_jilid_lama, 
        $id_jilid_baru, 
        $id_ujian, 
        $nilai_ujian, 
        $persentase_ujian, 
        $tanggal_naik,
        $keterangan
    );
    $stmt_kenaikan->execute();
    $id_kenaikan = $stmt_kenaikan->insert_id;
    $stmt_kenaikan->close();
    
    // Update tabel siswa_jilid
    $sql_update = "UPDATE siswa_jilid 
                   SET id_jilid_sekarang = ?, tanggal_mulai = ? 
                   WHERE no_induk = ?";
    
    $stmt_update = $conn->prepare($sql_update);
    $stmt_update->bind_param("iss", $id_jilid_baru, $tanggal_naik, $no_induk);
    $stmt_update->execute();
    $affected_rows = $stmt_update->affected_rows;
    $stmt_update->close();
    
    // Jika siswa belum ada di tabel siswa_jilid, insert
    if ($affected_rows === 0) {
        $sql_insert = "INSERT INTO siswa_jilid (no_induk, id_jilid_sekarang, tanggal_mulai) 
                       VALUES (?, ?, ?)";
        $stmt_insert = $conn->prepare($sql_insert);
        $stmt_insert->bind_param("sis", $no_induk, $id_jilid_baru, $tanggal_naik);
        $stmt_insert->execute();
        $stmt_insert->close();
    }
    
    // Ambil nama jilid baru
    $sql_jilid = "SELECT nama_jilid FROM jilid WHERE id_jilid = ?";
    $stmt_jilid = $conn->prepare($sql_jilid);
    $stmt_jilid->bind_param("i", $id_jilid_baru);
    $stmt_jilid->execute();
    $result_jilid = $stmt_jilid->get_result();
    $row_jilid = $result_jilid->fetch_assoc();
    $nama_jilid_baru = $row_jilid['nama_jilid'];
    $stmt_jilid->close();
    
    // Commit transaction
    $conn->commit();
    
    echo json_encode(array(
        'success' => true,
        'message' => 'Selamat! Anda naik ke ' . $nama_jilid_baru,
        'data' => array(
            'id_kenaikan' => $id_kenaikan,
            'no_induk' => $no_induk,
            'id_jilid_lama' => $id_jilid_lama,
            'id_jilid_baru' => $id_jilid_baru,
            'nama_jilid_baru' => $nama_jilid_baru,
            'nilai_ujian' => $nilai_ujian,
            'persentase_ujian' => $persentase_ujian,
            'tanggal_naik' => $tanggal_naik
        )
    ), JSON_UNESCAPED_UNICODE);
    
    $conn->close();
    
} catch (Exception $e) {
    // Rollback jika ada error
    $conn->rollback();
    
    http_response_code(500);
    echo json_encode(array(
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ), JSON_UNESCAPED_UNICODE);
}
?>
