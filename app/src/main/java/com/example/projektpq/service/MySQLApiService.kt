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
        private const val BASE_URL = "https://kampunginggrisori.com/api"
        private const val CONNECT_TIMEOUT = 15000
        private const val READ_TIMEOUT = 15000
    }

    data class LoginResponse(
        val success: Boolean,
        val message: String,
        val data: UserData? = null
    )

    data class UserData(
        val id_user: Int,
        val username: String,
        val role: String,
        val email: String? = null,
        val nomor_telepon: String? = null
    )

    data class ChangePasswordResponse(
        val success: Boolean,
        val message: String
    )

    data class UserProfileResponse(
        val success: Boolean,
        val message: String,
        val data: UserProfileData? = null
    )

    data class UserProfileData(
        val id_user: Int,
        val username: String,
        val email: String?,
        val nomor_telepon: String?,
        val role: String
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
                        role = dataObj.getString("role"),
                        email = if (dataObj.has("email")) dataObj.optString("email", null) else null,
                        nomor_telepon = if (dataObj.has("nomor_telepon")) dataObj.optString("nomor_telepon", null) else null
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
                        role = dataObj.getString("role"),
                        email = if (dataObj.has("email")) dataObj.optString("email", null) else null,
                        nomor_telepon = if (dataObj.has("nomor_telepon")) dataObj.optString("nomor_telepon", null) else null
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

    // ==================== GET USER PROFILE ====================
    suspend fun getUserProfile(idUser: Int): Result<UserProfileResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/get_user_profile.php?id_user=$idUser")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doInput = true
                    useCaches = false
                }

                Log.d(TAG, "Get Profile URL: $url")

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

                if (response.isBlank()) {
                    return@withContext Result.failure(Exception("Empty response from server"))
                }

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val profileData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserProfileData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        email = if (dataObj.has("email")) dataObj.optString("email", null) else null,
                        nomor_telepon = if (dataObj.has("nomor_telepon")) dataObj.optString("nomor_telepon", null) else null,
                        role = dataObj.getString("role")
                    )
                } else null

                Result.success(UserProfileResponse(success, message, profileData))

            } catch (e: Exception) {
                Log.e(TAG, "Get profile error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== UPDATE USER PROFILE ====================
    suspend fun updateUserProfile(
        idUser: Int,
        username: String,
        email: String?,
        nomorTelepon: String?
    ): Result<UserProfileResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/update_user_profile.php")
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
                    put("id_user", idUser)
                    put("username", username)
                    if (email != null) put("email", email)
                    if (nomorTelepon != null) put("nomor_telepon", nomorTelepon)
                }

                Log.d(TAG, "Update Profile URL: $url")
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

                if (response.isBlank()) {
                    return@withContext Result.failure(Exception("Empty response from server"))
                }

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val profileData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserProfileData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        email = if (dataObj.has("email")) dataObj.optString("email", null) else null,
                        nomor_telepon = if (dataObj.has("nomor_telepon")) dataObj.optString("nomor_telepon", null) else null,
                        role = dataObj.getString("role")
                    )
                } else null

                Result.success(UserProfileResponse(success, message, profileData))

            } catch (e: Exception) {
                Log.e(TAG, "Update profile error: ${e.message}", e)
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
}