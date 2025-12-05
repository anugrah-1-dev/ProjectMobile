package com.example.projektpq

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.graphics.Color
import android.util.Log
import com.example.projektpq.R
import com.example.projektpq.service.MySQLApiService
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import com.example.projektpq.models.UjianData
import com.example.projektpq.TambahSoalActivity
import com.example.projektpq.LihatSoalActivity
import com.example.projektpq.MulaiUjianActivity
import com.example.projektpq.EditSoalActivity
import com.example.projektpq.PengaturanActivity
import com.example.projektpq.PilihJilidActivity



class ManajemenSoalActivity : AppCompatActivity() {

    private lateinit var jilid: String
    private val apiService = MySQLApiService()
    private val monthlyData = mutableMapOf<String, MutableList<Double>>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val TAG = "ManajemenSoalActivity"
        private const val BASE_URL = "https://kampunginggrisori.com/api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.soal)

        // Ambil data dari intent
        jilid = intent.getStringExtra("JILID") ?: "JILID I"

        // Set judul JILID
        val jilidContainer = findViewById<LinearLayout>(R.id.jilid_container)
        val jilidText = jilidContainer.getChildAt(0) as TextView
        jilidText.text = jilid

        // Load data dan update UI
        loadHistoryData()

        // Inisialisasi tombol-tombol
        val btnTambah = findViewById<Button>(R.id.btn_tambah)
        val btnLihat = findViewById<Button>(R.id.btn_lihat)
        val btnEdit = findViewById<Button>(R.id.btn_edit)
        val btnMulai = findViewById<Button>(R.id.btn_mulai)
        val btnHome = findViewById<LinearLayout>(R.id.btn_home)
        val btnSettings = findViewById<LinearLayout>(R.id.btn_settings)

        // Klik pada tombol Tambah Soal
        btnTambah?.setOnClickListener {
            val intent = Intent(this, TambahSoalActivity::class.java)
            intent.putExtra("JILID", jilid)
            startActivity(intent)
        }

        // Klik pada tombol Lihat Soal
        btnLihat?.setOnClickListener {
            val intent = Intent(this, LihatSoalActivity::class.java)
            intent.putExtra("JILID", jilid)
            startActivity(intent)
        }

        // Klik pada tombol Edit Soal
        btnEdit?.setOnClickListener {
            val intent = Intent(this, EditSoalActivity::class.java)
            intent.putExtra("JILID", jilid)
            startActivity(intent)
        }

        // Klik pada tombol Mulai (untuk ujian)
        btnMulai?.setOnClickListener {
            val intent = Intent(this, MulaiUjianActivity::class.java)
            intent.putExtra("JILID", jilid)
            startActivity(intent)
        }

        // Klik pada tombol Home
        btnHome?.setOnClickListener {
            navigateBack()
        }

        // Klik pada tombol Settings
        btnSettings?.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadHistoryData() {
        scope.launch {
            try {
                // Tampilkan loading
                Toast.makeText(this@ManajemenSoalActivity, "Memuat data...", Toast.LENGTH_SHORT).show()

                val result = withContext(Dispatchers.IO) {
                    getUjianDataByJilid(jilid)
                }

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
                        processUjianData(data)
                        updateTable()
                    } else {
                        Toast.makeText(this@ManajemenSoalActivity, "Tidak ada data ujian", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ManajemenSoalActivity, "Gagal memuat data: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data: ${e.message}", e)
                Toast.makeText(this@ManajemenSoalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getUjianDataByJilid(jilidName: String): Result<List<UjianData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                // Endpoint untuk get ujian by jilid
                val url = URL("$BASE_URL/get_ujian_by_jilid.php?jilid=$jilidName")
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 15000
                    readTimeout = 15000
                    doInput = true
                    useCaches = false
                }

                Log.d(TAG, "Get Ujian URL: $url")

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

                if (success && jsonResponse.has("data")) {
                    val dataArray = jsonResponse.getJSONArray("data")
                    val ujianList = mutableListOf<UjianData>()

                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        ujianList.add(
                            UjianData(
                                tanggalUjian = item.getString("Tanggal_ujian"),
                                nilaiTotal = item.getDouble("nilai_total")
                            )
                        )
                    }

                    Result.success(ujianList)
                } else {
                    Result.failure(Exception("No data found"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Get ujian error: ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun processUjianData(ujianList: List<UjianData>) {
        monthlyData.clear()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM", Locale("id", "ID"))

        for (ujian in ujianList) {
            try {
                val date = dateFormat.parse(ujian.tanggalUjian)
                if (date != null) {
                    val monthName = monthFormat.format(date)

                    if (!monthlyData.containsKey(monthName)) {
                        monthlyData[monthName] = mutableListOf()
                    }
                    monthlyData[monthName]?.add(ujian.nilaiTotal)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date: ${ujian.tanggalUjian}", e)
            }
        }
    }

    private fun updateTable() {
        // Ambil LinearLayout table_card
        val tableCard = findViewById<LinearLayout>(R.id.table_card)

        // Hapus semua row data (kecuali header)
        val childCount = tableCard.childCount
        if (childCount > 1) {
            tableCard.removeViews(1, childCount - 1)
        }

        // Tambahkan data baru
        var rowNumber = 1
        for ((month, values) in monthlyData) {
            val average = values.average()

            val rowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(36)
                )
                orientation = LinearLayout.HORIZONTAL
            }

            // Column NO
            val tvNo = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f)
                text = rowNumber.toString()
                textSize = 12f
                setTextColor(Color.BLACK)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.button_border)
            }

            // Column BULAN
            val tvBulan = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.5f)
                text = month
                textSize = 12f
                setTextColor(Color.BLACK)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.button_border)
            }

            // Column Rata-rata Nilai
            val tvNilai = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.7f)
                text = String.format("%.2f", average)
                textSize = 12f
                setTextColor(Color.BLACK)
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.button_border)
            }

            rowLayout.addView(tvNo)
            rowLayout.addView(tvBulan)
            rowLayout.addView(tvNilai)

            tableCard.addView(rowLayout)
            rowNumber++
        }

        // Jika tidak ada data, tampilkan pesan
        if (monthlyData.isEmpty()) {
            Toast.makeText(this, "Belum ada data ujian untuk $jilid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun navigateBack() {
        val intent = Intent(this, PilihJilidActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Reload data setiap kali activity di-resume
        loadHistoryData()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    }