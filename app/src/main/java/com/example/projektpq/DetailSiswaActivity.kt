package com.example.projektpq

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Tambahkan data class SantriDetail di sini
data class SantriDetail(
    val no_induk: String = "-",
    val nama: String = "-",
    val tempat_tanggal_lahir: String = "-",
    val alamat: String = "-",
    val jilid: String = "-",
    val nik: String = "-",
    val no_kk: String = "-",
    val tahun_masuk: String = "-",
    val keterangan: String = "-"
)

class DetailSiswaActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var namaSiswa: TextView
    private lateinit var nomorInduk: TextView
    private lateinit var tempatTanggalLahir: TextView
    private lateinit var jilid: TextView
    private lateinit var btnHome: ImageButton
    private lateinit var btnSettings: ImageButton

    private var noInduk: String? = null

    private val API_URL = "https://kampunginggrisori.com/api/api_santri.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_siswa)

        noInduk = intent.getStringExtra("no_induk")
        Log.d("DetailSiswa", "No Induk received: $noInduk")

        initViews()
        setupListeners()

        if (noInduk != null && noInduk!!.isNotEmpty()) {
            loadDetailSantri(noInduk!!)
        } else {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        try {
            btnBack = findViewById(R.id.btn_back)
            namaSiswa = findViewById(R.id.nama_siswa)
            nomorInduk = findViewById(R.id.nomor_induk)
            tempatTanggalLahir = findViewById(R.id.tempat_tanggal_lahir)
            jilid = findViewById(R.id.jilid)
            btnHome = findViewById(R.id.btn_home)
            btnSettings = findViewById(R.id.btn_settings)

            Log.d("DetailSiswa", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("DetailSiswa", "Error initializing views: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            finish()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDetailSantri(noInduk: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "$API_URL?no_induk=$noInduk"
                Log.d("DetailSiswa", "Fetching URL: $urlString")

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                Log.d("DetailSiswa", "Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d("DetailSiswa", "Response: $response")

                    val jsonObject = JSONObject(response)

                    if (jsonObject.getBoolean("success")) {
                        val data = jsonObject.getJSONObject("data")

                        val santriDetail = SantriDetail(
                            no_induk = data.optString("no_induk", "-"),
                            nama = data.optString("nama", "-"),
                            tempat_tanggal_lahir = data.optString("Tempat_tanggal_lahir", "-"),
                            alamat = data.optString("Alamat", "-"),
                            jilid = data.optString("jilid", "-"),
                            nik = data.optString("NIK", "-"),
                            no_kk = data.optString("NO_KK", "-"),
                            tahun_masuk = data.optString("Tahun_masuk", "-"),
                            keterangan = data.optString("Keterangan", "-")
                        )

                        withContext(Dispatchers.Main) {
                            displaySantriDetail(santriDetail)
                        }
                    } else {
                        val message = jsonObject.optString("message", "Data tidak ditemukan")
                        Log.w("DetailSiswa", "API returned success=false: $message")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@DetailSiswaActivity,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                } else {
                    Log.e("DetailSiswa", "HTTP Error: $responseCode")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@DetailSiswaActivity,
                            "Gagal memuat data (HTTP $responseCode)",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DetailSiswa", "Error loading detail: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DetailSiswaActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun displaySantriDetail(santri: SantriDetail) {
        try {
            namaSiswa.text = santri.nama
            nomorInduk.text = santri.no_induk
            tempatTanggalLahir.text = santri.tempat_tanggal_lahir
            jilid.text = santri.jilid

            Log.d("DetailSiswa", "Data displayed successfully")
        } catch (e: Exception) {
            Log.e("DetailSiswa", "Error displaying data: ${e.message}", e)
            Toast.makeText(this, "Error menampilkan data", Toast.LENGTH_SHORT).show()
        }
    }
}