<?php
/**
 * Database Configuration
 * Konfigurasi untuk TPQ Application
 */

// Enable error logging (jangan tampilkan di output)
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

// Database credentials
$db_host = "localhost";
$db_user = "u221955505_dbtpq";
$db_pass = "@Dbtpq1.";
$db_name = "u221955505_dbtpq";

// Create MySQLi connection
$conn = @new mysqli($db_host, $db_user, $db_pass, $db_name);

// Check connection
if ($conn->connect_error) {
    error_log("Database connection failed: " . $conn->connect_error);
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    die(json_encode([
        'success' => false,
        'message' => 'Database connection failed',
        'error' => $conn->connect_error
    ], JSON_UNESCAPED_UNICODE));
}

// Set charset to utf8mb4
if (!$conn->set_charset("utf8mb4")) {
    error_log("Error setting charset: " . $conn->error);
}
?>
