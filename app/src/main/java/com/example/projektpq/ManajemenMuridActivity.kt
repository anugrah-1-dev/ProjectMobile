package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class ManajemenMuridActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var listContainer: LinearLayout
    private lateinit var btnHome: ImageButton
    private lateinit var btnSettings: ImageButton

    private val santriList = mutableListOf<SantriDetail>()
    private val filteredList = mutableListOf<SantriDetail>()

    private val API_URL = "https://kampunginggrisori.com/api/api_santri.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manajemen_murid)

        initViews()
        setupListeners()
        loadSantriData()
    }

    private fun initViews() {
        val searchContainer = findViewById<LinearLayout>(R.id.search_container)

        searchInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                marginStart = dpToPx(12)
            }
            hint = "Cari nama"
            setTextColor(resources.getColor(android.R.color.black, null))
            setHintTextColor(resources.getColor(android.R.color.darker_gray, null))
            textSize = 16f
            background = null
        }

        // Hapus TextView "Cari nama" yang lama jika ada
        val cariNama = findViewById<TextView?>(R.id.cari_nama)
        if (cariNama != null) {
            searchContainer.removeView(cariNama)
        }

        searchContainer.addView(searchInput, 1)

        listContainer = findViewById(R.id.list_container)
        btnHome = findViewById(R.id.btn_home)
        btnSettings = findViewById(R.id.btn_settings)
    }

    private fun setupListeners() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSantri(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnHome.setOnClickListener {
            finish()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSantriData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                Log.d("ManajemenMurid", "Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d("ManajemenMurid", "Response: $response")

                    val jsonObject = JSONObject(response)

                    if (jsonObject.getBoolean("success")) {
                        val dataArray = jsonObject.getJSONArray("data")
                        santriList.clear()

                        for (i in 0 until dataArray.length()) {
                            val item = dataArray.getJSONObject(i)
                            santriList.add(
                                SantriDetail(
                                    no_induk = item.optString("no_induk", ""),
                                    nama = item.optString("nama", "")
                                )
                            )
                        }

                        withContext(Dispatchers.Main) {
                            filteredList.clear()
                            filteredList.addAll(santriList)
                            displaySantriList()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ManajemenMuridActivity,
                                "Gagal memuat data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ManajemenMuridActivity,
                            "Error: HTTP $responseCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ManajemenMurid", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ManajemenMuridActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun filterSantri(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(santriList)
        } else {
            filteredList.addAll(
                santriList.filter {
                    it.nama.contains(query, ignoreCase = true)
                }
            )
        }
        displaySantriList()
    }

    private fun displaySantriList() {
        val childCount = listContainer.childCount
        if (childCount > 1) {
            listContainer.removeViews(1, childCount - 1)
        }

        filteredList.forEachIndexed { index, santri ->
            val itemView = createSantriItemView(index + 1, santri)
            listContainer.addView(itemView)
        }

        if (filteredList.isEmpty()) {
            val emptyView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(20)
                }
                text = "Tidak ada data"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black, null))
                gravity = android.view.Gravity.CENTER
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            }
            listContainer.addView(emptyView)
        }
    }

    private fun createSantriItemView(number: Int, santri: SantriDetail): View {
        val itemLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(60)
            ).apply {
                topMargin = dpToPx(8)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        // Kotak Nomor
        val numberLayout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(80),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundResource(R.drawable.rectangle_3)
        }

        val numberText = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            text = number.toString()
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, null))
        }
        numberLayout.addView(numberText)

        // Kotak Nama
        val nameLayout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            setBackgroundResource(R.drawable.rectangle_3)
            isClickable = true
            isFocusable = true
            foreground = resources.getDrawable(
                android.R.drawable.list_selector_background,
                null
            )
        }

        val nameText = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            text = santri.nama
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, null))
            setPadding(dpToPx(16), 0, dpToPx(16), 0)
        }
        nameLayout.addView(nameText)

        nameLayout.setOnClickListener {
            val intent = Intent(this, DetailSiswaActivity::class.java)
            intent.putExtra("no_induk", santri.no_induk)
            intent.putExtra("nama", santri.nama)
            startActivity(intent)
        }

        itemLayout.addView(numberLayout)
        itemLayout.addView(nameLayout)

        return itemLayout
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}