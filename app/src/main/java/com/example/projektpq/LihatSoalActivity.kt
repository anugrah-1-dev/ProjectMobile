package com.example.projektpq

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.MySQLApiService
import kotlinx.coroutines.launch

class LihatSoalActivity : AppCompatActivity() {

    private lateinit var apiService: MySQLApiService
    private var soalList: List<MySQLApiService.SoalData> = listOf()
    private var currentJilidId: Int = 0
    private lateinit var currentNamaJilid: String
    private var currentPageIndex: Int = 0
    private val itemsPerPage: Int = 4

    // UI Components
    private lateinit var jilidTitle: TextView
    private lateinit var backButton: View
    private lateinit var nextButton: View
    private lateinit var cancelButton: View
    private lateinit var choosePageButton: View

    // Question TextViews
    private lateinit var question1TextView: TextView
    private lateinit var question2TextView: TextView
    private lateinit var question3TextView: TextView
    private lateinit var question4TextView: TextView

    // Score Display TextViews
    private lateinit var score1TextView: TextView
    private lateinit var score2TextView: TextView
    private lateinit var score3TextView: TextView
    private lateinit var score4TextView: TextView

    // Question Containers
    private lateinit var container1: View
    private lateinit var container2: View
    private lateinit var container3: View
    private lateinit var container4: View

    companion object {
        private const val TAG = "LihatSoalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lihat_soal)

        apiService = MySQLApiService()

        // PERUBAHAN: Ambil data dari intent dengan KEY yang benar
        currentJilidId = intent.getIntExtra("ID_JILID", 0)
        currentNamaJilid = intent.getStringExtra("NAMA_JILID") ?: "JILID I"

        // Validasi ID Jilid
        if (currentJilidId == 0) {
            Toast.makeText(this, "Error: ID Jilid tidak valid", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "ID Jilid tidak ditemukan dari intent")
            finish()
            return
        }

        Log.d(TAG, "Received - ID Jilid: $currentJilidId, Nama Jilid: $currentNamaJilid")

        initViews()
        setupClickListeners()
        loadSoalData()
    }

    private fun initViews() {
        // Title
        jilidTitle = findViewById(R.id.jilid_i)
        jilidTitle.text = currentNamaJilid

        // Navigation Buttons
        backButton = findViewById(R.id.back_button)
        nextButton = findViewById(R.id.next_button)
        cancelButton = findViewById(R.id.cancel)
        choosePageButton = findViewById(R.id.choose_page)

        // Question TextViews
        question1TextView = findViewById(R.id.bacakan_kal_1)
        question2TextView = findViewById(R.id.bacakan_kal_2)
        question3TextView = findViewById(R.id.bacakan_kal_3)
        question4TextView = findViewById(R.id.bacakan_kal_4)

        // Score Display TextViews (Bobot Nilai)
        score1TextView = findViewById(R.id.berikan_nil_1)
        score2TextView = findViewById(R.id.berikan_nil_2)
        score3TextView = findViewById(R.id.berikan_nil_3)
        score4TextView = findViewById(R.id.berikan_nil_4)

        // Question Containers
        container1 = findViewById(R.id.rectangle_1_1)
        container2 = findViewById(R.id.rectangle_1_2)
        container3 = findViewById(R.id.rectangle_1_3)
        container4 = findViewById(R.id.rectangle_1_4)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            if (currentPageIndex > 0) {
                currentPageIndex--
                displayCurrentPage()
            } else {
                Toast.makeText(this, "Ini adalah halaman pertama", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            val totalPages = if (soalList.isEmpty()) 0 else (soalList.size + itemsPerPage - 1) / itemsPerPage
            if (currentPageIndex < totalPages - 1) {
                currentPageIndex++
                displayCurrentPage()
            } else {
                Toast.makeText(this, "Ini adalah halaman terakhir", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            navigateBackToManajemen()
        }

        choosePageButton.setOnClickListener {
            showPageSelectionDialog()
        }
    }

    private fun loadSoalData() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@LihatSoalActivity, "Memuat data soal...", Toast.LENGTH_SHORT).show()

                Log.d(TAG, "Loading soal for Jilid ID: $currentJilidId")

                val result = apiService.getSoalByJilid(currentJilidId)

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        soalList = response.data
                        currentPageIndex = 0
                        displayCurrentPage()
                        Log.d(TAG, "Loaded ${soalList.size} soal for jilid $currentJilidId")

                        if (soalList.isEmpty()) {
                            Toast.makeText(
                                this@LihatSoalActivity,
                                "Belum ada soal untuk $currentNamaJilid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LihatSoalActivity,
                            response.message ?: "Gagal memuat soal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@LihatSoalActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error loading soal", exception)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LihatSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Exception loading soal", e)
            }
        }
    }

    private fun displayCurrentPage() {
        val startIndex = currentPageIndex * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, soalList.size)

        // Cek apakah ada data
        if (startIndex >= soalList.size) {
            clearAllContainers()
            updateNavigationButtons()
            return
        }

        val currentPageSoal = soalList.subList(startIndex, endIndex)

        // Hide all containers first
        container1.visibility = View.GONE
        container2.visibility = View.GONE
        container3.visibility = View.GONE
        container4.visibility = View.GONE

        // Display soal for current page
        currentPageSoal.forEachIndexed { index, soal ->
            when (index) {
                0 -> {
                    container1.visibility = View.VISIBLE
                    question1TextView.text = soal.isi_soal ?: ""
                    score1TextView.text = "Bobot: ${soal.bobot_nilai ?: 10}"
                }
                1 -> {
                    container2.visibility = View.VISIBLE
                    question2TextView.text = soal.isi_soal ?: ""
                    score2TextView.text = "Bobot: ${soal.bobot_nilai ?: 10}"
                }
                2 -> {
                    container3.visibility = View.VISIBLE
                    question3TextView.text = soal.isi_soal ?: ""
                    score3TextView.text = "Bobot: ${soal.bobot_nilai ?: 10}"
                }
                3 -> {
                    container4.visibility = View.VISIBLE
                    question4TextView.text = soal.isi_soal ?: ""
                    score4TextView.text = "Bobot: ${soal.bobot_nilai ?: 10}"
                }
            }
        }

        updateNavigationButtons()
    }

    private fun clearAllContainers() {
        container1.visibility = View.GONE
        container2.visibility = View.GONE
        container3.visibility = View.GONE
        container4.visibility = View.GONE

        question1TextView.text = ""
        question2TextView.text = ""
        question3TextView.text = ""
        question4TextView.text = ""

        score1TextView.text = ""
        score2TextView.text = ""
        score3TextView.text = ""
        score4TextView.text = ""
    }

    private fun updateNavigationButtons() {
        val totalPages = if (soalList.isEmpty()) 0 else (soalList.size + itemsPerPage - 1) / itemsPerPage

        backButton.isEnabled = currentPageIndex > 0
        backButton.alpha = if (currentPageIndex > 0) 1.0f else 0.5f

        nextButton.isEnabled = currentPageIndex < totalPages - 1
        nextButton.alpha = if (currentPageIndex < totalPages - 1) 1.0f else 0.5f
    }

    private fun showPageSelectionDialog() {
        if (soalList.isEmpty()) {
            Toast.makeText(this, "Belum ada soal", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPages = (soalList.size + itemsPerPage - 1) / itemsPerPage
        val pageNumbers = (1..totalPages).map { "Halaman $it" }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Pilih Halaman")
            .setItems(pageNumbers) { _, which ->
                currentPageIndex = which
                displayCurrentPage()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateBackToManajemen() {
        val intent = Intent(this, ManajemenSoalActivity::class.java)
        intent.putExtra("ID_JILID", currentJilidId)
        intent.putExtra("NAMA_JILID", currentNamaJilid)
        startActivity(intent)
        finish()
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateBackToManajemen()
    }
}