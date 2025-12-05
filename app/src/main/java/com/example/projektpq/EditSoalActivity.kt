package com.example.projektpq

import android.app.AlertDialog
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

    // Edit Buttons - PERBAIKAN: Gunakan ID yang sesuai dengan layout
    private lateinit var editButton1: View
    private lateinit var editButton2: View
    private lateinit var editButton3: View
    private lateinit var editButton4: View

    // Question Containers
    private lateinit var container1: View
    private lateinit var container2: View
    private lateinit var container3: View
    private lateinit var container4: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_soal)

        apiService = MySQLApiService()

        // Get jilid_id from intent
        currentJilidId = intent.getIntExtra("jilid_id", 1).also {
            // Validasi jilid_id
            if (it !in 1..6) {
                Toast.makeText(this, "Jilid ID tidak valid", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }

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

        // Edit Buttons - PERBAIKAN: Cek apakah ID ini benar-benar ada di layout edit_soal.xml
        // Jika tidak ada, Anda perlu menambahkannya di XML atau gunakan ID yang sudah ada
        editButton1 = findViewById(R.id.edit_pencil_1)
        editButton2 = findViewById(R.id.edit_pencil_2)
        editButton3 = findViewById(R.id.edit_pencil_3)
        editButton4 = findViewById(R.id.edit_pencil_4)

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
            val totalPages = if (soalList.isEmpty()) 0 else (soalList.size + itemsPerPage - 1) / itemsPerPage
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
                // PERBAIKAN: Periksa tipe return dari getSoalByJilid
                val result = apiService.getSoalByJilid(currentJilidId)

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        soalList = response.data
                        currentPageIndex = 0 // Reset ke halaman pertama
                        displayCurrentPage()
                        Log.d("EditSoalActivity", "Loaded ${soalList.size} soal for jilid $currentJilidId")
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
                    Log.e("EditSoalActivity", "Error loading soal", exception)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EditSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("EditSoalActivity", "Exception loading soal", e)
            }
        }
    }

    private fun displayCurrentPage() {
        val startIndex = currentPageIndex * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, soalList.size)

        // Cek apakah ada data
        if (startIndex >= soalList.size) {
            // Kosongkan semua container
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
                    editButton1.visibility = View.VISIBLE
                }
                1 -> {
                    container2.visibility = View.VISIBLE
                    question2TextView.text = soal.isi_soal ?: ""
                    editButton2.visibility = View.VISIBLE
                }
                2 -> {
                    container3.visibility = View.VISIBLE
                    question3TextView.text = soal.isi_soal ?: ""
                    editButton3.visibility = View.VISIBLE
                }
                3 -> {
                    container4.visibility = View.VISIBLE
                    question4TextView.text = soal.isi_soal ?: ""
                    editButton4.visibility = View.VISIBLE
                }
            }
        }

        // Hide edit buttons for empty containers
        for (i in currentPageSoal.size until 4) {
            when (i) {
                0 -> editButton1.visibility = View.GONE
                1 -> editButton2.visibility = View.GONE
                2 -> editButton3.visibility = View.GONE
                3 -> editButton4.visibility = View.GONE
            }
        }

        // Update navigation buttons state
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
    }

    private fun updateNavigationButtons() {
        val totalPages = if (soalList.isEmpty()) 0 else (soalList.size + itemsPerPage - 1) / itemsPerPage

        // Enable/disable back button
        backButton.isEnabled = currentPageIndex > 0
        backButton.alpha = if (currentPageIndex > 0) 1.0f else 0.5f

        // Enable/disable next button
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

        // PERBAIKAN: Pastikan nama layout dialog benar
        // Jika layout dialog adalah dialog_edit_soal (bukan dialog_edit_soul)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_soal, null)

        // PERBAIKAN: Pastikan ID ini ada di layout dialog_edit_soal.xml
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
                    // Validasi bobot nilai
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
        // PERBAIKAN: Pastikan nama layout dialog benar
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_soal, null)

        // PERBAIKAN: Pastikan ID ini ada di layout dialog_edit_soal.xml
        val questionEditText = dialogView.findViewById<EditText>(R.id.edit_question)
        val bobotEditText = dialogView.findViewById<EditText>(R.id.edit_bobot)

        // Fokus ke input question
        questionEditText.requestFocus()
        bobotEditText.setText("10") // Default bobot nilai

        AlertDialog.Builder(this)
            .setTitle("Tambah Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Tambah") { _, _ ->
                val newQuestion = questionEditText.text.toString().trim()
                val bobot = bobotEditText.text.toString().toIntOrNull() ?: 10

                if (newQuestion.isNotEmpty()) {
                    // Validasi bobot
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
                // PERBAIKAN: Periksa apakah method updateSoal ada di MySQLApiService
                val result = apiService.updateSoal(soalId, newQuestion, newBobot)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@EditSoalActivity,
                            "Soal berhasil diperbarui",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reload data
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
                // PERBAIKAN: Periksa apakah method addSoal ada di MySQLApiService
                val result = apiService.addSoal(currentJilidId, newQuestion, bobot)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@EditSoalActivity,
                            "Soal berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reload data
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
                // PERBAIKAN: Periksa apakah method deleteSoal ada di MySQLApiService
                val result = apiService.deleteSoal(soalId)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@EditSoalActivity,
                            "Soal berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Reload data
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
}