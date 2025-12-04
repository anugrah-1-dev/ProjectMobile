package com.example.tpqapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class ManajemenSoalActivity : AppCompatActivity() {

    private lateinit var jilid: String
    private var isSuperAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manajemen_soal) // Anda perlu buat layout ini

        // Ambil data dari intent
        jilid = intent.getStringExtra("JILID") ?: "JILID I"
        isSuperAdmin = intent.getBooleanExtra("IS_SUPER_ADMIN", false)

        // Set judul activity
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle?.text = "Manajemen Soal - $jilid"

        // Inisialisasi tombol-tombol
        val btnTambahSoal = findViewById<Button>(R.id.btn_tambah_soal)
        val btnLihatSoal = findViewById<Button>(R.id.btn_lihat_soal)
        val btnEditSoal = findViewById<Button>(R.id.btn_edit_soal)
        val btnHapusSoal = findViewById<Button>(R.id.btn_hapus_soal)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // Klik pada tombol Tambah Soal
        btnTambahSoal?.setOnClickListener {
            val intent = Intent(this, TambahSoalActivity::class.java)
            intent.putExtra("JILID", jilid)
            intent.putExtra("IS_SUPER_ADMIN", isSuperAdmin)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada tombol Lihat Soal
        btnLihatSoal?.setOnClickListener {
            val intent = Intent(this, LihatSoalActivity::class.java)
            intent.putExtra("JILID", jilid)
            intent.putExtra("IS_SUPER_ADMIN", isSuperAdmin)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada tombol Edit Soal
        btnEditSoal?.setOnClickListener {
            // Navigasi ke halaman edit soal
            Toast.makeText(this, "Edit Soal $jilid", Toast.LENGTH_SHORT).show()
            // Implementasi selanjutnya
        }

        // Klik pada tombol Hapus Soal
        btnHapusSoal?.setOnClickListener {
            if (isSuperAdmin) {
                // Hanya super admin yang bisa hapus soal
                showDeleteConfirmation()
            } else {
                Toast.makeText(this, "Hanya Super Admin yang dapat menghapus soal", Toast.LENGTH_SHORT).show()
            }
        }

        // Klik pada tombol Back
        btnBack?.setOnClickListener {
            navigateBack()
        }
    }

    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Soal")
            .setMessage("Apakah Anda yakin ingin menghapus semua soal untuk $jilid?")
            .setPositiveButton("Hapus") { _, _ ->
                // Logika hapus soal
                Toast.makeText(this, "Soal $jilid berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateBack() {
        val intent = Intent(this, PilihJilidActivity::class.java)
        intent.putExtra("IS_SUPER_ADMIN", isSuperAdmin)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // Handle tombol back
    override fun onBackPressed() {
        navigateBack()
    }
}