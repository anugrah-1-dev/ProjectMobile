package com.example.projektpq

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.MySQLApiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MulaiUjianActivity : AppCompatActivity() {

    private lateinit var apiService: MySQLApiService
    private var soalList: List<MySQLApiService.SoalData> = listOf()
    private var currentJilidId: Int = 0
    private lateinit var currentNamaJilid: String
    private var currentPageIndex: Int = 0
    private val itemsPerPage: Int = 4

    // Data siswa
    private var namaSiswa: String = ""
    private var nomorInduk: String = ""

    // Map untuk menyimpan nilai yang sudah diinput
    private val nilaiMap = mutableMapOf<Int, Int>() // soalId -> nilai

    // UI Components
    private lateinit var jilidTitle: TextView
    private lateinit var backButton: View
    private lateinit var nextButton: View
    private lateinit var cancelButton: View
    private lateinit var choosePageButton: View
    private lateinit var backText: TextView
    private lateinit var nextText: TextView

    // Question TextViews
    private lateinit var question1TextView: TextView
    private lateinit var question2TextView: TextView
    private lateinit var question3TextView: TextView
    private lateinit var question4TextView: TextView

    // Input Nilai TextViews
    private lateinit var nilai1TextView: TextView
    private lateinit var nilai2TextView: TextView
    private lateinit var nilai3TextView: TextView
    private lateinit var nilai4TextView: TextView

    // Question Containers
    private lateinit var container1: View
    private lateinit var container2: View
    private lateinit var container3: View
    private lateinit var container4: View

    companion object {
        private const val TAG = "MulaiUjianActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mulai_ujian2)

        apiService = MySQLApiService()

        // Ambil data dari intent
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

        // Tampilkan dialog input nama dan nomor induk di awal
        showInputSiswaDialog()
    }

    private fun initViews() {
        // Title
        jilidTitle = findViewById(R.id.jilid_i)
        jilidTitle.text = currentNamaJilid

        // Navigation Buttons
        backButton = findViewById(R.id.back_button)
        nextButton = findViewById(R.id.next_button)
        backText = findViewById(R.id.back)
        nextText = findViewById(R.id.next)
        cancelButton = findViewById(R.id.cancel)
        choosePageButton = findViewById(R.id.choose_page)

        // Question TextViews
        question1TextView = findViewById(R.id.bacakan_kal_1)
        question2TextView = findViewById(R.id.bacakan_kal_2)
        question3TextView = findViewById(R.id.bacakan_kal_3)
        question4TextView = findViewById(R.id.bacakan_kal_4)

        // Nilai Input TextViews
        nilai1TextView = findViewById(R.id.berikan_nil_1)
        nilai2TextView = findViewById(R.id.berikan_nil_2)
        nilai3TextView = findViewById(R.id.berikan_nil_3)
        nilai4TextView = findViewById(R.id.berikan_nil_4)

        // Question Containers
        container1 = findViewById(R.id.rectangle_1_1)
        container2 = findViewById(R.id.rectangle_1_2)
        container3 = findViewById(R.id.rectangle_1_3)
        container4 = findViewById(R.id.rectangle_1_4)
    }

    private fun setupClickListeners() {
        // Back button
        backButton.setOnClickListener { navigatePrevious() }
        backText.setOnClickListener { navigatePrevious() }

        // Next button
        nextButton.setOnClickListener { navigateNext() }
        nextText.setOnClickListener { navigateNext() }

        // Cancel button
        cancelButton.setOnClickListener { showCancelConfirmation() }

        // Choose page button
        choosePageButton.setOnClickListener { showPageSelectionDialog() }

        // Input nilai click listeners
        nilai1TextView.setOnClickListener { showInputNilaiDialog(0) }
        nilai2TextView.setOnClickListener { showInputNilaiDialog(1) }
        nilai3TextView.setOnClickListener { showInputNilaiDialog(2) }
        nilai4TextView.setOnClickListener { showInputNilaiDialog(3) }
    }

    // ==================== DIALOG INPUT NAMA & NOMOR INDUK ====================
    private fun showInputSiswaDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_nama, null)

        val namaEditText = dialogView.findViewById<EditText>(R.id.edit_nama_siswa)
        val nomorIndukEditText = dialogView.findViewById<EditText>(R.id.edit_nomor_induk)
        val mulaiButton = dialogView.findViewById<TextView>(R.id.mulai)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        mulaiButton.setOnClickListener {
            val nama = namaEditText.text.toString().trim()
            val nomor = nomorIndukEditText.text.toString().trim()

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                namaEditText.requestFocus()
                return@setOnClickListener
            }

            if (nomor.isEmpty()) {
                Toast.makeText(this, "Nomor Induk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                nomorIndukEditText.requestFocus()
                return@setOnClickListener
            }

            // Simpan data siswa
            namaSiswa = nama
            nomorInduk = nomor

            Log.d(TAG, "Siswa - Nama: $namaSiswa, Nomor Induk: $nomorInduk")

            dialog.dismiss()

            // Load soal setelah input data siswa
            loadSoalData()
        }

        dialog.show()
    }

    private fun loadSoalData() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MulaiUjianActivity, "Memuat soal...", Toast.LENGTH_SHORT).show()

                Log.d(TAG, "Loading soal for Jilid ID: $currentJilidId")

                val result = apiService.getSoalByJilid(currentJilidId)

                result.onSuccess { response ->
                    if (response.success && response.data != null && response.data.isNotEmpty()) {
                        soalList = response.data
                        currentPageIndex = 0
                        displayCurrentPage()
                        Log.d(TAG, "Loaded ${soalList.size} soal for jilid $currentJilidId")
                    } else {
                        Toast.makeText(
                            this@MulaiUjianActivity,
                            "Belum ada soal untuk $currentNamaJilid",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@MulaiUjianActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error loading soal", exception)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MulaiUjianActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Exception loading soal", e)
                finish()
            }
        }
    }

    private fun displayCurrentPage() {
        val startIndex = currentPageIndex * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, soalList.size)

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
            val displayNilai = nilaiMap[soal.id_soal]?.toString() ?: "Berikan nilai....."

            when (index) {
                0 -> {
                    container1.visibility = View.VISIBLE
                    question1TextView.text = soal.isi_soal ?: ""
                    nilai1TextView.text = displayNilai
                    nilai1TextView.tag = soal.id_soal
                }
                1 -> {
                    container2.visibility = View.VISIBLE
                    question2TextView.text = soal.isi_soal ?: ""
                    nilai2TextView.text = displayNilai
                    nilai2TextView.tag = soal.id_soal
                }
                2 -> {
                    container3.visibility = View.VISIBLE
                    question3TextView.text = soal.isi_soal ?: ""
                    nilai3TextView.text = displayNilai
                    nilai3TextView.tag = soal.id_soal
                }
                3 -> {
                    container4.visibility = View.VISIBLE
                    question4TextView.text = soal.isi_soal ?: ""
                    nilai4TextView.text = displayNilai
                    nilai4TextView.tag = soal.id_soal
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

        nilai1TextView.text = "Berikan nilai....."
        nilai2TextView.text = "Berikan nilai....."
        nilai3TextView.text = "Berikan nilai....."
        nilai4TextView.text = "Berikan nilai....."
    }

    private fun updateNavigationButtons() {
        val totalPages = if (soalList.isEmpty()) 0 else (soalList.size + itemsPerPage - 1) / itemsPerPage

        backButton.isEnabled = currentPageIndex > 0
        backButton.alpha = if (currentPageIndex > 0) 1.0f else 0.5f
        backText.alpha = if (currentPageIndex > 0) 1.0f else 0.5f

        if (currentPageIndex < totalPages - 1) {
            nextText.text = "Next"
            nextButton.isEnabled = true
            nextButton.alpha = 1.0f
            nextText.alpha = 1.0f
        } else {
            nextText.text = "Selesai"
            nextButton.isEnabled = true
            nextButton.alpha = 1.0f
            nextText.alpha = 1.0f
        }
    }

    private fun navigatePrevious() {
        if (currentPageIndex > 0) {
            currentPageIndex--
            displayCurrentPage()
        } else {
            Toast.makeText(this, "Ini adalah halaman pertama", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateNext() {
        val totalPages = if (soalList.isEmpty()) 0 else (soalList.size + itemsPerPage - 1) / itemsPerPage

        if (currentPageIndex < totalPages - 1) {
            currentPageIndex++
            displayCurrentPage()
        } else {
            showFinishConfirmation()
        }
    }

    private fun showInputNilaiDialog(questionIndex: Int) {
        val startIndex = currentPageIndex * itemsPerPage
        val actualIndex = startIndex + questionIndex

        if (actualIndex >= soalList.size) return

        val soal = soalList[actualIndex]
        val currentNilai = nilaiMap[soal.id_soal] ?: 0

        val editText = EditText(this).apply {
            hint = "Masukkan nilai (0-${soal.bobot_nilai})"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(if (currentNilai > 0) currentNilai.toString() else "")
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Input Nilai")
            .setMessage("Soal: ${soal.isi_soal}\nBobot: ${soal.bobot_nilai}")
            .setView(editText)
            .setPositiveButton("Simpan") { _, _ ->
                val inputNilai = editText.text.toString().toIntOrNull() ?: 0
                val bobotNilai = soal.bobot_nilai ?: 100

                if (inputNilai in 0..bobotNilai) {
                    nilaiMap[soal.id_soal] = inputNilai
                    displayCurrentPage()
                    Toast.makeText(this, "Nilai disimpan: $inputNilai", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Nilai harus antara 0 dan $bobotNilai",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPageSelectionDialog() {
        if (soalList.isEmpty()) {
            Toast.makeText(this, "Belum ada soal", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPages = (soalList.size + itemsPerPage - 1) / itemsPerPage
        val pageNumbers = (1..totalPages).map { "Halaman $it" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Halaman")
            .setItems(pageNumbers) { _, which ->
                currentPageIndex = which
                displayCurrentPage()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Batalkan Ujian")
            .setMessage("Apakah Anda yakin ingin membatalkan ujian? Semua nilai yang sudah diinput akan hilang.")
            .setPositiveButton("Ya") { _, _ ->
                nilaiMap.clear()
                navigateBackToManajemen()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showFinishConfirmation() {
        val totalSoal = soalList.size
        val soalDinilai = nilaiMap.size
        val soalBelumDinilai = totalSoal - soalDinilai

        val message = if (soalBelumDinilai > 0) {
            "Ada $soalBelumDinilai soal yang belum dinilai.\nApakah Anda yakin ingin menyelesaikan ujian?"
        } else {
            "Semua soal sudah dinilai.\nApakah Anda yakin ingin menyelesaikan ujian?"
        }

        AlertDialog.Builder(this)
            .setTitle("Selesaikan Ujian")
            .setMessage(message)
            .setPositiveButton("Ya") { _, _ ->
                submitUjian()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun submitUjian() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MulaiUjianActivity, "Menyimpan hasil ujian...", Toast.LENGTH_LONG).show()

                // Hitung total nilai
                var totalNilai = 0.0
                for (soal in soalList) {
                    val nilaiSoal = nilaiMap[soal.id_soal] ?: 0
                    totalNilai += nilaiSoal
                }

                // Hitung persentase
                val totalBobotMaksimal = soalList.sumOf { it.bobot_nilai ?: 0 }
                val persentase = if (totalBobotMaksimal > 0) {
                    (totalNilai / totalBobotMaksimal) * 100
                } else {
                    0.0
                }

                Log.d(TAG, "Total Nilai: $totalNilai, Persentase: $persentase%")

                val tanggalUjian = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Call API to save ujian
                val result = apiService.saveUjian(
                    noInduk = nomorInduk,
                    idJilid = currentJilidId,
                    nilaiTotal = totalNilai,
                    tanggalUjian = tanggalUjian
                )

                result.onSuccess { response ->
                    if (response.success) {
                        // Show success screen with result
                        showSelesaiUjianScreen(totalNilai, persentase)
                    } else {
                        Toast.makeText(
                            this@MulaiUjianActivity,
                            "Gagal menyimpan: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@MulaiUjianActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error saving ujian", exception)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MulaiUjianActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Exception saving ujian", e)
            }
        }
    }

    // ==================== SCREEN SELESAI UJIAN ====================
    private fun showSelesaiUjianScreen(totalNilai: Double, persentase: Double) {
        // Switch to selesai_ujian layout
        setContentView(R.layout.selesai_ujian)

        // Find views
        val namaText = findViewById<TextView>(R.id.nama_text)
        val idText = findViewById<TextView>(R.id.id_text)
        val nilaiText = findViewById<TextView>(R.id.nilai_text)
        val buttonSelesai = findViewById<Button>(R.id.button_selesai)
        val homeIcon = findViewById<ImageView>(R.id.home_icon)
        val settingsIcon = findViewById<ImageView>(R.id.settings_icon)

        // Set data
        namaText.text = namaSiswa
        idText.text = nomorInduk
        nilaiText.text = String.format("Nilai: %.2f (%.1f%%)", totalNilai, persentase)

        // Button Selesai
        buttonSelesai.setOnClickListener {
            navigateBackToManajemen()
        }

        // Home button
        homeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Settings button
        settingsIcon.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateBackToManajemen() {
        val intent = Intent(this, ManajemenSoalActivity::class.java)
        intent.putExtra("ID_JILID", currentJilidId)
        intent.putExtra("NAMA_JILID", currentNamaJilid)
        startActivity(intent)
        finish()
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Jika masih di layar ujian, tampilkan konfirmasi cancel
        // Jika di layar selesai, langsung kembali
        if (findViewById<View>(R.id.mulai_ujian2) != null) {
            showCancelConfirmation()
        } else {
            navigateBackToManajemen()
        }
    }
}