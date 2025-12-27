<?php
/**
 * API Endpoint: Save Ujian (Modified)
 * Method: POST
 * Body: JSON {no_induk, id_jilid, nilai_total, tanggal_ujian}
 * Deskripsi: Menyimpan data ujian dan MENGEMBALIKAN id_ujian yang baru dibuat
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
if (!isset($data['no_induk']) || !isset($data['id_jilid']) || !isset($data['nilai_total'])) {
    http_response_code(400);
    echo json_encode(array(
        'success' => false,
        'message' => 'Parameter no_induk, id_jilid, dan nilai_total wajib diisi'
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$no_induk = trim($data['no_induk']);
$id_jilid = (int)$data['id_jilid'];
$nilai_total = (float)$data['nilai_total'];
$tanggal_ujian = isset($data['tanggal_ujian']) ? $data['tanggal_ujian'] : date('Y-m-d');

try {
    // Hitung persentase (asumsi total bobot maksimal diambil dari soal)
    $sql_total_bobot = "SELECT SUM(bobot_nilai) as total_bobot FROM soal WHERE id_jilid = ?";
    $stmt_bobot = $conn->prepare($sql_total_bobot);
    $stmt_bobot->bind_param("i", $id_jilid);
    $stmt_bobot->execute();
    $result_bobot = $stmt_bobot->get_result();
    $row_bobot = $result_bobot->fetch_assoc();
    $total_bobot = (int)$row_bobot['total_bobot'];
    $stmt_bobot->close();
    
    // Hitung persentase
    $persentase = 0;
    if ($total_bobot > 0) {
        $persentase = ($nilai_total / $total_bobot) * 100;
    }
    
    // Tentukan status
    $status = 'selesai';
    
    // Insert data ujian
    $sql = "INSERT INTO ujian (no_induk, id_jilid, nilai_total, persentase, status, Tanggal_ujian) 
            VALUES (?, ?, ?, ?, ?, ?)";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("siddss", $no_induk, $id_jilid, $nilai_total, $persentase, $status, $tanggal_ujian);
    $stmt->execute();
    
    $id_ujian = $stmt->insert_id; // PENTING: Ambil ID ujian yang baru dibuat
    
    $stmt->close();
    $conn->close();
    
    // Return response dengan id_ujian
    echo json_encode(array(
        'success' => true,
        'message' => 'Data ujian berhasil disimpan',
        'data' => array(
            'id_ujian' => $id_ujian,  // Return id_ujian untuk keperluan kenaikan jilid
            'no_induk' => $no_induk,
            'id_jilid' => $id_jilid,
            'nilai_total' => $nilai_total,
            'persentase' => round($persentase, 2),
            'status' => $status,
            'tanggal_ujian' => $tanggal_ujian
        )
    ), JSON_UNESCAPED_UNICODE);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array(
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ), JSON_UNESCAPED_UNICODE);
}
?>
