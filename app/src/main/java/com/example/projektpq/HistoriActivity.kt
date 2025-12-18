package com.example.projektpq

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class HistoriActivity : AppCompatActivity() {

    private lateinit var searchContainer: LinearLayout
    private lateinit var searchText: TextView
    private lateinit var contentContainer: LinearLayout
    private lateinit var requestQueue: RequestQueue
    private lateinit var btnBack: Button

    // URL API - sesuaikan dengan server Anda
    private val URL_GET_HISTORI = "https://kampunginggrisori.com/api/get_histori_ujian.php"

    private var historiList = ArrayList<HistoriUjian>()
    private var filteredList = ArrayList<HistoriUjian>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.histori)

        initViews()
        setupBackButton()
        setupSearchBar()

        requestQueue = Volley.newRequestQueue(this)
        loadHistoriData()
    }

    private fun initViews() {
        searchContainer = findViewById(R.id.search_container)
        searchText = findViewById(R.id.search_text)
        btnBack = findViewById(R.id.btn_back)

        // Perbaikan: Gunakan ScrollView
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scroll_content)
        contentContainer = scrollView.getChildAt(0) as LinearLayout
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }
    }

    private fun setupSearchBar() {
        searchContainer.setOnClickListener {
            showSearchDialog()
        }
    }

    private fun showSearchDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Cari Histori Ujian")

        val input = EditText(this)
        input.hint = "Masukkan nama siswa..."
        val padding = dpToPx(16)
        input.setPadding(padding, padding, padding, padding)
        builder.setView(input)

        builder.setPositiveButton("Cari") { dialog, _ ->
            val query = input.text.toString()
            filterHistori(query)
            searchText.text = if (query.isEmpty()) "Search..." else query
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun filterHistori(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(historiList)
        } else {
            for (histori in historiList) {
                if (histori.namaSiswa.contains(query, ignoreCase = true)) {
                    filteredList.add(histori)
                }
            }
        }

        displayHistoriData()
    }

    private fun loadHistoriData() {
        val stringRequest = StringRequest(
            Request.Method.GET, URL_GET_HISTORI,
            { response ->
                try {
                    if (response.isNotEmpty()) {
                        parseHistoriData(response)
                    } else {
                        Toast.makeText(this, "Tidak ada data histori", Toast.LENGTH_SHORT).show()
                        // Clear existing items
                        clearDynamicContent()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Clear existing items
                    clearDynamicContent()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Clear existing items
                    clearDynamicContent()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
                // Clear existing items
                clearDynamicContent()
            }
        )

        requestQueue.add(stringRequest)
    }

    private fun parseHistoriData(response: String) {
        historiList.clear()

        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val histori = HistoriUjian(
                idUjian = jsonObject.getInt("id_ujian"),
                noInduk = jsonObject.getInt("no_induk"),
                namaSiswa = jsonObject.getString("nama_siswa"),
                idJilid = jsonObject.getInt("id_jilid"),
                namaJilid = jsonObject.getString("nama_jilid"),
                nilaiTotal = jsonObject.getDouble("nilai_total"),
                status = jsonObject.getString("status"),
                tanggalUjian = jsonObject.getString("tanggal_ujian")
            )

            historiList.add(histori)
        }

        filteredList.addAll(historiList)
        displayHistoriData()
    }

    private fun clearDynamicContent() {
        // Hapus semua item dinamis kecuali elemen statis pertama (jika ada)
        // Dalam layout XML Anda, ada beberapa elemen statis
        // Kita akan menghapus semua dan menampilkan pesan kosong
        contentContainer.removeAllViews()

        val emptyText = TextView(this).apply {
            text = "Tidak ada data histori ujian"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(32)
            }
        }
        contentContainer.addView(emptyText)
    }

    private fun displayHistoriData() {
        // Clear existing content
        contentContainer.removeAllViews()

        if (filteredList.isEmpty()) {
            // Tampilkan pesan jika tidak ada data
            val emptyText = TextView(this).apply {
                text = "Tidak ada data histori ujian"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(32)
                }
            }
            contentContainer.addView(emptyText)
            return
        }

        // Sort by date (newest first)
        val sortedList = filteredList.sortedByDescending {
            it.tanggalUjian
        }

        // Group by month
        val groupedByMonth = sortedList.groupBy {
            getMonthYear(it.tanggalUjian)
        }

        // Add items
        for ((monthYear, items) in groupedByMonth) {
            // Add month header
            addMonthHeader(monthYear)

            // Add items for this month
            for (histori in items) {
                addHistoriItem(histori)
            }
        }
    }

    private fun addMonthHeader(monthYear: String) {
        val monthHeader = TextView(this).apply {
            text = monthYear
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
                topMargin = if (contentContainer.childCount > 0) dpToPx(16) else 0
            }
        }
        contentContainer.addView(monthHeader)
    }

    private fun addHistoriItem(histori: HistoriUjian) {
        // Buat card container
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            setBackgroundResource(R.drawable.rounded_card) // Gunakan drawable dari XML
            elevation = dpToPx(2).toFloat()
        }

        // Buat horizontal container
        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Left side (info siswa)
        val leftLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Tanggal
        val tvTanggal = TextView(this).apply {
            text = formatTanggal(histori.tanggalUjian)
            textSize = 10f
            setTextColor(0xFF999999.toInt())
        }

        // Nama siswa
        val tvNama = TextView(this).apply {
            text = histori.namaSiswa
            textSize = 14f
            setTextColor(0xFF000000.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
        }

        // Deskripsi
        val tvDeskripsi = TextView(this).apply {
            text = "Telah melakukan ujian ${histori.namaJilid}"
            textSize = 11f
            setTextColor(0xFF666666.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(2)
            }
        }

        leftLayout.addView(tvTanggal)
        leftLayout.addView(tvNama)
        leftLayout.addView(tvDeskripsi)

        // Right side (nilai)
        val rightLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvNilai = TextView(this).apply {
            text = "Nilai: ${histori.nilaiTotal.toInt()}"
            textSize = 12f
            setTextColor(0xFF000000.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        rightLayout.addView(tvNilai)

        // Gabungkan semua
        horizontalLayout.addView(leftLayout)
        horizontalLayout.addView(rightLayout)
        cardLayout.addView(horizontalLayout)

        contentContainer.addView(cardLayout)
    }

    private fun getMonthYear(tanggal: String): String {
        return try {
            // Format: 2025-12-07 -> Desember 2025
            val parts = tanggal.split("-")
            if (parts.size != 3) return "Unknown Date"

            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "Januari"
                "02" -> "Februari"
                "03" -> "Maret"
                "04" -> "April"
                "05" -> "Mei"
                "06" -> "Juni"
                "07" -> "Juli"
                "08" -> "Agustus"
                "09" -> "September"
                "10" -> "Oktober"
                "11" -> "November"
                "12" -> "Desember"
                else -> "Unknown"
            }
            "$month $year"
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    private fun formatTanggal(tanggal: String): String {
        return try {
            // Format: 2025-12-07 -> 07/12/2025
            val parts = tanggal.split("-")
            if (parts.size != 3) return tanggal
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            tanggal
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // Data class untuk menyimpan data histori ujian
    data class HistoriUjian(
        val idUjian: Int,
        val noInduk: Int,
        val namaSiswa: String,
        val idJilid: Int,
        val namaJilid: String,
        val nilaiTotal: Double,
        val status: String,
        val tanggalUjian: String
    )
}