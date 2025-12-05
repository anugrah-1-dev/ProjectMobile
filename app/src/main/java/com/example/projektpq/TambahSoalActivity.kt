package com.example.projektpq
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TambahSoalActivity : AppCompatActivity() {

    // Deklarasi variabel untuk input field
    private lateinit var nomorSoalInput: EditText
    private lateinit var soalInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tambah_soal)

        // Inisialisasi input field
        nomorSoalInput = findViewById(R.id.edit_nomor_soal)
        soalInput = findViewById(R.id.edit_soal)

        // Inisialisasi tombol simpan
        val simpanButton = findViewById<View>(R.id.simpan)
        val simpanContainer = findViewById<View>(R.id.group_44)

        // Tombol home di bottom navigation
        val homeButton = findViewById<View>(R.id.home_button_container)
        homeButton?.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }

        // Tombol settings di bottom navigation
        val settingButton = findViewById<View>(R.id.setting_button_container)
        settingButton?.setOnClickListener {
            // Navigasi ke settings activity
            // val intent = Intent(this, SettingsActivity::class.java)
            // startActivity(intent)
        }

        // Handle klik tombol simpan
        simpanButton?.setOnClickListener {
            simpanSoal()
        }

        simpanContainer?.setOnClickListener {
            simpanSoal()
        }
    }

    private fun simpanSoal() {
        // Dapatkan nilai dari input field
        val nomorSoal = nomorSoalInput.text.toString()
        val soal = soalInput.text.toString()

        // Validasi input
        if (nomorSoal.isEmpty() || soal.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        // Simpan data ke SharedPreferences
        val sharedPref = getSharedPreferences("soal_ujian", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("nomor_$nomorSoal", soal)
            apply()
        }

        Toast.makeText(this, "Soal nomor $nomorSoal berhasil disimpan!", Toast.LENGTH_SHORT).show()

        // Reset form setelah disimpan
        resetForm()
    }

    private fun resetForm() {
        nomorSoalInput.text.clear()
        soalInput.text.clear()
        nomorSoalInput.requestFocus()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}