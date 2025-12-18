package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DetailSiswaActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var namaSiswa: TextView
    private lateinit var nomorInduk: TextView
    private lateinit var tempatTanggalLahir: TextView
    private lateinit var jilid: TextView

    companion object {
        private const val TAG = "DetailSiswaActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_siswa)

        Log.d(TAG, "DetailSiswaActivity onCreate")

        initializeViews()
        setupViews()
        setupBackPressHandler()
        displayStudentData()

        // Coba load data tambahan dari API jika no_induk tersedia
        val noInduk = intent.getStringExtra("no_induk")
        if (!noInduk.isNullOrEmpty()) {
            loadDetailFromApi(noInduk)
        }
    }

    private fun initializeViews() {
        Log.d(TAG, "Initializing views")

        btnBack = findViewById(R.id.btn_back)
        namaSiswa = findViewById(R.id.nama_siswa)
        nomorInduk = findViewById(R.id.nomor_induk)
        tempatTanggalLahir = findViewById(R.id.tempat_tanggal_lahir)
        jilid = findViewById(R.id.jilid)
    }

    private fun setupViews() {
        Log.d(TAG, "Setting up views")

        // Tombol back
        btnBack.setOnClickListener {
            Log.d(TAG, "Tombol back diklik")
            navigateBack()
        }
    }

    private fun setupBackPressHandler() {
        // Setup hardware back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBack()
            }
        })
    }

    private fun navigateBack() {
        Log.d(TAG, "Navigating back")
        finish()
    }

    private fun displayStudentData() {
        Log.d(TAG, "Displaying student data from intent")

        // Ambil data dari intent
        val noInduk = intent.getStringExtra("no_induk") ?: "-"
        val nama = intent.getStringExtra("nama") ?: "-"
        val tempatLahir = intent.getStringExtra("tempat_lahir") ?: "-"
        val tanggalLahir = intent.getStringExtra("tanggal_lahir") ?: "-"
        val alamat = intent.getStringExtra("alamat") ?: "-"
        val jilidData = intent.getStringExtra("jilid") ?: "-"

        Log.d(TAG, "Data dari intent:")
        Log.d(TAG, "  No Induk: $noInduk")
        Log.d(TAG, "  Nama: $nama")
        Log.d(TAG, "  Tempat Lahir: $tempatLahir")
        Log.d(TAG, "  Tanggal Lahir: $tanggalLahir")
        Log.d(TAG, "  Alamat: $alamat")
        Log.d(TAG, "  Jilid: $jilidData")

        // Format tempat tanggal lahir
        val tempatTanggalLahirText = if (tempatLahir != "-" && tanggalLahir != "-") {
            "$tempatLahir, $tanggalLahir"
        } else if (tempatLahir != "-") {
            tempatLahir
        } else if (tanggalLahir != "-") {
            tanggalLahir
        } else {
            "Belum diisi"
        }

        // Tampilkan data
        try {
            namaSiswa.text = nama
            nomorInduk.text = noInduk
            tempatTanggalLahir.text = tempatTanggalLahirText

            val jilidText = if (jilidData != "-" && jilidData.isNotEmpty()) {
                "Jilid $jilidData"
            } else {
                "Belum ditentukan"
            }
            jilid.text = jilidText

            Log.d(TAG, "Data berhasil ditampilkan di UI")
        } catch (e: Exception) {
            Log.e(TAG, "Error menampilkan data: ${e.message}")
            Toast.makeText(this, "Error menampilkan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDetailFromApi(noInduk: String) {
        Log.d(TAG, "Memuat detail dari API untuk no_induk: $noInduk")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiUrl = "https://kampunginggrisori.com/api/api_santri.php?no_induk=$noInduk"
                Log.d(TAG, "Mengakses API: $apiUrl")

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "TPQ-App-Android")

                val responseCode = connection.responseCode
                Log.d(TAG, "API Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "API Response: ${response.substring(0, kotlin.math.min(200, response.length))}...")

                    val jsonObject = JSONObject(response)

                    if (jsonObject.getBoolean("success")) {
                        val data = jsonObject.getJSONObject("data")

                        withContext(Dispatchers.Main) {
                            try {
                                // Update UI dengan data tambahan dari API
                                val nik = data.optString("nik", "")
                                val noKk = data.optString("no_kk", "")
                                val tahunMasuk = data.optString("tahun_masuk", "")
                                val keterangan = data.optString("keterangan", "")

                                // Jika ada TextView tambahan di layout, bisa diupdate di sini
                                // Contoh: findViewById<TextView>(R.id.tv_nik).text = nik

                                Log.d(TAG, "Data tambahan dari API:")
                                Log.d(TAG, "  NIK: $nik")
                                Log.d(TAG, "  No KK: $noKk")
                                Log.d(TAG, "  Tahun Masuk: $tahunMasuk")
                                Log.d(TAG, "  Keterangan: $keterangan")

                            } catch (e: Exception) {
                                Log.e(TAG, "Error update UI dari API: ${e.message}")
                            }
                        }
                    } else {
                        val errorMessage = jsonObject.optString("message", "Gagal memuat detail")
                        Log.w(TAG, "API error: $errorMessage")
                    }
                } else {
                    Log.w(TAG, "HTTP Error: $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error load detail dari API: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DetailSiswaActivity onDestroy")
    }
}