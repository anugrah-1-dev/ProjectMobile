<?php
/**
 * Simplified Get Siswa Jilid - For Testing
 * Remove all complex logic to find error
 */

// Enable error display
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

try {
    // Step 1: Include config
    require_once 'config.php';
    
    // Step 2: Get parameter
    $no_induk = isset($_GET['no_induk']) ? trim($_GET['no_induk']) : '';
    
    if (empty($no_induk)) {
        echo json_encode([
            'success' => false,
            'message' => 'Parameter no_induk tidak boleh kosong'
        ]);
        exit;
    }
    
    // Step 3: Simple query
    $sql = "SELECT * FROM santri WHERE no_induk = ? LIMIT 1";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $no_induk);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        echo json_encode([
            'success' => true,
            'message' => 'Santri ditemukan',
            'data' => [
                'no_induk' => $row['no_induk'],
                'nama_siswa' => $row['nama'],
                'id_jilid_sekarang' => 1,
                'nama_jilid' => 'JILID I',
                'urutan' => 1,
                'passing_grade' => 70.0,
                'tanggal_mulai' => date('Y-m-d'),
                'is_new_student' => false
            ]
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Santri dengan no_induk ' . $no_induk . ' tidak ditemukan'
        ]);
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine()
    ]);
}
?>
