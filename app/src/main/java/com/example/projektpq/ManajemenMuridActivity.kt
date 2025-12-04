package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projektpq.models.SantriDetail
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ManajemenMuridActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var listContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var btnHome: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var loadingIndicator: ProgressBar

    private val santriList = mutableListOf<SantriDetail>()
    private val filteredList = mutableListOf<SantriDetail>()

    private val API_URL = "https://kampunginggrisori.com/api/api_santri.php"

    companion object {
        private const val TAG = "ManajemenMuridActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manajemen_murid)

        Log.d(TAG, "ManajemenMuridActivity onCreate")

        initViews()
        setupListeners()
        loadSantriData()
    }

    private fun initViews() {
        try {
            searchInput = findViewById(R.id.search_input)
            listContainer = findViewById(R.id.list_container)
            scrollView = findViewById(R.id.scroll_view)
            btnHome = findViewById(R.id.btn_home)
            btnSettings = findViewById(R.id.btn_settings)
            loadingIndicator = findViewById(R.id.loading_indicator)

            Log.d(TAG, "Semua view berhasil diinisialisasi")
        } catch (e: Exception) {
            Log.e(TAG, "Error inisialisasi views: ${e.message}")
            Toast.makeText(this, "Error inisialisasi layout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        // Listener untuk search input
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSantri(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener untuk tombol home
        btnHome.setOnClickListener {
            Log.d(TAG, "Tombol home diklik")
            finish()
        }

        // Listener untuk tombol settings
        btnSettings.setOnClickListener {
            Log.d(TAG, "Tombol settings diklik")
            Toast.makeText(this, "Pengaturan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSantriData() {
        Log.d(TAG, "Memulai load data santri")
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Mengakses API: $API_URL")
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 20000
                connection.readTimeout = 20000
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "TPQ-App-Android")

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Response berhasil diterima")
                    Log.d(TAG, "Response length: ${response.length}")

                    // Cek apakah response valid JSON
                    if (response.isNotEmpty()) {
                        try {
                            val jsonObject = JSONObject(response)
                            Log.d(TAG, "JSON parsed successfully")

                            if (jsonObject.has("success") && jsonObject.getBoolean("success")) {
                                if (jsonObject.has("data")) {
                                    val dataArray = jsonObject.getJSONArray("data")
                                    Log.d(TAG, "Jumlah data santri: ${dataArray.length()}")

                                    santriList.clear()

                                    for (i in 0 until dataArray.length()) {
                                        try {
                                            val item = dataArray.getJSONObject(i)
                                            val santri = SantriDetail(
                                                no_induk = item.optString("no_induk", ""),
                                                nama = item.optString("nama", ""),
                                                tempat_lahir = item.optString("tempat_lahir", null),
                                                tanggal_lahir = item.optString("tanggal_lahir", null),
                                                alamat = item.optString("alamat", null),
                                                jilid = item.optString("jilid", null)
                                            )

                                            if (santri.no_induk.isNotEmpty() && santri.nama.isNotEmpty()) {
                                                santriList.add(santri)
                                                Log.d(TAG, "✓ ${santri.nama} (${santri.no_induk})")
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error parsing item $i: ${e.message}")
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        filteredList.clear()
                                        filteredList.addAll(santriList)
                                        displaySantriList()
                                        showLoading(false)

                                        if (santriList.isEmpty()) {
                                            Toast.makeText(
                                                this@ManajemenMuridActivity,
                                                "Tidak ada data santri",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@ManajemenMuridActivity,
                                                "Data ${santriList.size} santri berhasil dimuat",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    throw Exception("Data tidak ditemukan dalam response")
                                }
                            } else {
                                val errorMessage = jsonObject.optString("message", "Gagal memuat data")
                                throw Exception(errorMessage)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing JSON: ${e.message}")
                            withContext(Dispatchers.Main) {
                                showLoading(false)
                                Toast.makeText(
                                    this@ManajemenMuridActivity,
                                    "Error parsing data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        throw Exception("Response kosong")
                    }
                } else {
                    val errorMessage = try {
                        connection.errorStream?.bufferedReader()?.readText() ?: "Tidak ada detail"
                    } catch (e: Exception) {
                        "Error membaca error stream"
                    }
                    throw Exception("HTTP Error $responseCode: $errorMessage")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error load data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@ManajemenMuridActivity,
                        "Gagal memuat data: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Tampilkan pesan error di UI
                    displayErrorMessage(e.localizedMessage ?: "Error tidak diketahui")
                }
            }
        }
    }

    private fun displayErrorMessage(message: String) {
        listContainer.removeAllViews()

        val errorLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(40)
            }
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val errorIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(80),
                dpToPx(80)
            )
            setImageResource(android.R.drawable.ic_dialog_alert)
            setColorFilter(ContextCompat.getColor(this@ManajemenMuridActivity, android.R.color.holo_red_dark))
        }

        val errorText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
            text = "Error: $message"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            gravity = android.view.Gravity.CENTER
        }

        val retryButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
            text = "Coba Lagi"
            setOnClickListener {
                loadSantriData()
            }
        }

        errorLayout.addView(errorIcon)
        errorLayout.addView(errorText)
        errorLayout.addView(retryButton)
        listContainer.addView(errorLayout)
    }

    private fun filterSantri(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(santriList)
        } else {
            val lowercaseQuery = query.lowercase()
            filteredList.addAll(
                santriList.filter {
                    it.nama.lowercase().contains(lowercaseQuery) ||
                            it.no_induk.lowercase().contains(lowercaseQuery) ||
                            (it.jilid?.lowercase()?.contains(lowercaseQuery) ?: false)
                }
            )
        }
        displaySantriList()
    }

    private fun displaySantriList() {
        // Hapus semua view kecuali header
        val childCount = listContainer.childCount
        if (childCount > 1) {
            listContainer.removeViews(1, childCount - 1)
        }

        if (filteredList.isEmpty()) {
            val emptyLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(40)
                }
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
            }

            val emptyIcon = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(80),
                    dpToPx(80)
                )
                setImageResource(android.R.drawable.ic_menu_search)
                setColorFilter(ContextCompat.getColor(this@ManajemenMuridActivity, android.R.color.darker_gray))
            }

            val emptyText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(16)
                }
                text = if (santriList.isEmpty()) {
                    "Belum ada data santri"
                } else if (searchInput.text.isNotEmpty()) {
                    "Tidak ditemukan untuk: \"${searchInput.text}\""
                } else {
                    "Data tidak tersedia"
                }
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                gravity = android.view.Gravity.CENTER
            }

            emptyLayout.addView(emptyIcon)
            emptyLayout.addView(emptyText)
            listContainer.addView(emptyLayout)
            return
        }

        // Tambahkan item santri
        filteredList.forEachIndexed { index, santri ->
            val itemView = createSantriItemView(index + 1, santri)
            listContainer.addView(itemView)
        }
    }

    private fun createSantriItemView(number: Int, santri: SantriDetail): View {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(70)
            ).apply {
                topMargin = dpToPx(8)
                marginStart = dpToPx(16)
                marginEnd = dpToPx(16)
            }
            orientation = LinearLayout.HORIZONTAL
            background = resources.getDrawable(R.drawable.rectangle_3, null)
            elevation = 4f

            // Kotak Nomor
            val numberLayout = FrameLayout(this@ManajemenMuridActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(60),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                addView(TextView(this@ManajemenMuridActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    text = number.toString()
                    textSize = 16f
                    setTextColor(resources.getColor(android.R.color.black, null))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
            }
            addView(numberLayout)

            // Kotak Nama dan Info
            val infoLayout = LinearLayout(this@ManajemenMuridActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dpToPx(16), 0, dpToPx(8), 0)

                // Set clickable untuk seluruh area info
                isClickable = true
                isFocusable = true
                foreground = resources.getDrawable(
                    android.R.drawable.list_selector_background,
                    null
                )
                setOnClickListener {
                    navigateToDetail(santri)
                }

                addView(TextView(this@ManajemenMuridActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = santri.nama
                    textSize = 16f
                    setTextColor(resources.getColor(android.R.color.black, null))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })

                addView(TextView(this@ManajemenMuridActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(4)
                    }
                    text = "No. Induk: ${santri.no_induk} | Jilid: ${santri.jilid ?: "-"}"
                    textSize = 12f
                    setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })
            }
            addView(infoLayout)

            // Kotak Aksi (Tombol Detail)
            val actionLayout = FrameLayout(this@ManajemenMuridActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(50),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                // Set clickable untuk area aksi
                isClickable = true
                isFocusable = true
                foreground = resources.getDrawable(
                    android.R.drawable.list_selector_background,
                    null
                )
                setOnClickListener {
                    navigateToDetail(santri)
                }

                addView(TextView(this@ManajemenMuridActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    text = "▶"
                    textSize = 18f
                    setTextColor(resources.getColor(android.R.color.darker_gray, null))
                })
            }
            addView(actionLayout)
        }
    }

    private fun navigateToDetail(santri: SantriDetail) {
        Log.d(TAG, "Navigasi ke detail santri: ${santri.nama} (${santri.no_induk})")

        try {
            // Pastikan intent menggunakan nama package yang benar
            val intent = Intent(this, DetailSiswaActivity::class.java).apply {
                // Kirim semua data yang diperlukan
                putExtra("no_induk", santri.no_induk)
                putExtra("nama", santri.nama)
                putExtra("tempat_lahir", santri.tempat_lahir ?: "")
                putExtra("tanggal_lahir", santri.tanggal_lahir ?: "")
                putExtra("alamat", santri.alamat ?: "")
                putExtra("jilid", santri.jilid ?: "")

                // Tambahkan flag untuk membersihkan stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            Log.d(TAG, "Intent dibuat, memulai DetailSiswaActivity...")
            startActivity(intent)
            Log.d(TAG, "DetailSiswaActivity berhasil dimulai")

        } catch (e: Exception) {
            Log.e(TAG, "Gagal navigasi ke detail: ${e.message}", e)

            // Debug: Cek apakah Activity terdaftar
            val packageManager = packageManager
            try {
                val intent = Intent(this, DetailSiswaActivity::class.java)
                val activities = packageManager.queryIntentActivities(intent, 0)

                if (activities.isEmpty()) {
                    Toast.makeText(
                        this,
                        "ERROR: DetailSiswaActivity belum terdaftar di AndroidManifest.xml!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "DetailSiswaActivity tidak ditemukan di AndroidManifest.xml")
                } else {
                    Toast.makeText(
                        this,
                        "Gagal membuka detail: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(TAG, "DetailSiswaActivity ditemukan di manifest")
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Error checking activity: ${e2.message}")
                Toast.makeText(
                    this,
                    "Error sistem: ${e2.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        runOnUiThread {
            if (show) {
                loadingIndicator.visibility = View.VISIBLE
                scrollView.visibility = View.GONE
            } else {
                loadingIndicator.visibility = View.GONE
                scrollView.visibility = View.VISIBLE
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}