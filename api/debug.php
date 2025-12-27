<?php
/**
 * Simple Debug Test
 * Check if basic PHP and database work
 */

// Show all errors
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "Step 1: PHP is working<br>";

// Test config
try {
    require_once 'config.php';
    echo "Step 2: Config loaded<br>";
    echo "Step 3: Database connected<br>";
    
    // Test simple query
    $result = $conn->query("SELECT 1");
    echo "Step 4: Simple query works<br>";
    
    // Test santri table
    $result = $conn->query("SELECT COUNT(*) as c FROM santri");
    $row = $result->fetch_assoc();
    echo "Step 5: Santri table accessible, count: " . $row['c'] . "<br>";
    
    echo "<br><strong>ALL TESTS PASSED!</strong><br>";
    echo "<br>Now test: <a href='get_siswa_jilid.php?no_induk=001'>get_siswa_jilid.php?no_induk=001</a>";
    
} catch (Exception $e) {
    echo "<br><strong>ERROR:</strong> " . $e->getMessage();
}
?>
