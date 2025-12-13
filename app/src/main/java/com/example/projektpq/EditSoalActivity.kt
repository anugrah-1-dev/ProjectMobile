package com.example.projektpq

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.MySQLApiService
import kotlinx.coroutines.launch

class EditSoalActivity : AppCompatActivity() {

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

    // Edit Buttons
    private lateinit var editButton1: View
    private lateinit var editButton2: View
    private lateinit var editButton3: View
    private lateinit var editButton4: View

    // Question Containers
    private lateinit var container1: View
    private lateinit var container2: View
    private lateinit var container3: View
    private lateinit var container4: View

    // Score TextViews
    private lateinit var score1TextView: TextView
    private lateinit var score2TextView: TextView
    private lateinit var score3TextView: TextView
    private lateinit var score4TextView: TextView

    companion object {
        private const val TAG = "EditSoalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_soal)

        apiService = MySQLApiService()

        // Ambil data dari intent
        currentJilidId = intent.getIntExtra("ID_JILID", 0)
        currentNamaJilid = intent.getStringExtra("NAMA_JILID") ?: "JILID I"

        // Validasi jilid_id
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

        // Edit Buttons
        editButton1 = findViewById(R.id.edit_pencil_1)
        editButton2 = findViewById(R.id.edit_pencil_2)
        editButton3 = findViewById(R.id.edit_pencil_3)
        editButton4 = findViewById(R.id.edit_pencil_4)

        // Question Containers
        container1 = findViewById(R.id.question_container_1)
        container2 = findViewById(R.id.question_container_2)
        container3 = findViewById(R.id.question_container_3)
        container4 = findViewById(R.id.question_container_4)

        // Score TextViews
        score1TextView = findViewById(R.id.berikan_nil_1)
        score2TextView = findViewById(R.id.berikan_nil_2)
        score3TextView = findViewById(R.id.berikan_nil_3)
        score4TextView = findViewById(R.id.berikan_nil_4)
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
            showAddQuestionDialog()
        }

        // Edit button click listeners
        editButton1.setOnClickListener {
            val startIndex = currentPageIndex * itemsPerPage
            if (startIndex < soalList.size) showEditQuestionDialog(0)
        }
        editButton2.setOnClickListener {
            val startIndex = currentPageIndex * itemsPerPage + 1
            if (startIndex < soalList.size) showEditQuestionDialog(1)
        }
        editButton3.setOnClickListener {
            val startIndex = currentPageIndex * itemsPerPage + 2
            if (startIndex < soalList.size) showEditQuestionDialog(2)
        }
        editButton4.setOnClickListener {
            val startIndex = currentPageIndex * itemsPerPage + 3
            if (startIndex < soalList.size) showEditQuestionDialog(3)
        }
    }

    private fun loadSoalData() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@EditSoalActivity, "Memuat data soal...", Toast.LENGTH_SHORT).show()

                Log.d(TAG, "Loading soal for Jilid ID: $currentJilidId")

                val result = apiService.getSoalByJilid(currentJilidId)

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        soalList = response.data
                        currentPageIndex = 0 // Reset ke halaman pertama
                        displayCurrentPage()
                        Log.d(TAG, "Loaded ${soalList.size} soal for jilid $currentJilidId")

                        if (soalList.isEmpty()) {
                            Toast.makeText(
                                this@EditSoalActivity,
                                "Belum ada soal untuk $currentNamaJilid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@EditSoalActivity,
                            response.message ?: "Gagal memuat soal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@EditSoalActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error loading soal", exception)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditSoalActivity,
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
                    score1TextView.text = "Berikan nilai....."
                    editButton1.visibility = View.VISIBLE
                }
                1 -> {
                    container2.visibility = View.VISIBLE
                    question2TextView.text = soal.isi_soal ?: ""
                    score2TextView.text = "Berikan nilai....."
                    editButton2.visibility = View.VISIBLE
                }
                2 -> {
                    container3.visibility = View.VISIBLE
                    question3TextView.text = soal.isi_soal ?: ""
                    score3TextView.text = "Berikan nilai....."
                    editButton3.visibility = View.VISIBLE
                }
                3 -> {
                    container4.visibility = View.VISIBLE
                    question4TextView.text = soal.isi_soal ?: ""
                    score4TextView.text = "Berikan nilai....."
                    editButton4.visibility = View.VISIBLE
                }
            }
        }

        // Hide edit buttons for empty containers
        for (i in currentPageSoal.size until 4) {
            when (i) {
                0 -> {
                    editButton1.visibility = View.GONE
                    score1TextView.text = ""
                }
                1 -> {
                    editButton2.visibility = View.GONE
                    score2TextView.text = ""
                }
                2 -> {
                    editButton3.visibility = View.GONE
                    score3TextView.text = ""
                }
                3 -> {
                    editButton4.visibility = View.GONE
                    score4TextView.text = ""
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

        editButton1.visibility = View.GONE
        editButton2.visibility = View.GONE
        editButton3.visibility = View.GONE
        editButton4.visibility = View.GONE

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

    private fun showEditQuestionDialog(questionIndex: Int) {
        val startIndex = currentPageIndex * itemsPerPage
        val actualIndex = startIndex + questionIndex

        if (actualIndex >= soalList.size) {
            Toast.makeText(this, "Soal tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val soal = soalList[actualIndex]

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_soal, null)
        val questionEditText = dialogView.findViewById<EditText>(R.id.edit_question)
        val bobotEditText = dialogView.findViewById<EditText>(R.id.edit_bobot)

        questionEditText.setText(soal.isi_soal ?: "")
        bobotEditText.setText(soal.bobot_nilai?.toString() ?: "10")

        AlertDialog.Builder(this)
            .setTitle("Edit Soal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newQuestion = questionEditText.text.toString().trim()
                val newBobot = bobotEditText.text.toString().toIntOrNull() ?: (soal.bobot_nilai ?: 10)

                if (newQuestion.isNotEmpty()) {
                    if (newBobot in 1..100) {
                        updateSoal(soal.id_soal, newQuestion, newBobot)
                    } else {
                        Toast.makeText(this, "Bobot nilai harus antara 1-100", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Soal tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .setNeutralButton("Hapus") { _, _ ->
                showDeleteConfirmationDialog(soal.id_soal)
            }
            .show()
    }

    private fun showAddQuestionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_soal, null)
        val questionEditText = dialogView.findViewById<EditText>(R.id.edit_question)
        val bobotEditText = dialogView.findViewById<EditText>(R.id.edit_bobot)

        questionEditText.requestFocus()
        bobotEditText.setText("10")

        AlertDialog.Builder(this)
            .setTitle("Tambah Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Tambah") { _, _ ->
                val newQuestion = questionEditText.text.toString().trim()
                val bobot = bobotEditText.text.toString().toIntOrNull() ?: 10

                if (newQuestion.isNotEmpty()) {
                    if (bobot in 1..100) {
                        addNewSoal(newQuestion, bobot)
                    } else {
                        Toast.makeText(this, "Bobot nilai harus antara 1-100", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Soal tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(soalId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Soal")
            .setMessage("Apakah Anda yakin ingin menghapus soal ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                deleteSoal(soalId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateSoal(soalId: Int, newQuestion: String, newBobot: Int) {
        lifecycleScope.launch {
            try {
                val result = apiService.updateSoal(soalId, newQuestion, newBobot)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@EditSoalActivity,
                            "Soal berhasil diperbarui",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSoalData()
                    } else {
                        Toast.makeText(
                            this@EditSoalActivity,
                            response.message ?: "Gagal memperbarui soal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@EditSoalActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addNewSoal(newQuestion: String, bobot: Int) {
        lifecycleScope.launch {
            try {
                val result = apiService.addSoal(currentJilidId, newQuestion, bobot)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@EditSoalActivity,
                            "Soal berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSoalData()
                    } else {
                        Toast.makeText(
                            this@EditSoalActivity,
                            response.message ?: "Gagal menambahkan soal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@EditSoalActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteSoal(soalId: Int) {
        lifecycleScope.launch {
            try {
                val result = apiService.deleteSoal(soalId)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@EditSoalActivity,
                            "Soal berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSoalData()
                    } else {
                        Toast.makeText(
                            this@EditSoalActivity,
                            response.message ?: "Gagal menghapus soal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@EditSoalActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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