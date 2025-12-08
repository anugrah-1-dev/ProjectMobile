package com.example.projektpq

import android.annotation.SuppressLint
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

class TambahSoalActivity : AppCompatActivity() {

    private lateinit var apiService: MySQLApiService
    private var currentJilidId: Int = 0
    private lateinit var currentNamaJilid: String

    // Deklarasi variabel untuk UI components
    private lateinit var jilidTitle: TextView
    private lateinit var isiSoalInput: EditText
    private lateinit var bobotNilaiInput: EditText
    private lateinit var simpanButton: View
    private lateinit var homeButton: View
    private lateinit var settingButton: View

    companion object {
        private const val TAG = "TambahSoalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tambah_soal)

        // Inisialisasi API Service
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

        // Inisialisasi views
        initializeViews()

        // Setup listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        // Judul Jilid - update TextView yang sudah ada
        jilidTitle = findViewById(R.id.soal_ujian_)
        jilidTitle.text = "Soal Ujian\n$currentNamaJilid"

        // Input fields
        isiSoalInput = findViewById(R.id.edit_soal)
        bobotNilaiInput = findViewById(R.id.edit_bobot_nilai)

        // Set default bobot nilai
        bobotNilaiInput.setText("10")

        // Buttons
        simpanButton = findViewById(R.id.simpan)
        homeButton = findViewById(R.id.home_button_container)
        settingButton = findViewById(R.id.setting_button_container)
    }

    private fun setupClickListeners() {
        // Tombol Simpan
        simpanButton.setOnClickListener {
            simpanSoal()
        }

        // Tombol Home - kembali ke ManajemenSoalActivity
        homeButton.setOnClickListener {
            navigateBackToManajemen()
        }

        // Tombol Settings
        settingButton.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun simpanSoal() {
        // Ambil nilai dari input
        val isiSoal = isiSoalInput.text.toString().trim()
        val bobotNilaiStr = bobotNilaiInput.text.toString().trim()

        // Validasi input
        if (isiSoal.isEmpty()) {
            Toast.makeText(this, "Isi soal tidak boleh kosong", Toast.LENGTH_SHORT).show()
            isiSoalInput.requestFocus()
            return
        }

        if (bobotNilaiStr.isEmpty()) {
            Toast.makeText(this, "Bobot nilai tidak boleh kosong", Toast.LENGTH_SHORT).show()
            bobotNilaiInput.requestFocus()
            return
        }

        val bobotNilai = bobotNilaiStr.toIntOrNull()
        if (bobotNilai == null || bobotNilai < 1 || bobotNilai > 100) {
            Toast.makeText(this, "Bobot nilai harus angka antara 1-100", Toast.LENGTH_SHORT).show()
            bobotNilaiInput.requestFocus()
            return
        }

        // Simpan ke database
        saveSoalToDatabase(isiSoal, bobotNilai)
    }

    private fun saveSoalToDatabase(isiSoal: String, bobotNilai: Int) {
        lifecycleScope.launch {
            try {
                // Tampilkan loading
                Toast.makeText(this@TambahSoalActivity, "Menyimpan soal...", Toast.LENGTH_SHORT).show()

                // Disable button saat proses
                simpanButton.isEnabled = false

                Log.d(TAG, "Saving soal - Jilid ID: $currentJilidId, Isi: $isiSoal, Bobot: $bobotNilai")

                // Panggil API untuk menyimpan soal
                val result = apiService.addSoal(currentJilidId, isiSoal, bobotNilai)

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@TambahSoalActivity,
                            "Soal berhasil disimpan!",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(TAG, "Soal saved successfully")

                        // Reset form
                        resetForm()

                        // Optional: Kembali ke activity sebelumnya setelah 1 detik
                        // Handler(Looper.getMainLooper()).postDelayed({
                        //     navigateBackToManajemen()
                        // }, 1000)
                    } else {
                        Toast.makeText(
                            this@TambahSoalActivity,
                            response.message ?: "Gagal menyimpan soal",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "Failed to save soal: ${response.message}")
                    }
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        this@TambahSoalActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error saving soal", exception)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@TambahSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Exception saving soal", e)
            } finally {
                // Re-enable button
                simpanButton.isEnabled = true
            }
        }
    }

    private fun resetForm() {
        isiSoalInput.text.clear()
        bobotNilaiInput.setText("10") // Reset ke default
        isiSoalInput.requestFocus()
    }

    private fun navigateBackToManajemen() {
        // Kembali ke ManajemenSoalActivity dengan data yang sama
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