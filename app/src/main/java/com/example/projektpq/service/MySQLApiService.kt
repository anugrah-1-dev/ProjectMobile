package com.example.projektpq.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MySQLApiService {

    companion object {
        private const val TAG = "MySQLApiService"

        // ✅ URL yang benar
        private const val BASE_URL = "https://kampunginggrisori.com/api"

        private const val CONNECT_TIMEOUT = 15000 // 15 seconds
        private const val READ_TIMEOUT = 15000 // 15 seconds
    }

    data class LoginResponse(
        val success: Boolean,
        val message: String,
        val data: UserData? = null
    )

    data class UserData(
        val id_user: Int,
        val username: String,
        val role: String
    )

    data class ChangePasswordResponse(
        val success: Boolean,
        val message: String
    )

    // ==================== LOGIN ====================

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/login.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                    useCaches = false
                }

                // Kirim data
                val jsonInput = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }

                Log.d(TAG, "Login URL: $url")
                Log.d(TAG, "Request: $jsonInput")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonInput.toString())
                    writer.flush()
                }

                // Baca response
                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }

                Log.d(TAG, "Response: $response")

                // Handle empty response
                if (response.isBlank()) {
                    return@withContext Result.failure(Exception("Empty response from server"))
                }

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val userData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        role = dataObj.getString("role")
                    )
                } else null

                Result.success(LoginResponse(success, message, userData))

            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== REGISTER ====================

    suspend fun register(username: String, password: String, role: String = "admin biasa"): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/register.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                    useCaches = false
                }

                val jsonInput = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                    put("role", role)
                }

                Log.d(TAG, "Register URL: $url")
                Log.d(TAG, "Request: $jsonInput")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonInput.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }

                Log.d(TAG, "Response: $response")

                // Handle empty response
                if (response.isBlank()) {
                    return@withContext Result.failure(Exception("Empty response from server"))
                }

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val userData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        role = dataObj.getString("role")
                    )
                } else null

                Result.success(LoginResponse(success, message, userData))

            } catch (e: Exception) {
                Log.e(TAG, "Register error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== CHANGE PASSWORD ====================

    suspend fun changePassword(
        username: String,
        currentPassword: String,
        newPassword: String
    ): Result<ChangePasswordResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/change_password.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                    useCaches = false
                }

                // Kirim data
                val jsonInput = JSONObject().apply {
                    put("username", username)
                    put("current_password", currentPassword)
                    put("new_password", newPassword)
                }

                Log.d(TAG, "Change Password URL: $url")
                Log.d(TAG, "Request: username=$username (password hidden for security)")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonInput.toString())
                    writer.flush()
                }

                // Baca response
                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }

                Log.d(TAG, "Response: $response")

                // Handle empty response
                if (response.isBlank()) {
                    return@withContext Result.failure(Exception("Empty response from server"))
                }

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Log.d(TAG, if (success) "✓ Password changed successfully" else "✗ Password change failed: $message")

                Result.success(ChangePasswordResponse(success, message))

            } catch (e: Exception) {
                Log.e(TAG, "Change password error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Test koneksi ke server
     */
    suspend fun testConnection(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/test.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Connection test - Response Code: $responseCode")

                Result.success(responseCode == HttpURLConnection.HTTP_OK)

            } catch (e: Exception) {
                Log.e(TAG, "Connection test error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * Get server status
     */
    suspend fun getServerStatus(): Result<String> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/status.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                }

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use {
                        it.readText()
                    }
                    Result.success(response)
                } else {
                    Result.failure(Exception("Server returned code: $responseCode"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Get server status error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }
}