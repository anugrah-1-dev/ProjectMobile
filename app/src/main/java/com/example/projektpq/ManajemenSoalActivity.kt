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
import android.content.Context.CONNECTIVITY_SERVICE
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import com.example.projektpq.models.UjianData
// Import untuk MPAndroidChart (Library untuk membuat grafik)
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import android.widget.ImageView
import android.widget.FrameLayout

/**
 * Activity untuk menampilkan grafik nilai dan tabel histori ujian
 * Menampilkan data berdasarkan Jilid yang dipilih
 */
class ManajemenSoalActivity : AppCompatActivity() {

    // Variable untuk menyimpan ID dan Nama Jilid dari halaman sebelumnya
    private var idJilid: Int = 0
    private lateinit var namaJilid: String

    // Map untuk menyimpan data nilai per bulan
    // Format: "Desember 2025" -> [24.0, 30.0, 28.5]
    private val monthlyData = mutableMapOf<String, MutableList<Double>>()

    // Coroutine scope untuk operasi async (background task)
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Container untuk chart/grafik
    private lateinit var chartContainer: FrameLayout

    companion object {
        private const val TAG = "ManajemenSoalActivity" // Untuk logging di Logcat
        private const val BASE_URL = "https://kampunginggrisori.com/api" // URL API
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.soal) // Set layout XML

        // Ambil data ID_JILID dan NAMA_JILID dari Intent
        idJilid = intent.getIntExtra("ID_JILID", 0)
        namaJilid = intent.getStringExtra("NAMA_JILID") ?: "JILID 1"

        // Validasi: Jika ID Jilid tidak valid, tutup activity
        if (idJilid == 0) {
            Toast.makeText(this, "Error: ID Jilid tidak valid", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "ID Jilid tidak ditemukan dari intent")
            finish()
            return
        }

        // Log untuk debugging
        Log.d(TAG, "============================================")
        Log.d(TAG, "MEMULAI ManajemenSoalActivity")
        Log.d(TAG, "ID Jilid: $idJilid, Nama Jilid: $namaJilid")
        Log.d(TAG, "============================================")

        // Set judul JILID di tampilan (misalnya "JILID V")
        try {
            val jilidContainer = findViewById<LinearLayout>(R.id.jilid_container)
            val jilidText = jilidContainer.getChildAt(0) as TextView
            jilidText.text = namaJilid
            Log.d(TAG, "Judul jilid berhasil diset")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat set judul jilid: ${e.message}", e)
        }

        // Inisialisasi container untuk chart
        chartContainer = findViewById(R.id.chart_container)

        // Cek koneksi internet
        testNetworkConnectivity()

        // Setup chart (grafik garis)
        setupDynamicChart()

        // Load data dari API
        loadHistoryData()

        // Setup semua tombol (Tambah, Lihat, Edit, Mulai, dll)
        setupButtons()
    }

    /**
     * Fungsi untuk mengecek koneksi internet
     * Menampilkan toast jika tidak ada koneksi
     */
    private fun testNetworkConnectivity() {
        scope.launch {
            try {
                Log.d(TAG, "🔍 Mengecek koneksi internet...")

                val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

                // Gunakan API modern untuk Android M (API 23) ke atas
                val isConnected = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    // Cek apakah ada koneksi WiFi, Cellular, atau Ethernet
                    capabilities != null && (
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                            )
                } else {
                    // Fallback untuk Android versi lama
                    @Suppress("DEPRECATION")
                    val activeNetwork = connectivityManager.activeNetworkInfo
                    @Suppress("DEPRECATION")
                    activeNetwork?.isConnectedOrConnecting == true
                }

                Log.d(TAG, "📶 Status koneksi: $isConnected")

                // Jika tidak ada koneksi, tampilkan peringatan
                if (!isConnected) {
                    Log.e(TAG, "❌ Tidak ada koneksi internet!")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ManajemenSoalActivity,
                            "Tidak ada koneksi internet",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saat cek koneksi: ${e.message}", e)
            }
        }
    }

    /**
     * Fungsi untuk membuat LineChart (grafik garis) secara dinamis
     * Mengganti placeholder ImageView dengan chart yang bisa menampilkan data
     */
    private fun setupDynamicChart() {
        try {
            Log.d(TAG, "🎨 Membuat grafik dinamis...")

            // Hapus placeholder image jika ada
            val chartImage = chartContainer.findViewById<ImageView>(R.id.chart_image)
            if (chartImage != null) {
                chartContainer.removeView(chartImage)
                Log.d(TAG, "Placeholder image dihapus")
            }

            // Buat LineChart baru dengan konfigurasi
            val lineChart = LineChart(this).apply {
                id = R.id.chart_image // Gunakan ID yang sama dengan placeholder
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    // Tambahkan margin agar tidak terlalu mepet
                    setMargins(16, 16, 16, 16)
                }

                // Konfigurasi tampilan chart
                description.isEnabled = false // Sembunyikan deskripsi
                setTouchEnabled(true) // Bisa disentuh untuk interaksi
                isDragEnabled = true // Bisa di-drag
                setScaleEnabled(true) // Bisa di-zoom
                setPinchZoom(false) // Disable pinch zoom
                setDrawGridBackground(false) // Tanpa background grid
                legend.isEnabled = false // Sembunyikan legend

                // Konfigurasi touch
                isDoubleTapToZoomEnabled = false // Disable double tap zoom
                setNoDataText("Belum ada data untuk ditampilkan") // Pesan jika tidak ada data
                setNoDataTextColor(Color.WHITE)

                // Background transparan agar terlihat background hijau dari layout
                setBackgroundColor(Color.TRANSPARENT)

                // Tambah spacing ekstra
                setExtraOffsets(10f, 10f, 10f, 20f)

                // Konfigurasi Sumbu X (horizontal, untuk bulan)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM // Label di bawah
                    setDrawGridLines(true) // Tampilkan garis grid
                    gridColor = Color.parseColor("#80FFFFFF") // Putih semi-transparan
                    textColor = Color.WHITE // Warna teks putih
                    textSize = 10f
                    granularity = 1f // Jarak antar titik minimal 1
                    setDrawAxisLine(false) // Sembunyikan garis axis
                    setAvoidFirstLastClipping(true) // Hindari label terpotong
                    yOffset = 10f // Offset ke bawah
                }

                // Konfigurasi Sumbu Y Kiri (vertical, untuk nilai 0-100)
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.parseColor("#80FFFFFF")
                    textColor = Color.WHITE
                    textSize = 10f
                    axisMinimum = 0f // Nilai minimal 0
                    axisMaximum = 100f // Nilai maksimal 100
                    setDrawAxisLine(false)
                    setLabelCount(5, true) // 5 label: 0, 25, 50, 75, 100
                    setDrawZeroLine(true) // Tampilkan garis di angka 0
                    zeroLineColor = Color.parseColor("#80FFFFFF")
                    xOffset = 10f
                }

                // Sembunyikan Sumbu Y Kanan
                axisRight.isEnabled = false
            }

            // Tambahkan chart ke container
            chartContainer.addView(lineChart, 0)

            Log.d(TAG, "✅ Grafik berhasil dibuat")
            Log.d(TAG, "   Ukuran container: ${chartContainer.width}x${chartContainer.height}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saat buat grafik: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Fungsi untuk setup semua tombol di halaman
     */
    private fun setupButtons() {
        val btnTambah = findViewById<Button>(R.id.btn_tambah)
        val btnLihat = findViewById<Button>(R.id.btn_lihat)
        val btnEdit = findViewById<Button>(R.id.btn_edit)
        val btnMulai = findViewById<Button>(R.id.btn_mulai)
        val btnHome = findViewById<LinearLayout>(R.id.btn_home)
        val btnSettings = findViewById<LinearLayout>(R.id.btn_settings)

        // Tombol Tambah - Ke halaman tambah soal
        btnTambah?.setOnClickListener {
            val intent = Intent(this, TambahSoalActivity::class.java)
            intent.putExtra("ID_JILID", idJilid)
            intent.putExtra("NAMA_JILID", namaJilid)
            startActivity(intent)
        }

        // Tombol Lihat - Ke halaman lihat soal
        btnLihat?.setOnClickListener {
            val intent = Intent(this, LihatSoalActivity::class.java)
            intent.putExtra("ID_JILID", idJilid)
            intent.putExtra("NAMA_JILID", namaJilid)
            startActivity(intent)
        }

        // Tombol Edit - Ke halaman edit soal
        btnEdit?.setOnClickListener {
            val intent = Intent(this, EditSoalActivity::class.java)
            intent.putExtra("ID_JILID", idJilid)
            intent.putExtra("NAMA_JILID", namaJilid)
            startActivity(intent)
        }

        // Tombol Mulai - Ke halaman mulai ujian
        btnMulai?.setOnClickListener {
            val intent = Intent(this, MulaiUjianActivity::class.java)
            intent.putExtra("ID_JILID", idJilid)
            intent.putExtra("NAMA_JILID", namaJilid)
            startActivity(intent)
        }

        // Tombol Home - Kembali ke halaman pilih jilid
        btnHome?.setOnClickListener {
            navigateBack()
        }

        // Tombol Settings - Ke halaman pengaturan
        btnSettings?.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Fungsi utama untuk load data histori ujian dari API
     * Dipanggil di onCreate dan onResume
     */
    private fun loadHistoryData() {
        scope.launch {
            try {
                Log.d(TAG, "============================================")
                Log.d(TAG, "MEMUAT DATA HISTORI")
                Log.d(TAG, "============================================")

                Toast.makeText(this@ManajemenSoalActivity, "Memuat data...", Toast.LENGTH_SHORT).show()

                // Panggil API di background thread (IO)
                val result = withContext(Dispatchers.IO) {
                    getUjianDataByJilidId(idJilid)
                }

                Log.d(TAG, "Hasil diterima - Berhasil: ${result.isSuccess}")

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    Log.d(TAG, "Jumlah data: ${data?.size ?: 0}")

                    if (data != null && data.isNotEmpty()) {
                        Log.d(TAG, "Memproses ${data.size} record ujian...")

                        // Proses data: kelompokkan per bulan
                        processUjianData(data)

                        // Pastikan data sudah berhasil diproses
                        if (monthlyData.isNotEmpty()) {
                            Log.d(TAG, "📊 Data bulanan tersedia: ${monthlyData.keys}")

                            // Update grafik dan tabel
                            updateChart()
                            updateTable()

                            Toast.makeText(
                                this@ManajemenSoalActivity,
                                "Data berhasil dimuat: ${data.size} ujian",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "✅ Data berhasil dimuat dan ditampilkan")
                        } else {
                            // Jika data kosong setelah diproses
                            Log.e(TAG, "❌ Data bulanan kosong setelah diproses!")
                            Toast.makeText(
                                this@ManajemenSoalActivity,
                                "Error: Data tidak dapat diproses",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        // Jika tidak ada data dari API
                        Toast.makeText(
                            this@ManajemenSoalActivity,
                            "Belum ada data ujian untuk $namaJilid",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.w(TAG, "⚠️ Tidak ada data untuk Jilid ID: $idJilid")

                        // Bersihkan grafik dan tabel
                        val lineChart = chartContainer.findViewById<LineChart>(R.id.chart_image)
                        lineChart?.clear()
                        lineChart?.invalidate()
                        clearTable()
                    }
                } else {
                    // Jika API gagal
                    val error = result.exceptionOrNull()
                    val errorMsg = error?.message ?: "Error tidak diketahui"

                    Log.e(TAG, "❌ Gagal memuat data: $errorMsg")
                    error?.printStackTrace()

                    Toast.makeText(
                        this@ManajemenSoalActivity,
                        "Gagal memuat data: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception di loadHistoryData: ${e.message}", e)
                e.printStackTrace()

                Toast.makeText(
                    this@ManajemenSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Fungsi untuk mengambil data ujian dari API berdasarkan ID Jilid
     * @param jilidId ID Jilid yang ingin diambil datanya
     * @return Result<List<UjianData>> - Success jika berhasil, Failure jika gagal
     */
    private suspend fun getUjianDataByJilidId(jilidId: Int): Result<List<UjianData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                // Buat URL dengan parameter id_jilid
                val urlString = "$BASE_URL/get_ujian_by_jilid.php?id_jilid=$jilidId"
                Log.d(TAG, "📡 Request ke: $urlString")

                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection

                // Konfigurasi koneksi HTTP
                connection.apply {
                    requestMethod = "GET" // Method GET
                    setRequestProperty("Accept", "application/json") // Terima JSON
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("User-Agent", "TPQ-Android-App") // User agent custom
                    connectTimeout = 30000  // Timeout koneksi 30 detik
                    readTimeout = 30000     // Timeout baca 30 detik
                    doInput = true // Izinkan input
                    useCaches = false // Jangan gunakan cache

                    // Log protocol yang digunakan (HTTP atau HTTPS)
                    if (url.protocol.equals("https", ignoreCase = true)) {
                        Log.d(TAG, "Menggunakan koneksi HTTPS")
                    } else {
                        Log.d(TAG, "Menggunakan koneksi HTTP")
                    }
                }

                // Mulai koneksi
                Log.d(TAG, "Menghubungkan...")
                connection.connect()
                Log.d(TAG, "Koneksi berhasil")

                // Cek response code
                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage
                Log.d(TAG, "📡 Response Code: $responseCode")
                Log.d(TAG, "📡 Response Message: $responseMessage")

                // Baca response dari server
                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Membaca dari input stream")
                    connection.inputStream
                } else {
                    Log.e(TAG, "❌ Response code buruk: $responseCode - $responseMessage")
                    connection.errorStream ?: connection.inputStream
                }

                // Konversi stream ke string
                val response = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    val text = reader.readText()
                    Log.d(TAG, "Selesai membaca response")
                    text
                }

                Log.d(TAG, "📡 Panjang Response: ${response.length}")
                Log.d(TAG, "📡 Response: $response")

                // Validasi: response tidak boleh kosong
                if (response.isBlank()) {
                    Log.e(TAG, "❌ Response kosong dari server")
                    return@withContext Result.failure(Exception("Server mengembalikan response kosong"))
                }

                // Parse JSON response
                val jsonResponse = try {
                    JSONObject(response)
                } catch (e: JSONException) {
                    Log.e(TAG, "❌ Error parsing JSON: ${e.message}")
                    Log.e(TAG, "Response yang gagal di-parse: $response")
                    e.printStackTrace()
                    return@withContext Result.failure(Exception("Format response tidak valid: ${e.message}"))
                }

                // Cek field "success"
                val success = jsonResponse.optBoolean("success", false)
                Log.d(TAG, "📊 Success flag: $success")

                if (success) {
                    // Jika success = true, ambil data
                    if (!jsonResponse.has("data")) {
                        Log.w(TAG, "⚠️ Response tidak punya field 'data'")
                        return@withContext Result.success(emptyList())
                    }

                    val dataArray = jsonResponse.getJSONArray("data")
                    Log.d(TAG, "📊 Panjang array data: ${dataArray.length()}")

                    if (dataArray.length() == 0) {
                        Log.w(TAG, "⚠️ Array data kosong")
                        return@withContext Result.success(emptyList())
                    }

                    val ujianList = mutableListOf<UjianData>()

                    // Loop setiap item di array
                    for (i in 0 until dataArray.length()) {
                        try {
                            val item = dataArray.getJSONObject(i)

                            // Debug: Print semua key yang ada di object
                            val keys = item.keys().asSequence().toList()
                            Log.d(TAG, "📋 Item $i keys: $keys")

                            // Cari field tanggal (bisa berbeda-beda nama fieldnya)
                            val tanggal = when {
                                item.has("Tanggal_ujian") && !item.isNull("Tanggal_ujian") -> {
                                    item.getString("Tanggal_ujian")
                                }
                                item.has("tanggal_ujian") && !item.isNull("tanggal_ujian") -> {
                                    item.getString("tanggal_ujian")
                                }
                                item.has("created_at") && !item.isNull("created_at") -> {
                                    item.getString("created_at")
                                }
                                else -> {
                                    // Jika tidak ada field tanggal, gunakan tanggal hari ini
                                    Log.e(TAG, "❌ Field tanggal tidak ditemukan di item $i")
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                }
                            }

                            // Ambil nilai ujian
                            val nilai = when {
                                item.has("nilai_total") && !item.isNull("nilai_total") -> {
                                    val nilaiStr = item.getString("nilai_total")
                                    try {
                                        nilaiStr.toDouble()
                                    } catch (e: Exception) {
                                        Log.e(TAG, "❌ Gagal parse nilai: $nilaiStr")
                                        0.0
                                    }
                                }
                                else -> {
                                    Log.w(TAG, "⚠️ nilai_total null atau tidak ada untuk item $i")
                                    0.0
                                }
                            }

                            // Pastikan format tanggal YYYY-MM-DD (10 karakter)
                            val tanggalFormatted = if (tanggal.length >= 10) {
                                tanggal.substring(0, 10)
                            } else {
                                tanggal
                            }

                            // Buat object UjianData
                            val ujianData = UjianData(
                                tanggalUjian = tanggalFormatted,
                                nilaiTotal = nilai
                            )

                            ujianList.add(ujianData)

                            Log.d(TAG, "✅ Item $i: tanggal=$tanggalFormatted, nilai=$nilai")

                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error parsing item $i: ${e.message}", e)
                            e.printStackTrace()
                        }
                    }

                    Log.d(TAG, "✅ Berhasil parse ${ujianList.size}/${dataArray.length()} record")

                    if (ujianList.isEmpty()) {
                        Log.w(TAG, "⚠️ Tidak ada data valid setelah parsing")
                    }

                    Result.success(ujianList)
                } else {
                    // Jika success = false
                    val message = jsonResponse.optString("message", "Tidak ada data")
                    Log.w(TAG, "⚠️ API return success=false: $message")
                    Result.success(emptyList())
                }

            } catch (e: javax.net.ssl.SSLException) {
                // Error SSL/Certificate
                val errorMsg = "SSL Error: ${e.message ?: "Validasi sertifikat gagal"}"
                Log.e(TAG, "❌ $errorMsg", e)
                e.printStackTrace()
                Result.failure(Exception(errorMsg))
            } catch (e: java.net.UnknownHostException) {
                // Error tidak bisa connect ke server
                val errorMsg = "Network Error: Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                Log.e(TAG, "❌ $errorMsg", e)
                e.printStackTrace()
                Result.failure(Exception(errorMsg))
            } catch (e: java.net.SocketTimeoutException) {
                // Error timeout
                val errorMsg = "Timeout: Server tidak merespons. Coba lagi."
                Log.e(TAG, "❌ $errorMsg", e)
                e.printStackTrace()
                Result.failure(Exception(errorMsg))
            } catch (e: java.io.IOException) {
                // Error IO (baca/tulis)
                val errorMsg = "IO Error: ${e.message ?: "Gagal membaca data dari server"}"
                Log.e(TAG, "❌ $errorMsg", e)
                e.printStackTrace()
                Result.failure(Exception(errorMsg))
            } catch (e: Exception) {
                // Error lainnya
                val errorMsg = "Error: ${e.javaClass.simpleName} - ${e.message ?: "Unknown error"}"
                Log.e(TAG, "❌ Exception di getUjianDataByJilidId: $errorMsg", e)
                e.printStackTrace()
                Result.failure(Exception(errorMsg))
            } finally {
                // Pastikan koneksi ditutup
                try {
                    connection?.disconnect()
                    Log.d(TAG, "🔌 Koneksi ditutup")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saat tutup koneksi: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Fungsi untuk memproses data ujian
     * Mengelompokkan data per bulan dan menghitung rata-rata
     * @param ujianList List data ujian dari API
     */
    private fun processUjianData(ujianList: List<UjianData>) {
        monthlyData.clear() // Bersihkan data lama

        // Format untuk parsing tanggal dari database (YYYY-MM-DD)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Format untuk nama bulan dalam bahasa Indonesia (Desember 2025)
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))

        Log.d(TAG, "📊 Memproses ${ujianList.size} record ujian...")

        // Loop setiap data ujian
        for ((index, ujian) in ujianList.withIndex()) {
            try {
                Log.d(TAG, "Memproses item $index: tanggal='${ujian.tanggalUjian}' nilai=${ujian.nilaiTotal}")

                // Parse tanggal dari string ke Date object
                val date = dateFormat.parse(ujian.tanggalUjian)
                if (date != null) {
                    // Convert Date ke nama bulan (contoh: "Desember 2025")
                    val monthName = monthFormat.format(date)

                    // Jika bulan ini belum ada di map, buat entry baru
                    if (!monthlyData.containsKey(monthName)) {
                        monthlyData[monthName] = mutableListOf()
                        Log.d(TAG, "📅 Buat entry bulan baru: $monthName")
                    }
                    // Tambahkan nilai ke bulan tersebut
                    monthlyData[monthName]?.add(ujian.nilaiTotal)

                    Log.d(TAG, "✅ Ditambahkan ke $monthName: ${ujian.nilaiTotal}")
                } else {
                    Log.e(TAG, "❌ Gagal parse tanggal: ${ujian.tanggalUjian}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error memproses item $index: ${e.message}", e)
                e.printStackTrace()
            }
        }

        // Log ringkasan data
        if (monthlyData.isEmpty()) {
            Log.e(TAG, "⚠️ PERINGATAN: Data bulanan kosong setelah diproses!")
            Log.e(TAG, "   Ini mungkin karena masalah parsing tanggal")
        } else {
            Log.d(TAG, "📊 Ringkasan data bulanan:")
            for ((month, values) in monthlyData) {
                val avg = values.average()
                Log.d(TAG, "   $month: ${values.size} ujian, rata-rata = $avg, nilai = $values")
            }
        }
    }

    /**
     * Fungsi untuk update grafik dengan data terbaru
     * Menampilkan 3 bulan terakhir di grafik garis
     */
    private fun updateChart() {
        try {
            Log.d(TAG, "📈 Update grafik...")

            // Cari LineChart yang sudah dibuat
            val lineChart = chartContainer.findViewById<LineChart>(R.id.chart_image)
            if (lineChart == null) {
                Log.e(TAG, "❌ LineChart tidak ditemukan!")
                return
            }

            // Jika tidak ada data bulanan, kosongkan grafik
            if (monthlyData.isEmpty()) {
                Log.d(TAG, "⚠️ Tidak ada data bulanan untuk ditampilkan")
                lineChart.clear()
                lineChart.invalidate()
                return
            }

            // Sorting bulan dari yang terbaru ke terlama
            val sortedMonths = monthlyData.keys.sortedByDescending { monthKey ->
                try {
                    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                    dateFormat.parse(monthKey)?.time ?: 0L
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing bulan untuk sorting: $monthKey", e)
                    0L
                }
            }

            // Ambil 3 bulan terakhir, lalu balik urutan (untuk grafik kiri ke kanan)
            val last3Months = sortedMonths.take(3).reversed()

            Log.d(TAG, "📈 3 Bulan terakhir: $last3Months")

            if (last3Months.isEmpty()) {
                lineChart.clear()
                lineChart.invalidate()
                return
            }

            // Siapkan data untuk grafik
            val entries = mutableListOf<Entry>() // Titik-titik di grafik
            val monthLabels = mutableListOf<String>() // Label bulan di sumbu X

            // Loop setiap bulan
            last3Months.forEachIndexed { index, month ->
                val values = monthlyData[month] ?: emptyList()
                if (values.isNotEmpty()) {
                    // Hitung rata-rata nilai di bulan ini
                    val average = values.average().toFloat()
                    // Tambahkan titik ke grafik (x = index, y = rata-rata)
                    entries.add(Entry(index.toFloat(), average))

                    // Ambil nama bulan saja (tanpa tahun)
                    // Contoh: "Desember 2025" -> "Desember"
                    val shortMonth = month.split(" ")[0]
                    monthLabels.add(shortMonth)

                    Log.d(TAG, "📈 Titik grafik $index: $shortMonth = $average")
                }
            }

            // Jika tidak ada entries, kosongkan grafik
            if (entries.isEmpty()) {
                Log.d(TAG, "⚠️ Tidak ada entries untuk grafik")
                lineChart.clear()
                lineChart.invalidate()
                return
            }

            // Buat dataset (garis di grafik) dengan styling
            val dataSet = LineDataSet(entries, "Rata-rata Nilai").apply {
                color = Color.WHITE // Warna garis putih
                setCircleColor(Color.WHITE) // Warna lingkaran putih
                lineWidth = 3f // Ketebalan garis
                circleRadius = 8f // Ukuran lingkaran
                circleHoleRadius = 4f // Ukuran lubang di lingkaran
                setDrawCircleHole(true) // Tampilkan lubang di lingkaran

                // PERBAIKAN: Gunakan circleHoleColor sebagai pengganti setCircleColorHole
                // Yang tidak tersedia di versi tertentu MPAndroidChart
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // Untuk Android O ke atas, gunakan setCircleHoleColor
                    try {
                        // Coba gunakan reflection untuk versi yang lebih baru
                        val method = this.javaClass.getMethod("setCircleHoleColor", Int::class.java)
                        method.invoke(this, Color.parseColor("#00DF82"))
                    } catch (e: NoSuchMethodException) {
                        // Fallback untuk versi yang lebih lama
                        try {
                            // Beberapa versi menggunakan holeColor
                            val method = this.javaClass.getMethod("holeColor", Int::class.java)
                            method.invoke(this, Color.parseColor("#00DF82"))
                        } catch (e2: Exception) {
                            // Jika semua gagal, gunakan cara alternatif tanpa hole color
                            Log.w(TAG, "setCircleHoleColor tidak tersedia di versi MPAndroidChart ini")
                            setDrawCircleHole(false) // Nonaktifkan hole
                        }
                    }
                } else {
                    // Untuk Android lama, nonaktifkan hole
                    setDrawCircleHole(false)
                }

                valueTextSize = 11f // Ukuran text nilai
                valueTextColor = Color.WHITE // Warna text nilai
                setDrawValues(true) // Tampilkan nilai di atas titik
                mode = LineDataSet.Mode.LINEAR // Mode garis lurus
                setDrawFilled(true) // Isi area di bawah garis
                fillColor = Color.parseColor("#80FFFFFF") // Warna isi putih semi-transparan
                fillAlpha = 128 // Transparansi isi
            }

            // Set data ke chart
            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Setup Sumbu X dengan label bulan
            lineChart.xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < monthLabels.size) {
                            monthLabels[index]
                        } else ""
                    }
                }

                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = Color.parseColor("#80FFFFFF")
                textColor = Color.WHITE
                textSize = 10f
                granularity = 1f
                setDrawAxisLine(false)

                // Handle jika hanya 1 data point
                if (monthLabels.size == 1) {
                    axisMinimum = -0.5f
                    axisMaximum = 0.5f
                    labelCount = 1
                } else {
                    axisMinimum = 0f
                    axisMaximum = (monthLabels.size - 1).toFloat()
                    labelCount = monthLabels.size
                }
            }

            // Setup Sumbu Y (0-100)
            lineChart.axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#80FFFFFF")
                textColor = Color.WHITE
                textSize = 10f
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawAxisLine(false)
                setLabelCount(5, true)
            }

            // Disable sumbu Y kanan
            lineChart.axisRight.isEnabled = false

            // Refresh grafik dengan animasi
            lineChart.setVisibleXRangeMaximum(3f) // Maksimal 3 titik terlihat
            lineChart.moveViewToX(0f) // Posisi awal di kiri
            lineChart.animateX(1000) // Animasi 1 detik
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()

            Log.d(TAG, "✅ Grafik berhasil diupdate dengan ${entries.size} titik")
            Log.d(TAG, "   Nilai: ${entries.map { "${it.x}=${it.y}" }}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error update grafik: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Fungsi untuk update tabel dengan data bulanan
     * Menampilkan semua bulan yang ada data
     */
    private fun updateTable() {
        try {
            Log.d(TAG, "📊 Update tabel...")

            val tableCard = findViewById<LinearLayout>(R.id.table_card)

            // Hapus semua row kecuali header (index 0)
            val childCount = tableCard.childCount
            if (childCount > 1) {
                tableCard.removeViews(1, childCount - 1)
            }

            // Jika tidak ada data, keluar dari fungsi
            if (monthlyData.isEmpty()) {
                Log.d(TAG, "⚠️ Tidak ada data untuk tabel")
                return
            }

            var rowNumber = 1

            // Sort bulan dari terbaru ke terlama
            val sortedMonthly = monthlyData.toSortedMap(compareByDescending {
                try {
                    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                    dateFormat.parse(it)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            })

            // Loop setiap bulan
            for ((month, values) in sortedMonthly) {
                // Hitung rata-rata nilai di bulan ini
                val average = values.average()

                // Buat row layout untuk tabel
                val rowLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(36) // Tinggi row 36dp
                    )
                    orientation = LinearLayout.HORIZONTAL
                }

                // Kolom NO (nomor urut)
                val tvNo = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.8f // Weight 0.8
                    )
                    text = rowNumber.toString()
                    textSize = 12f
                    setTextColor(Color.BLACK)
                    gravity = android.view.Gravity.CENTER
                    setBackgroundResource(R.drawable.button_border)
                }

                // Kolom BULAN (nama bulan)
                val tvBulan = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.5f // Weight 1.5
                    )
                    text = month
                    textSize = 12f
                    setTextColor(Color.BLACK)
                    gravity = android.view.Gravity.CENTER
                    setBackgroundResource(R.drawable.button_border)
                }

                // Kolom NILAI (rata-rata nilai)
                val tvNilai = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.7f // Weight 1.7
                    )
                    text = String.format("%.2f", average) // Format 2 desimal
                    textSize = 12f
                    setTextColor(Color.BLACK)
                    gravity = android.view.Gravity.CENTER
                    setBackgroundResource(R.drawable.button_border)
                }

                // Tambahkan semua kolom ke row
                rowLayout.addView(tvNo)
                rowLayout.addView(tvBulan)
                rowLayout.addView(tvNilai)

                // Tambahkan row ke table
                tableCard.addView(rowLayout)

                Log.d(TAG, "📊 Tambah row: $rowNumber. $month = ${String.format("%.2f", average)}")
                rowNumber++
            }

            Log.d(TAG, "✅ Tabel berhasil diupdate dengan ${monthlyData.size} baris")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error update tabel: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Fungsi untuk membersihkan tabel
     * Menghapus semua row kecuali header
     */
    private fun clearTable() {
        try {
            val tableCard = findViewById<LinearLayout>(R.id.table_card)
            val childCount = tableCard.childCount
            if (childCount > 1) {
                tableCard.removeViews(1, childCount - 1)
            }
            Log.d(TAG, "🗑️ Tabel dibersihkan")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat bersihkan tabel: ${e.message}", e)
        }
    }

    /**
     * Fungsi helper untuk convert DP ke Pixel
     * @param dp Nilai dalam DP
     * @return Nilai dalam Pixel
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Fungsi untuk kembali ke halaman pilih jilid
     */
    private fun navigateBack() {
        val intent = Intent(this, PilihJilidActivity::class.java)
        startActivity(intent)
        finish() // Tutup activity ini
    }

    /**
     * Dipanggil saat activity kembali ke foreground
     * Reload data untuk memastikan data terbaru
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 onResume - Reload data")
        loadHistoryData()
    }

    /**
     * Dipanggil saat activity di-destroy
     * Cancel semua coroutine untuk hindari memory leak
     */
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Cancel semua background task
        Log.d(TAG, "🛑 Activity di-destroy")
    }
}