package com.example.projektpq.service

import android.util.Log
import com.example.projektpq.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class SoalApiService {

    companion object {
        private const val TAG = "SoalApiService"
        private const val BASE_URL = "https://kampunginggrisori.com/api"
        private const val CONNECT_TIMEOUT = 15000
        private const val READ_TIMEOUT = 15000
    }

    // ==================== GET ALL JILID ====================
    suspend fun getAllJilid(): Result<JilidResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/get_jilid.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doInput = true
                }

                Log.d(TAG, "Get Jilid URL: $url")

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val jilidList = if (success && jsonResponse.has("data")) {
                    val dataArray = jsonResponse.getJSONArray("data")
                    List(dataArray.length()) { i ->
                        val obj = dataArray.getJSONObject(i)
                        Jilid(
                            id_jilid = obj.getInt("id_jilid"),
                            nama_jilid = obj.getString("nama_jilid"),
                            deskripsi = obj.optString("deskripsi", "")
                        )
                    }
                } else emptyList()

                Result.success(JilidResponse(success, message, jilidList))

            } catch (e: Exception) {
                Log.e(TAG, "Get jilid error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== GET SOAL BY JILID ====================
    suspend fun getSoalByJilid(idJilid: Int): Result<SoalResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/get_soal.php?id_jilid=$idJilid")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doInput = true
                }

                Log.d(TAG, "Get Soal URL: $url")

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val soalList = if (success && jsonResponse.has("data")) {
                    val dataArray = jsonResponse.getJSONArray("data")
                    List(dataArray.length()) { i ->
                        val obj = dataArray.getJSONObject(i)
                        Soal(
                            id_soal = obj.getInt("id_soal"),
                            id_jilid = obj.getInt("id_jilid"),
                            nomor_soal = obj.getInt("nomor_soal"),
                            isi_soal = obj.getString("isi_soal"),
                            tipe_soal = obj.optString("tipe_soal", "praktek"),
                            bobot_nilai = obj.optInt("bobot_nilai", 25)
                        )
                    }
                } else emptyList()

                Result.success(SoalResponse(success, message, soalList))

            } catch (e: Exception) {
                Log.e(TAG, "Get soal error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== CREATE UJIAN (DENGAN NO_INDUK) ====================
    suspend fun createUjian(noInduk: String, idJilid: Int): Result<UjianResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/create_ujian.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val jsonInput = JSONObject().apply {
                    put("no_induk", noInduk)  // Pakai no_induk
                    put("id_jilid", idJilid)
                    put("status", "berlangsung")
                }

                Log.d(TAG, "Create Ujian Request: $jsonInput")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                    it.write(jsonInput.toString())
                    it.flush()
                }

                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val ujian = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    // SESUAIKAN dengan model Ujian yang baru (no_induk: String)
                    Ujian(
                        id_ujian = obj.getInt("id_ujian"),
                        no_induk = obj.optString("no_induk", noInduk), // Parameter ke-2: no_induk (String)
                        id_jilid = obj.getInt("id_jilid"),
                        tanggal_ujian = dateFormat.parse(obj.getString("tanggal_ujian")) ?: Date(),
                        nilai_total = obj.optDouble("nilai_total", 0.0),
                        status = obj.optString("status", "berlangsung")
                    )
                } else null

                Result.success(UjianResponse(success, message, ujian))

            } catch (e: Exception) {
                Log.e(TAG, "Create ujian error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== SUBMIT JAWABAN ====================
    suspend fun submitJawaban(
        idUjian: Int,
        idSoal: Int,
        nilai: Double,
        catatan: String? = null
    ): Result<JawabanResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/submit_jawaban.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                }

                val jsonInput = JSONObject().apply {
                    put("id_ujian", idUjian)
                    put("id_soal", idSoal)
                    put("nilai", nilai)
                    if (catatan != null) put("catatan", catatan)
                }

                Log.d(TAG, "Submit Jawaban Request: $jsonInput")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                    it.write(jsonInput.toString())
                    it.flush()
                }

                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Result.success(JawabanResponse(success, message))

            } catch (e: Exception) {
                Log.e(TAG, "Submit jawaban error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== FINISH UJIAN ====================
    suspend fun finishUjian(idUjian: Int): Result<UjianResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/finish_ujian.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                }

                val jsonInput = JSONObject().apply {
                    put("id_ujian", idUjian)
                }

                Log.d(TAG, "Finish Ujian Request: $jsonInput")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                    it.write(jsonInput.toString())
                    it.flush()
                }

                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val ujian = if (success && jsonResponse.has("data")) {
                    val obj = jsonResponse.getJSONObject("data")
                    // SESUAIKAN dengan model Ujian yang baru (no_induk: String)
                    Ujian(
                        id_ujian = obj.getInt("id_ujian"),
                        no_induk = obj.optString("no_induk", ""), // Parameter ke-2: no_induk (String)
                        id_jilid = obj.getInt("id_jilid"),
                        tanggal_ujian = dateFormat.parse(obj.getString("tanggal_ujian")) ?: Date(),
                        nilai_total = obj.getDouble("nilai_total"),
                        status = obj.getString("status")
                    )
                } else null

                Result.success(UjianResponse(success, message, ujian))

            } catch (e: Exception) {
                Log.e(TAG, "Finish ujian error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ==================== ADD SOAL ====================
    suspend fun addSoal(
        idJilid: Int,
        nomorSoal: Int,
        isiSoal: String,
        tipeSoal: String = "praktek",
        bobotNilai: Int = 25
    ): Result<JawabanResponse> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/add_soal.php")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    doOutput = true
                    doInput = true
                }

                val jsonInput = JSONObject().apply {
                    put("id_jilid", idJilid)
                    put("nomor_soal", nomorSoal)
                    put("isi_soal", isiSoal)
                    put("tipe_soal", tipeSoal)
                    put("bobot_nilai", bobotNilai)
                }

                Log.d(TAG, "Add Soal Request: $jsonInput")

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                    it.write(jsonInput.toString())
                    it.flush()
                }

                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, Charsets.UTF_8)
                ).use { it.readText() }

                Log.d(TAG, "Response: $response")

                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                Result.success(JawabanResponse(success, message))

            } catch (e: Exception) {
                Log.e(TAG, "Add soal error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }
}