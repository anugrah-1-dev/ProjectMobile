package com.example.projektpq.service

import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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

        // Ktor client untuk API calls yang lebih modern
        private val apiClient = HttpClient {
            expectSuccess = false // Handle errors manually
        }
    }

    // ==================== DATA CLASSES ====================
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

    // ==================== SOAL DATA CLASSES ====================
    data class SoalData(
        val id_soal: Int,
        val nomor_soal: Int,
        val isi_soal: String,
        val tipe_soal: String,
        val bobot_nilai: Int,
        val jawaban: String?,
        val id_jilid: Int,
        val created_at: String? = null
    )

    data class SoalListResponse(
        val success: Boolean,
        val message: String,
        val data: List<SoalData>? = null
    )

    data class SoalResponse(
        val success: Boolean,
        val message: String,
        val data: SoalData? = null
    )

    // ==================== JILID DATA CLASSES ====================
    data class JilidData(
        val id_jilid: Int,
        val nama_jilid: String,
        val deskripsi: String?,
        val created_at: String?,
        val updated_at: String?
    )

    data class JilidListResponse(
        val success: Boolean,
        val message: String,
        val data: List<JilidData>? = null
    )

    // ==================== HASIL TES DATA CLASSES ====================
    data class HasilTesData(
        val id_hasil_tes: Int,
        val id_user: Int,
        val id_jilid: Int,
        val total_nilai: Int,
        val total_bobot: Int,
        val tanggal_tes: String,
        val created_at: String?,
        val nama_jilid: String?,
        val username: String?
    )

    data class HasilTesResponse(
        val success: Boolean,
        val message: String,
        val data: List<HasilTesData>? = null
    )

    data class SubmitJawabanData(
        val id_soal: Int,
        val jawaban_user: String,
        val is_correct: Boolean? = null
    )

    data class SubmitTesRequest(
        val id_user: Int,
        val id_jilid: Int,
        val jawaban: List<SubmitJawabanData>
    )

    data class SubmitTesResponse(
        val success: Boolean,
        val message: String,
        val data: Map<String, Any>? = null
    )

    // ==================== GENERIC API RESPONSE ====================
    data class ApiResponse<T>(
        val success: Boolean,
        val message: String,
        val data: T? = null
    )

    // ==================== SIMPLE API RESPONSE ====================
    data class SimpleApiResponse(
        val success: Boolean,
        val message: String
    )

    // ==================== KUALIFIKASI DATA CLASSES ====================
    data class KualifikasiResponse(
        val success: Boolean,
        val qualified: Boolean?, // true jika memenuhi syarat
        val message: String?,
        val previous_jilid: Int?,
        val previous_status: String?,
        val previous_nilai: Double?,
        val previous_persentase: Double?
    )

    // ==================== RIWAYAT UJIAN DATA CLASSES ====================
    data class RiwayatUjianResponse(
        val success: Boolean,
        val message: String?,
        val data: List<RiwayatUjianData>?
    )

    data class RiwayatUjianData(
        val id_ujian: Int,
        val id_jilid: Int,
        val nama_jilid: String?,
        val nilai_total: Double,
        val persentase: Double,
        val status: String, // "LULUS" atau "TIDAK LULUS"
        val tanggal_ujian: String
    )

    // ==================== PRIVATE HELPER METHODS ====================
    private fun parseJsonResponse(response: String): JSONObject {
        return JSONObject(response)
    }

    private fun handleErrorResponse(response: String): String {
        return try {
            val json = JSONObject(response)
            json.optString("message", "Unknown error occurred")
        } catch (e: Exception) {
            "Failed to parse error response"
        }
    }

    // ==================== KUALIFIKASI FUNCTIONS ====================

    // ✅ Cek apakah siswa memenuhi syarat untuk ujian jilid tertentu
    suspend fun checkKualifikasiJilid(noInduk: String, idJilid: Int): Result<KualifikasiResponse> {
        return try {
            val response = apiClient.get("$BASE_URL/check_kualifikasi.php") {
                parameter("no_induk", noInduk)
                parameter("id_jilid", idJilid)
            }.body<KualifikasiResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("MySQLApiService", "Error checking kualifikasi", e)
            Result.failure(e)
        }
    }

    // ✅ Simpan ujian dengan status kelulusan
    suspend fun saveUjianWithStatus(
        noInduk: String,
        idJilid: Int,
        nilaiTotal: Double,
        persentase: Double,
        status: String,
        tanggalUjian: String
    ): Result<ApiResponse<Any>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/save_ujian_with_status.php")
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
                    put("no_induk", noInduk)
                    put("id_jilid", idJilid)
                    put("nilai_total", nilaiTotal)
                    put("persentase", persentase)
                    put("status", status)
                    put("tanggal_ujian", tanggalUjian)
                }

                Log.d(TAG, "Save Ujian with Status URL: $url")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Result.success(ApiResponse(success, message, null))

            } catch (e: Exception) {
                Log.e(TAG, "Error saving ujian with status", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ✅ Get riwayat ujian siswa untuk tracking progress
    suspend fun getRiwayatUjianSiswa(noInduk: String): Result<RiwayatUjianResponse> {
        return try {
            val response = apiClient.get("$BASE_URL/get_riwayat_ujian.php") {
                parameter("no_induk", noInduk)
            }.body<RiwayatUjianResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("MySQLApiService", "Error getting riwayat ujian", e)
            Result.failure(e)
        }
    }

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
                Log.d(TAG, "Request: username=$username")

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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val userData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        role = dataObj.getString("role"),
                        email = dataObj.optString("email", null).takeIf { it != "null" && it.isNotEmpty() },
                        nomor_telepon = dataObj.optString("nomor_telepon", null).takeIf { it != "null" && it.isNotEmpty() }
                    )
                } else null

                Result.success(LoginResponse(success, message, userData))

            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
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
                Log.d(TAG, "Request: username=$username")

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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val userData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        role = dataObj.getString("role"),
                        email = dataObj.optString("email", null).takeIf { it != "null" && it.isNotEmpty() },
                        nomor_telepon = dataObj.optString("nomor_telepon", null).takeIf { it != "null" && it.isNotEmpty() }
                    )
                } else null

                Result.success(LoginResponse(success, message, userData))

            } catch (e: Exception) {
                Log.e(TAG, "Register error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val profileData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserProfileData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        email = dataObj.optString("email", null).takeIf { it != "null" && it.isNotEmpty() },
                        nomor_telepon = dataObj.optString("nomor_telepon", null).takeIf { it != "null" && it.isNotEmpty() },
                        role = dataObj.getString("role")
                    )
                } else null

                Result.success(UserProfileResponse(success, message, profileData))

            } catch (e: Exception) {
                Log.e(TAG, "Get profile error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
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
                    put("email", email ?: "")
                    put("nomor_telepon", nomorTelepon ?: "")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val profileData = if (success && jsonResponse.has("data")) {
                    val dataObj = jsonResponse.getJSONObject("data")
                    UserProfileData(
                        id_user = dataObj.getInt("id_user"),
                        username = dataObj.getString("username"),
                        email = dataObj.optString("email", null).takeIf { it != "null" && it.isNotEmpty() },
                        nomor_telepon = dataObj.optString("nomor_telepon", null).takeIf { it != "null" && it.isNotEmpty() },
                        role = dataObj.getString("role")
                    )
                } else null

                Result.success(UserProfileResponse(success, message, profileData))

            } catch (e: Exception) {
                Log.e(TAG, "Update profile error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Log.d(TAG, if (success) "✓ Password changed successfully" else "✗ Password change failed: $message")

                Result.success(ChangePasswordResponse(success, message))

            } catch (e: Exception) {
                Log.e(TAG, "Change password error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== GET ALL SOAL BY JILID ====================
    suspend fun getSoalByJilid(idJilid: Int): Result<ApiResponse<List<SoalData>>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/get_soal_by_jilid.php?id_jilid=$idJilid")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doInput = true
                    useCaches = false
                }

                Log.d(TAG, "Get Soal by Jilid URL: $url")

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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalList = if (success && jsonResponse.has("data")) {
                    val dataArray = jsonResponse.getJSONArray("data")
                    List(dataArray.length()) { i ->
                        val obj = dataArray.getJSONObject(i)
                        SoalData(
                            id_soal = obj.getInt("id_soal"),
                            nomor_soal = obj.getInt("nomor_soal"),
                            isi_soal = obj.getString("isi_soal"),
                            tipe_soal = obj.getString("tipe_soal"),
                            bobot_nilai = obj.getInt("bobot_nilai"),
                            jawaban = obj.optString("jawaban", null).takeIf { it != "null" && it.isNotEmpty() },
                            id_jilid = obj.getInt("id_jilid"),
                            created_at = obj.optString("created_at", null)
                        )
                    }
                } else emptyList()

                Result.success(ApiResponse(success, message, soalList))

            } catch (e: Exception) {
                Log.e(TAG, "Get soal by jilid error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== GET SOAL BY ID ====================
    suspend fun getSoalById(idSoal: Int): Result<ApiResponse<SoalData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/get_soal_by_id.php?id_soal=$idSoal")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doInput = true
                    useCaches = false
                }

                Log.d(TAG, "Get Soal by ID URL: $url")

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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalData = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    SoalData(
                        id_soal = obj.getInt("id_soal"),
                        nomor_soal = obj.getInt("nomor_soal"),
                        isi_soal = obj.getString("isi_soal"),
                        tipe_soal = obj.getString("tipe_soal"),
                        bobot_nilai = obj.getInt("bobot_nilai"),
                        jawaban = obj.optString("jawaban", null).takeIf { it != "null" && it.isNotEmpty() },
                        id_jilid = obj.getInt("id_jilid"),
                        created_at = obj.optString("created_at", null)
                    )
                } else null

                Result.success(ApiResponse(success, message, soalData))

            } catch (e: Exception) {
                Log.e(TAG, "Get soal by id error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== CREATE SOAL ====================
    suspend fun createSoal(
        nomorSoal: Int,
        isiSoal: String,
        tipeSoal: String,
        bobotNilai: Int,
        jawaban: String?,
        idJilid: Int
    ): Result<ApiResponse<SoalData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/create_soal.php")
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
                    put("nomor_soal", nomorSoal)
                    put("isi_soal", isiSoal)
                    put("tipe_soal", tipeSoal)
                    put("bobot_nilai", bobotNilai)
                    put("jawaban", jawaban ?: "")
                    put("id_jilid", idJilid)
                }

                Log.d(TAG, "Create Soal URL: $url")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalData = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    SoalData(
                        id_soal = obj.getInt("id_soal"),
                        nomor_soal = obj.getInt("nomor_soal"),
                        isi_soal = obj.getString("isi_soal"),
                        tipe_soal = obj.getString("tipe_soal"),
                        bobot_nilai = obj.getInt("bobot_nilai"),
                        jawaban = obj.optString("jawaban", null).takeIf { it != "null" && it.isNotEmpty() },
                        id_jilid = obj.getInt("id_jilid"),
                        created_at = obj.optString("created_at", null)
                    )
                } else null

                Result.success(ApiResponse(success, message, soalData))

            } catch (e: Exception) {
                Log.e(TAG, "Create soal error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== UPDATE SOAL (FULL) ====================
    suspend fun updateSoal(
        idSoal: Int,
        nomorSoal: Int,
        isiSoal: String,
        tipeSoal: String,
        bobotNilai: Int,
        jawaban: String?,
        idJilid: Int
    ): Result<ApiResponse<SoalData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/update_soal.php")
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
                    put("id_soal", idSoal)
                    put("nomor_soal", nomorSoal)
                    put("isi_soal", isiSoal)
                    put("tipe_soal", tipeSoal)
                    put("bobot_nilai", bobotNilai)
                    put("jawaban", jawaban ?: "")
                    put("id_jilid", idJilid)
                }

                Log.d(TAG, "Update Soal (Full) URL: $url")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalData = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    SoalData(
                        id_soal = obj.getInt("id_soal"),
                        nomor_soal = obj.getInt("nomor_soal"),
                        isi_soal = obj.getString("isi_soal"),
                        tipe_soal = obj.getString("tipe_soal"),
                        bobot_nilai = obj.getInt("bobot_nilai"),
                        jawaban = obj.optString("jawaban", null).takeIf { it != "null" && it.isNotEmpty() },
                        id_jilid = obj.getInt("id_jilid"),
                        created_at = obj.optString("created_at", null)
                    )
                } else null

                Result.success(ApiResponse(success, message, soalData))

            } catch (e: Exception) {
                Log.e(TAG, "Update soal (full) error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== UPDATE SOAL (SIMPLIFIED) ====================
    suspend fun updateSoal(
        idSoal: Int,
        isiSoal: String,
        bobotNilai: Int
    ): Result<ApiResponse<SoalData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/update_soal.php")
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
                    put("id_soal", idSoal)
                    put("isi_soal", isiSoal)
                    put("bobot_nilai", bobotNilai)
                }

                Log.d(TAG, "Update Soal (Simplified) URL: $url")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalData = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    SoalData(
                        id_soal = obj.getInt("id_soal"),
                        nomor_soal = obj.getInt("nomor_soal"),
                        isi_soal = obj.getString("isi_soal"),
                        tipe_soal = obj.getString("tipe_soal"),
                        bobot_nilai = obj.getInt("bobot_nilai"),
                        jawaban = obj.optString("jawaban", null).takeIf { it != "null" && it.isNotEmpty() },
                        id_jilid = obj.getInt("id_jilid"),
                        created_at = obj.optString("created_at", null)
                    )
                } else null

                Result.success(ApiResponse(success, message, soalData))

            } catch (e: Exception) {
                Log.e(TAG, "Update soal (simplified) error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== DELETE SOAL ====================
    suspend fun deleteSoal(idSoal: Int): Result<ApiResponse<Any>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/delete_soal.php")
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
                    put("id_soal", idSoal)
                }

                Log.d(TAG, "Delete Soal URL: $url")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Result.success(ApiResponse(success, message, null))

            } catch (e: Exception) {
                Log.e(TAG, "Delete soal error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== ADD SOAL ====================
    suspend fun addSoal(
        jilidId: Int,
        isiSoal: String,
        bobotNilai: Int
    ): Result<ApiResponse<SoalData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/addSoal.php")
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
                    put("jilid_id", jilidId)
                    put("isi_soal", isiSoal)
                    put("bobot_nilai", bobotNilai)
                }

                Log.d(TAG, "Add Soal URL: $url")
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

                val jsonResponse = parseJsonResponse(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalData = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    SoalData(
                        id_soal = obj.getInt("id_soal"),
                        nomor_soal = obj.getInt("nomor_soal"),
                        isi_soal = obj.getString("isi_soal"),
                        tipe_soal = obj.getString("tipe_soal"),
                        bobot_nilai = obj.getInt("bobot_nilai"),
                        jawaban = obj.optString("jawaban", null).takeIf { it != "null" && it.isNotEmpty() },
                        id_jilid = obj.getInt("id_jilid"),
                        created_at = obj.optString("created_at", null)
                    )
                } else null

                Result.success(ApiResponse(success, message, soalData))

            } catch (e: Exception) {
                Log.e(TAG, "Add soal error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== SAVE UJIAN (FIXED) ====================
    suspend fun saveUjian(
        noInduk: String,
        idJilid: Int,
        nilaiTotal: Double,
        tanggalUjian: String
    ): Result<SimpleApiResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/save_ujian.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                    useCaches = false
                }

                // PERBAIKAN: Nama field sesuai dengan yang di PHP
                val jsonData = JSONObject().apply {
                    put("no_induk", noInduk)
                    put("id_jilid", idJilid)
                    put("nilai_total", nilaiTotal)
                    put("tanggal_ujian", tanggalUjian)
                    put("status", "selesai")
                }

                val jsonString = jsonData.toString()
                Log.d(TAG, "===== SAVE UJIAN REQUEST =====")
                Log.d(TAG, "URL: $url")
                Log.d(TAG, "Request Body: $jsonString")

                // Write data
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val inputStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }

                Log.d(TAG, "Response Body: $response")

                if (response.isBlank()) {
                    Log.e(TAG, "Empty response from server")
                    return@withContext Result.failure(Exception("Empty response from server"))
                }

                val jsonResponse = try {
                    parseJsonResponse(response)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse JSON response: ${e.message}")
                    Log.e(TAG, "Raw response: $response")
                    return@withContext Result.failure(Exception("Invalid JSON response: ${e.message}"))
                }

                val success = jsonResponse.optBoolean("success", false)
                val message = jsonResponse.optString("message", "Unknown error")

                Log.d(TAG, "Success: $success, Message: $message")

                if (success) {
                    Result.success(SimpleApiResponse(success = true, message = message))
                } else {
                    Result.failure(Exception(message))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Save ujian error: ${e.message}", e)
                e.printStackTrace()
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }
}