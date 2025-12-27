<?php
/**
 * API Endpoint: Update Nama Siswa
 * Method: POST
 * Body: JSON {no_induk, nama_siswa}
 * Deskripsi: Update nama siswa di tabel siswa_jilid
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
if (!isset($data['no_induk']) || !isset($data['nama_siswa'])) {
    http_response_code(400);
    echo json_encode(array(
        'success' => false,
        'message' => 'Parameter no_induk dan nama_siswa wajib diisi'
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$no_induk = trim($data['no_induk']);
$nama_siswa = trim($data['nama_siswa']);

try {
    // Update nama siswa
    $sql = "UPDATE siswa_jilid SET nama_siswa = ? WHERE no_induk = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $nama_siswa, $no_induk);
    $stmt->execute();
    
    if ($stmt->affected_rows > 0) {
        echo json_encode(array(
            'success' => true,
            'message' => 'Nama siswa berhasil diupdate'
        ), JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(array(
            'success' => false,
            'message' => 'Tidak ada data yang diupdate'
        ), JSON_UNESCAPED_UNICODE);
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array(
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ), JSON_UNESCAPED_UNICODE);
}
?>
