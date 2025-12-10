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
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import com.example.projektpq.models.UjianData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import android.widget.FrameLayout

class ManajemenSoalActivity : AppCompatActivity() {

    private var idJilid: Int = 0
    private lateinit var namaJilid: String
    private val monthlyData = mutableMapOf<String, MutableList<Double>>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var chartContainer: FrameLayout
    private var lineChart: LineChart? = null

    companion object {
        private const val TAG = "ManajemenSoalActivity"
        private const val BASE_URL = "https://kampunginggrisori.com/api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.soal)

        Log.d(TAG, "============================================")
        Log.d(TAG, "MEMULAI ManajemenSoalActivity")
        Log.d(TAG, "============================================")

        idJilid = intent.getIntExtra("ID_JILID", 0)
        namaJilid = intent.getStringExtra("NAMA_JILID") ?: "JILID 1"

        if (idJilid == 0) {
            Toast.makeText(this, "Error: ID Jilid tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "ID Jilid: $idJilid, Nama Jilid: $namaJilid")

        try {
            val jilidContainer = findViewById<LinearLayout>(R.id.jilid_container)
            val jilidText = jilidContainer.getChildAt(0) as TextView
            jilidText.text = namaJilid
        } catch (e: Exception) {
            Log.e(TAG, "Error set judul: ${e.message}")
        }

        chartContainer = findViewById(R.id.chart_container)

        setupDynamicChart()
        setupButtons()

        chartContainer.post {
            loadHistoryData()
        }
    }

    private fun setupDynamicChart() {
        try {
            Log.d(TAG, "🎨 Membuat grafik dengan sumbu hitam...")
            chartContainer.removeAllViews()

            lineChart = LineChart(this).apply {
                id = R.id.chart_image
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(32, 32, 32, 32)
                }

                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                legend.isEnabled = false
                isDoubleTapToZoomEnabled = false
                setBackgroundColor(Color.TRANSPARENT)
                setExtraOffsets(20f, 20f, 20f, 40f)

                setNoDataText("Memuat data...")
                setNoDataTextColor(Color.BLACK)

                // SUMBU X - HITAM
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridColor = "#40000000".toColorInt()
                    gridLineWidth = 1f
                    textColor = Color.BLACK
                    textSize = 14f
                    granularity = 1f
                    setDrawAxisLine(true)
                    axisLineColor = Color.BLACK
                    axisLineWidth = 2f
                    setAvoidFirstLastClipping(true)
                    yOffset = 20f
                    labelRotationAngle = 0f
                    setCenterAxisLabels(false)
                }

                // SUMBU Y - HITAM
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = "#40000000".toColorInt()
                    gridLineWidth = 1f
                    textColor = Color.BLACK
                    textSize = 12f
                    axisMinimum = 0f
                    axisMaximum = 100f
                    setDrawAxisLine(true)
                    axisLineColor = Color.BLACK
                    axisLineWidth = 2f
                    setLabelCount(6, true)
                    setDrawZeroLine(true)
                    zeroLineColor = Color.BLACK
                    zeroLineWidth = 2f
                    xOffset = 15f
                }

                axisRight.isEnabled = false
            }

            chartContainer.addView(lineChart)
            lineChart?.invalidate()

            Log.d(TAG, "✅ Grafik dengan sumbu hitam berhasil dibuat")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setup grafik: ${e.message}", e)
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_tambah)?.setOnClickListener {
            startActivity(Intent(this, TambahSoalActivity::class.java).apply {
                putExtra("ID_JILID", idJilid)
                putExtra("NAMA_JILID", namaJilid)
            })
        }

        findViewById<Button>(R.id.btn_lihat)?.setOnClickListener {
            startActivity(Intent(this, LihatSoalActivity::class.java).apply {
                putExtra("ID_JILID", idJilid)
                putExtra("NAMA_JILID", namaJilid)
            })
        }

        findViewById<Button>(R.id.btn_edit)?.setOnClickListener {
            startActivity(Intent(this, EditSoalActivity::class.java).apply {
                putExtra("ID_JILID", idJilid)
                putExtra("NAMA_JILID", namaJilid)
            })
        }

        findViewById<Button>(R.id.btn_mulai)?.setOnClickListener {
            startActivity(Intent(this, MulaiUjianActivity::class.java).apply {
                putExtra("ID_JILID", idJilid)
                putExtra("NAMA_JILID", namaJilid)
            })
        }

        findViewById<LinearLayout>(R.id.btn_home)?.setOnClickListener {
            startActivity(Intent(this, PilihJilidActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.btn_settings)?.setOnClickListener {
            startActivity(Intent(this, PengaturanActivity::class.java))
        }
    }

    private fun loadHistoryData() {
        scope.launch {
            try {
                Log.d(TAG, "📊 Memuat data histori untuk ID Jilid: $idJilid")

                val result = withContext(Dispatchers.IO) {
                    getUjianDataByJilidId(idJilid)
                }

                Log.d(TAG, "📊 Result success: ${result.isSuccess}")

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    Log.d(TAG, "📊 Data retrieved: ${data?.size ?: 0} items")

                    if (data != null && data.isNotEmpty()) {
                        Log.d(TAG, "✅ Data diterima: ${data.size} ujian")

                        processUjianData(data)

                        if (monthlyData.isNotEmpty()) {
                            updateChart()
                            updateTable()
                            Toast.makeText(this@ManajemenSoalActivity,
                                "Data dimuat: ${data.size} ujian", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "❌ Data bulanan kosong setelah diproses!")
                            showEmptyChart()
                        }
                    } else {
                        Log.w(TAG, "⚠️ Tidak ada data ujian")
                        showEmptyChart()
                        Toast.makeText(this@ManajemenSoalActivity,
                            "Belum ada data ujian untuk jilid ini", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "❌ Error mengambil data: ${error?.message}")
                    Log.e(TAG, "❌ Error type: ${error?.javaClass?.simpleName}")
                    error?.printStackTrace()

                    showEmptyChart()

                    val errorMessage = when {
                        error?.message?.contains("timeout", ignoreCase = true) == true ->
                            "Timeout: Koneksi terlalu lama"
                        error?.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                            "Tidak ada koneksi internet"
                        error?.message?.contains("HTTP", ignoreCase = true) == true ->
                            "Server error: ${error.message}"
                        else -> "Gagal mengambil data: ${error?.message ?: "Unknown error"}"
                    }

                    Toast.makeText(this@ManajemenSoalActivity,
                        errorMessage, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception di loadHistoryData: ${e.message}", e)
                e.printStackTrace()
                showEmptyChart()
                Toast.makeText(this@ManajemenSoalActivity,
                    "Error: ${e.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEmptyChart() {
        lineChart?.apply {
            clear()
            setNoDataText("Belum ada data ujian")
            setNoDataTextColor(Color.BLACK)
            invalidate()
        }
        clearTable()
    }

    private suspend fun getUjianDataByJilidId(jilidId: Int): Result<List<UjianData>> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$BASE_URL/get_ujian_by_jilid.php?id_jilid=$jilidId"
                Log.d(TAG, "📡 Request URL: $urlString")

                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    doInput = true
                    useCaches = false
                }

                connection.connect()
                val responseCode = connection.responseCode
                Log.d(TAG, "📡 Response Code: $responseCode")

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = connection.errorStream
                    val errorText = errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "No error details available"
                    Log.e(TAG, "❌ HTTP Error $responseCode: $errorText")
                    return@withContext Result.failure(Exception("HTTP $responseCode: $errorText"))
                }

                val inputStream = connection.inputStream
                    ?: return@withContext Result.failure(Exception("Server tidak mengembalikan data"))

                val response = inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "📡 Response length: ${response.length} chars")
                Log.d(TAG, "📡 Response preview: ${response.take(500)}")

                if (response.isBlank()) {
                    Log.e(TAG, "❌ Response kosong!")
                    return@withContext Result.failure(Exception("Server mengembalikan response kosong"))
                }

                val json = JSONObject(response)

                val success = json.optBoolean("success", false)
                val message = json.optString("message", "")
                Log.d(TAG, "📡 API Success: $success, Message: $message")

                if (!success) {
                    Log.w(TAG, "⚠️ API returned success=false: $message")
                    return@withContext Result.success(emptyList())
                }

                if (!json.has("data")) {
                    Log.e(TAG, "❌ JSON tidak memiliki field 'data'")
                    Log.e(TAG, "JSON keys: ${json.keys().asSequence().toList()}")
                    return@withContext Result.failure(Exception("Response tidak memiliki data"))
                }

                val dataArray = json.getJSONArray("data")
                Log.d(TAG, "📡 Data array length: ${dataArray.length()}")

                if (dataArray.length() == 0) {
                    Log.w(TAG, "⚠️ Data array kosong")
                    return@withContext Result.success(emptyList())
                }

                val list = mutableListOf<UjianData>()

                for (i in 0 until dataArray.length()) {
                    try {
                        val item = dataArray.getJSONObject(i)
                        Log.d(TAG, "  Processing item $i: ${item}")

                        val tanggal = item.optString("Tanggal_ujian", "")
                            .takeIf { it.isNotBlank() }
                            ?: item.optString("tanggal_ujian", "")

                        if (tanggal.isBlank()) {
                            Log.w(TAG, "⚠️ Item $i: tanggal kosong, skip")
                            continue
                        }

                        val nilai = item.optDouble("nilai_total", -1.0)

                        if (nilai < 0) {
                            Log.w(TAG, "⚠️ Item $i: nilai tidak valid ($nilai), skip")
                            continue
                        }

                        val cleanTanggal = tanggal.take(10)
                        val ujianData = UjianData(
                            tanggalUjian = cleanTanggal,
                            nilaiTotal = nilai
                        )

                        list.add(ujianData)
                        Log.d(TAG, "  ✓ Item $i: $cleanTanggal = $nilai")

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error parsing item $i: ${e.message}", e)
                    }
                }

                Log.d(TAG, "✅ Successfully parsed: ${list.size} items")

                if (list.isEmpty()) {
                    Log.w(TAG, "⚠️ Tidak ada data valid yang bisa di-parse")
                }

                Result.success(list)

            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "❌ Network Error - Unknown Host: ${e.message}")
                Result.failure(Exception("Tidak dapat terhubung ke server. Periksa koneksi internet."))
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "❌ Network Error - Timeout: ${e.message}")
                Result.failure(Exception("Koneksi timeout. Server tidak merespons."))
            } catch (e: java.io.IOException) {
                Log.e(TAG, "❌ IO Error: ${e.message}", e)
                Result.failure(Exception("Error koneksi: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected Error: ${e.javaClass.simpleName} - ${e.message}", e)
                Result.failure(e)
            } finally {
                connection?.disconnect()
                Log.d(TAG, "📡 Connection closed")
            }
        }
    }

    private fun processUjianData(ujianList: List<UjianData>) {
        monthlyData.clear()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))

        Log.d(TAG, "📊 Processing ${ujianList.size} ujian...")

        for (ujian in ujianList) {
            try {
                val date = dateFormat.parse(ujian.tanggalUjian)
                if (date == null) {
                    Log.w(TAG, "  ✗ Cannot parse date: ${ujian.tanggalUjian}")
                    continue
                }

                val monthName = monthFormat.format(date)

                monthlyData.getOrPut(monthName) { mutableListOf() }.add(ujian.nilaiTotal)

                Log.d(TAG, "  ✓ $monthName: ${ujian.nilaiTotal}")
            } catch (e: Exception) {
                Log.e(TAG, "  ✗ Error processing ${ujian.tanggalUjian}: ${e.message}")
            }
        }

        Log.d(TAG, "📊 Total months: ${monthlyData.size}")
        monthlyData.forEach { (month, values) ->
            val avg = values.average()
            Log.d(TAG, "  $month: ${values.size} ujian, avg=${String.format(Locale.getDefault(), "%.2f", avg)}")
        }
    }

    private fun updateChart() {
        try {
            Log.d(TAG, "📈 Updating chart...")

            if (lineChart == null) {
                Log.e(TAG, "⚠️ Chart is null!")
                return
            }

            if (monthlyData.isEmpty()) {
                Log.w(TAG, "⚠️ No monthly data!")
                showEmptyChart()
                return
            }

            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val sortedMonths = monthlyData.keys.sortedByDescending {
                try {
                    dateFormat.parse(it)?.time ?: 0L
                } catch (_: Exception) {
                    0L
                }
            }

            Log.d(TAG, "📊 All months sorted: $sortedMonths")

            val last3Months = sortedMonths.take(3).reversed()
            Log.d(TAG, "📊 Last 3 months (oldest to newest): $last3Months")

            val entries = mutableListOf<Entry>()
            val labels = mutableListOf<String>()

            last3Months.forEachIndexed { index, month ->
                val values = monthlyData[month]
                if (values != null && values.isNotEmpty()) {
                    val avg = values.average().toFloat()
                    entries.add(Entry(index.toFloat(), avg))

                    val monthName = month.split(" ")[0]
                    labels.add(monthName)

                    Log.d(TAG, "  ✓ Entry[$index]: x=$index, y=${String.format(Locale.getDefault(), "%.2f", avg)}, label='$monthName'")
                }
            }

            Log.d(TAG, "📊 Final labels: $labels (size=${labels.size})")
            Log.d(TAG, "📊 Final entries: ${entries.size}")

            if (entries.isEmpty()) {
                Log.w(TAG, "⚠️ No entries created!")
                showEmptyChart()
                return
            }

            val dataSet = LineDataSet(entries, "Rata-rata Nilai").apply {
                color = "#00DF82".toColorInt()
                lineWidth = 3f
                setCircleColor("#00DF82".toColorInt())
                circleRadius = 8f
                setDrawCircleHole(true)
                circleHoleRadius = 4f
                circleHoleColor = Color.WHITE
                valueTextSize = 13f
                valueTextColor = Color.BLACK
                setDrawValues(true)
                mode = LineDataSet.Mode.LINEAR
                setDrawFilled(true)
                fillColor = "#00DF82".toColorInt()
                fillAlpha = 40
            }

            lineChart?.apply {
                // Set data
                data = LineData(dataSet)

                // Configure X axis with custom formatter
                xAxis.apply {
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            val label = if (index >= 0 && index < labels.size) {
                                labels[index]
                            } else {
                                ""
                            }
                            Log.d(TAG, "X-Axis formatter: value=$value, index=$index, label='$label'")
                            return label
                        }
                    }

                    // Set range untuk 3 bulan
                    axisMinimum = -0.5f
                    axisMaximum = (labels.size - 0.5f)
                    setLabelCount(labels.size, true)
                    granularity = 1f

                    // Styling untuk lebih jelas
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridColor = "#40000000".toColorInt()
                    textColor = Color.BLACK
                    textSize = 14f
                    axisLineColor = Color.BLACK
                    axisLineWidth = 2f
                    yOffset = 20f
                    setCenterAxisLabels(false)

                    Log.d(TAG, "📊 X-Axis configured: labels=$labels")
                }

                // Ensure chart shows all data
                setVisibleXRangeMaximum(labels.size.toFloat())
                setVisibleXRangeMinimum(labels.size.toFloat())
                moveViewToX(0f)

                // Force refresh
                notifyDataSetChanged()
                invalidate()

                // Delayed refresh untuk memastikan
                postDelayed({
                    invalidate()
                    Log.d(TAG, "✅ Chart refreshed (delayed)")
                }, 100)

                Log.d(TAG, "✅ Chart configured with ${labels.size} labels")
            }

            Log.d(TAG, "✅ Chart updated successfully with ${entries.size} points")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Update chart error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun updateTable() {
        try {
            val tableCard = findViewById<LinearLayout>(R.id.table_card)

            while (tableCard.childCount > 1) {
                tableCard.removeViewAt(1)
            }

            if (monthlyData.isEmpty()) return

            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val sorted = monthlyData.toSortedMap(compareByDescending {
                try {
                    dateFormat.parse(it)?.time ?: 0L
                } catch (_: Exception) {
                    0L
                }
            })

            sorted.entries.forEachIndexed { index, (month, values) ->
                val avg = values.average()

                val row = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (36 * resources.displayMetrics.density).toInt()
                    )
                    orientation = LinearLayout.HORIZONTAL
                }

                row.addView(createTableCell((index + 1).toString(), 0.8f))
                row.addView(createTableCell(month, 1.5f))
                row.addView(createTableCell(String.format(Locale.getDefault(), "%.2f", avg), 1.7f))

                tableCard.addView(row)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Update table error: ${e.message}")
        }
    }

    private fun createTableCell(text: String, weight: Float): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            this.text = text
            textSize = 12f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.button_border)
        }
    }

    private fun clearTable() {
        try {
            val tableCard = findViewById<LinearLayout>(R.id.table_card)
            while (tableCard.childCount > 1) {
                tableCard.removeViewAt(1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Clear table error: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}