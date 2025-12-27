<?php
/**
 * API Endpoint: Get All Jilid
 * Method: GET
 * Deskripsi: Mengambil semua data jilid (1-7)
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

// Koneksi database
require_once 'config.php';

try {
    // Query untuk mengambil semua jilid diurutkan berdasarkan urutan
    $sql = "SELECT id_jilid, nama_jilid, urutan, deskripsi, passing_grade, created_at, updated_at 
            FROM jilid 
            ORDER BY urutan ASC";
    
    $stmt = $conn->prepare($sql);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $jilidList = array();
    
    while ($row = $result->fetch_assoc()) {
        $jilidList[] = array(
            'id_jilid' => (int)$row['id_jilid'],
            'nama_jilid' => $row['nama_jilid'],
            'urutan' => (int)$row['urutan'],
            'deskripsi' => $row['deskripsi'],
            'passing_grade' => (float)$row['passing_grade'],
            'created_at' => $row['created_at'],
            'updated_at' => $row['updated_at']
        );
    }
    
    if (count($jilidList) > 0) {
        echo json_encode(array(
            'success' => true,
            'message' => 'Data jilid berhasil diambil',
            'data' => $jilidList
        ), JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(array(
            'success' => false,
            'message' => 'Data jilid tidak ditemukan',
            'data' => null
        ), JSON_UNESCAPED_UNICODE);
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array(
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'data' => null
    ), JSON_UNESCAPED_UNICODE);
}
?>
