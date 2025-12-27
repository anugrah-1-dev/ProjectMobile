<?php
/**
 * API Endpoint: Get Siswa Jilid
 * Method: GET
 * Parameter: no_induk (required)
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

// Include config
require_once 'config.php';

// Enable error logging
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

try {
    // Validasi parameter
    if (!isset($_GET['no_induk']) || empty($_GET['no_induk'])) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Parameter no_induk wajib diisi',
            'data' => null
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
    
    $no_induk = trim($_GET['no_induk']);
    
    // STEP 1: Cek di siswa_jilid
    $sql = "SELECT sj.no_induk, sj.id_jilid_sekarang, sj.tanggal_mulai, 
                   s.nama as nama_siswa
            FROM siswa_jilid sj
            LEFT JOIN santri s ON sj.no_induk = s.no_induk
            WHERE sj.no_induk = ?";
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("s", $no_induk);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        // Siswa found in siswa_jilid
        $row = $result->fetch_assoc();
        
        // Get jilid info
        $jilid_sql = "SELECT id_jilid, nama_jilid, urutan, passing_grade 
                      FROM jilid 
                      WHERE id_jilid = ?";
        $jilid_stmt = $conn->prepare($jilid_sql);
        $jilid_stmt->bind_param("i", $row['id_jilid_sekarang']);
        $jilid_stmt->execute();
        $jilid_result = $jilid_stmt->get_result();
        
        if ($jilid_result->num_rows > 0) {
            $jilid_row = $jilid_result->fetch_assoc();
            
            echo json_encode([
                'success' => true,
                'message' => 'Data siswa ditemukan',
                'data' => [
                    'no_induk' => $row['no_induk'],
                    'nama_siswa' => $row['nama_siswa'],
                    'id_jilid_sekarang' => (int)$row['id_jilid_sekarang'],
                    'nama_jilid' => $jilid_row['nama_jilid'],
                    'urutan' => (int)$jilid_row['urutan'],
                    'passing_grade' => (float)$jilid_row['passing_grade'],
                    'tanggal_mulai' => $row['tanggal_mulai'],
                    'is_new_student' => false
                ]
            ], JSON_UNESCAPED_UNICODE);
        } else {
            // Jilid not found - use default
            echo json_encode([
                'success' => true,
                'message' => 'Data siswa ditemukan',
                'data' => [
                    'no_induk' => $row['no_induk'],
                    'nama_siswa' => $row['nama_siswa'],
                    'id_jilid_sekarang' => (int)$row['id_jilid_sekarang'],
                    'nama_jilid' => 'Jilid ' . $row['id_jilid_sekarang'],
                    'urutan' => (int)$row['id_jilid_sekarang'],
                    'passing_grade' => 70.0,
                    'tanggal_mulai' => $row['tanggal_mulai'],
                    'is_new_student' => false
                ]
            ], JSON_UNESCAPED_UNICODE);
        }
        
        $jilid_stmt->close();
        
    } else {
        // STEP 2: Not in siswa_jilid, check santri table
        $santri_sql = "SELECT no_induk, nama FROM santri WHERE no_induk = ?";
        $santri_stmt = $conn->prepare($santri_sql);
        
        if (!$santri_stmt) {
            throw new Exception("Prepare santri failed: " . $conn->error);
        }
        
        $santri_stmt->bind_param("s", $no_induk);
        $santri_stmt->execute();
        $santri_result = $santri_stmt->get_result();
        
        if ($santri_result->num_rows > 0) {
            // Santri exists, create siswa_jilid record
            $santri_row = $santri_result->fetch_assoc();
            
            // Use INSERT IGNORE to avoid duplicate key error
            $insert_sql = "INSERT IGNORE INTO siswa_jilid (no_induk, id_jilid_sekarang, tanggal_mulai) 
                           VALUES (?, 1, CURDATE())";
            $insert_stmt = $conn->prepare($insert_sql);
            $insert_stmt->bind_param("s", $no_induk);
            $insert_stmt->execute();
            
            // Check if inserted or already exists
            $affected_rows = $insert_stmt->affected_rows;
            $insert_stmt->close();
            
            // Get jilid 1 info
            $jilid_sql = "SELECT id_jilid, nama_jilid, urutan, passing_grade 
                          FROM jilid WHERE id_jilid = 1";
            $jilid_result = $conn->query($jilid_sql);
            
            if ($jilid_result && $jilid_result->num_rows > 0) {
                $jilid_row = $jilid_result->fetch_assoc();
                $nama_jilid = $jilid_row['nama_jilid'];
                $urutan = (int)$jilid_row['urutan'];
                $passing_grade = (float)$jilid_row['passing_grade'];
            } else {
                // Default if jilid table doesn't exist
                $nama_jilid = 'Jilid 1';
                $urutan = 1;
                $passing_grade = 70.0;
            }
            
            echo json_encode([
                'success' => true,
                'message' => $affected_rows > 0 ? 'Siswa terdaftar di Jilid 1' : 'Siswa sudah terdaftar',
                'data' => [
                    'no_induk' => $no_induk,
                    'nama_siswa' => $santri_row['nama'],
                    'id_jilid_sekarang' => 1,
                    'nama_jilid' => $nama_jilid,
                    'urutan' => $urutan,
                    'passing_grade' => $passing_grade,
                    'tanggal_mulai' => date('Y-m-d'),
                    'is_new_student' => $affected_rows > 0
                ]
            ], JSON_UNESCAPED_UNICODE);
        } else {
            // Santri not found
            http_response_code(404);
            echo json_encode([
                'success' => false,
                'message' => 'Santri dengan no_induk ' . $no_induk . ' tidak ditemukan',
                'data' => null
            ], JSON_UNESCAPED_UNICODE);
        }
        
        $santri_stmt->close();
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    // Log error
    error_log("get_siswa_jilid.php Error: " . $e->getMessage());
    
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Server error: ' . $e->getMessage(),
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
}
?>
