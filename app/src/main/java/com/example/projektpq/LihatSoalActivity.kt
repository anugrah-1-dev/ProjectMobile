package com.example.projektpq

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
    private var currentJilidId: Int = 1
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

    // Score Input TextViews
    private lateinit var score1TextView: TextView
    private lateinit var score2TextView: TextView
    private lateinit var score3TextView: TextView
    private lateinit var score4TextView: TextView

    // Question Containers
    private lateinit var container1: View
    private lateinit var container2: View
    private lateinit var container3: View
    private lateinit var container4: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lihat_soal)

        apiService = MySQLApiService()

        // Get jilid_id from intent
        currentJilidId = intent.getIntExtra("jilid_id", 1)

        initViews()
        setupClickListeners()
        loadSoalData()
    }

    private fun initViews() {
        // Title
        jilidTitle = findViewById(R.id.jilid_i)

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

        // Score Input TextViews
        score1TextView = findViewById(R.id.berikan_nil_1)
        score2TextView = findViewById(R.id.berikan_nil_2)
        score3TextView = findViewById(R.id.berikan_nil_3)
        score4TextView = findViewById(R.id.berikan_nil_4)

        // Question Containers
        container1 = findViewById(R.id.rectangle_1_1)
        container2 = findViewById(R.id.rectangle_1_2)
        container3 = findViewById(R.id.rectangle_1_3)
        container4 = findViewById(R.id.rectangle_1_4)

        // Set jilid title
        updateJilidTitle()
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
            val totalPages = (soalList.size + itemsPerPage - 1) / itemsPerPage
            if (currentPageIndex < totalPages - 1) {
                currentPageIndex++
                displayCurrentPage()
            } else {
                Toast.makeText(this, "Ini adalah halaman terakhir", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }

        choosePageButton.setOnClickListener {
            // TODO: Implement page selection dialog
            Toast.makeText(this, "Pilih halaman (coming soon)", Toast.LENGTH_SHORT).show()
        }

        // Score input click listeners
        score1TextView.setOnClickListener { showScoreInputDialog(0) }
        score2TextView.setOnClickListener { showScoreInputDialog(1) }
        score3TextView.setOnClickListener { showScoreInputDialog(2) }
        score4TextView.setOnClickListener { showScoreInputDialog(3) }
    }

    private fun updateJilidTitle() {
        jilidTitle.text = when (currentJilidId) {
            1 -> "JILID I"
            2 -> "JILID II"
            3 -> "JILID III"
            4 -> "JILID IV"
            5 -> "JILID V"
            6 -> "JILID VI"
            else -> "JILID $currentJilidId"
        }
    }

    private fun loadSoalData() {
        lifecycleScope.launch {
            try {
                val result = apiService.getSoalByJilid(currentJilidId)

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        soalList = response.data
                        displayCurrentPage()
                        Log.d("LihatSoalActivity", "Loaded ${soalList.size} soal for jilid $currentJilidId")
                    } else {
                        Toast.makeText(
                            this@LihatSoalActivity,
                            response.message,
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
                    Log.e("LihatSoalActivity", "Error loading soal", exception)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LihatSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("LihatSoalActivity", "Exception loading soal", e)
            }
        }
    }

    private fun displayCurrentPage() {
        val startIndex = currentPageIndex * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, soalList.size)
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
                    question1TextView.text = soal.isi_soal
                    score1TextView.text = "Berikan nilai....."
                }
                1 -> {
                    container2.visibility = View.VISIBLE
                    question2TextView.text = soal.isi_soal
                    score2TextView.text = "Berikan nilai....."
                }
                2 -> {
                    container3.visibility = View.VISIBLE
                    question3TextView.text = soal.isi_soal
                    score3TextView.text = "Berikan nilai....."
                }
                3 -> {
                    container4.visibility = View.VISIBLE
                    question4TextView.text = soal.isi_soal
                    score4TextView.text = "Berikan nilai....."
                }
            }
        }

        // Update navigation buttons state
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        val totalPages = (soalList.size + itemsPerPage - 1) / itemsPerPage

        // Enable/disable back button
        backButton.isEnabled = currentPageIndex > 0
        backButton.alpha = if (currentPageIndex > 0) 1.0f else 0.5f

        // Enable/disable next button
        nextButton.isEnabled = currentPageIndex < totalPages - 1
        nextButton.alpha = if (currentPageIndex < totalPages - 1) 1.0f else 0.5f
    }

    private fun showScoreInputDialog(questionIndex: Int) {
        val startIndex = currentPageIndex * itemsPerPage
        val actualIndex = startIndex + questionIndex

        if (actualIndex >= soalList.size) return

        val soal = soalList[actualIndex]

        // TODO: Implement score input dialog
        // For now, just show a toast
        Toast.makeText(
            this,
            "Input nilai untuk: ${soal.isi_soal}\nBobot: ${soal.bobot_nilai}",
            Toast.LENGTH_SHORT
        ).show()
    }
}