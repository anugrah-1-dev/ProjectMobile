<?php
/**
 * Comprehensive API Test
 * Test semua endpoint yang dibutuhkan aplikasi
 */

header('Content-Type: application/json; charset=utf-8');

$results = [
    'test_time' => date('Y-m-d H:i:s'),
    'tests' => []
];

// Test 1: config.php
try {
    require_once 'config.php';
    $results['tests']['config'] = [
        'status' => 'OK',
        'message' => 'Config loaded, database connected'
    ];
} catch (Exception $e) {
    $results['tests']['config'] = [
        'status' => 'FAILED',
        'error' => $e->getMessage()
    ];
    echo json_encode($results, JSON_PRETTY_PRINT);
    exit;
}

// Test 2: Tabel santri
try {
    $sql = "SELECT COUNT(*) as count FROM santri";
    $result = $conn->query($sql);
    $row = $result->fetch_assoc();
    $results['tests']['tabel_santri'] = [
        'status' => 'OK',
        'count' => (int)$row['count']
    ];
    
    // Get sample data
    $sql_sample = "SELECT no_induk, nama FROM santri LIMIT 3";
    $sample_result = $conn->query($sql_sample);
    $samples = [];
    while ($s = $sample_result->fetch_assoc()) {
        $samples[] = $s;
    }
    $results['tests']['tabel_santri']['sample_data'] = $samples;
    
} catch (Exception $e) {
    $results['tests']['tabel_santri'] = [
        'status' => 'FAILED',
        'error' => $e->getMessage()
    ];
}

// Test 3: Tabel siswa_jilid
try {
    $sql = "SELECT COUNT(*) as count FROM siswa_jilid";
    $result = $conn->query($sql);
    $row = $result->fetch_assoc();
    $results['tests']['tabel_siswa_jilid'] = [
        'status' => 'OK',
        'count' => (int)$row['count']
    ];
} catch (Exception $e) {
    $results['tests']['tabel_siswa_jilid'] = [
        'status' => 'FAILED',
        'error' => $e->getMessage()
    ];
}

// Test 4: Tabel jilid
try {
    $sql = "SELECT * FROM jilid ORDER BY urutan";
    $result = $conn->query($sql);
    $jilids = [];
    while ($row = $result->fetch_assoc()) {
        $jilids[] = [
            'id_jilid' => $row['id_jilid'],
            'nama_jilid' => $row['nama_jilid'],
            'urutan' => $row['urutan']
        ];
    }
    $results['tests']['tabel_jilid'] = [
        'status' => 'OK',
        'count' => count($jilids),
        'data' => $jilids
    ];
} catch (Exception $e) {
    $results['tests']['tabel_jilid'] = [
        'status' => 'FAILED',
        'error' => $e->getMessage()
    ];
}

// Test 5: Test JOIN query (yang digunakan di get_siswa_jilid.php)
try {
    // Ambil sample no_induk dari santri
    $sample_sql = "SELECT no_induk FROM santri LIMIT 1";
    $sample_result = $conn->query($sample_sql);
    
    if ($sample_result && $sample_result->num_rows > 0) {
        $sample_row = $sample_result->fetch_assoc();
        $test_no_induk = $sample_row['no_induk'];
        
        // Test JOIN query
        $join_sql = "SELECT sj.no_induk, sj.id_jilid_sekarang, sj.tanggal_mulai, 
                            s.nama as nama_siswa
                     FROM siswa_jilid sj
                     LEFT JOIN santri s ON sj.no_induk = s.no_induk
                     WHERE sj.no_induk = ?";
        
        $stmt = $conn->prepare($join_sql);
        $stmt->bind_param("s", $test_no_induk);
        $stmt->execute();
        $join_result = $stmt->get_result();
        
        if ($join_result->num_rows > 0) {
            $results['tests']['join_query'] = [
                'status' => 'OK',
                'test_no_induk' => $test_no_induk,
                'result' => 'Found in siswa_jilid'
            ];
        } else {
            $results['tests']['join_query'] = [
                'status' => 'OK',
                'test_no_induk' => $test_no_induk,
                'result' => 'Not in siswa_jilid (will be auto-created)'
            ];
        }
        $stmt->close();
    } else {
        $results['tests']['join_query'] = [
            'status' => 'SKIP',
            'message' => 'No santri data to test'
        ];
    }
} catch (Exception $e) {
    $results['tests']['join_query'] = [
        'status' => 'FAILED',
        'error' => $e->getMessage()
    ];
}

// Test 6: Test endpoint get_siswa_jilid.php
try {
    // Get first santri no_induk
    $sql = "SELECT no_induk FROM santri LIMIT 1";
    $result = $conn->query($sql);
    
    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $test_no_induk = $row['no_induk'];
        
        $results['tests']['endpoint_test'] = [
            'status' => 'READY',
            'message' => 'Test this URL in browser:',
            'url' => 'https://kampunginggrisori.com/api/get_siswa_jilid.php?no_induk=' . $test_no_induk,
            'test_no_induk' => $test_no_induk
        ];
    } else {
        $results['tests']['endpoint_test'] = [
            'status' => 'SKIP',
            'message' => 'No santri data'
        ];
    }
} catch (Exception $e) {
    $results['tests']['endpoint_test'] = [
        'status' => 'FAILED',
        'error' => $e->getMessage()
    ];
}

$conn->close();

// Summary
$all_ok = true;
foreach ($results['tests'] as $test) {
    if (isset($test['status']) && $test['status'] === 'FAILED') {
        $all_ok = false;
        break;
    }
}

$results['summary'] = [
    'all_tests_passed' => $all_ok,
    'message' => $all_ok ? 'All tests passed! API should work now.' : 'Some tests failed. Check errors above.'
];

echo json_encode($results, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
?>
