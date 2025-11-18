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
}