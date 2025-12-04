package com.example.tpqapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class HistoriActivity : AppCompatActivity() {

    private var isSuperAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_histori) // Layout histori yang Anda berikan

        // Cek apakah user adalah super admin
        isSuperAdmin = intent.getBooleanExtra("IS_SUPER_ADMIN", false)

        // Inisialisasi elemen UI dari layout histori
        val btnEdit = findViewById<Button>(R.id.btn_edit)
        val btnMulai = findViewById<Button>(R.id.btn_mulai)
        val btnLihat = findViewById<Button>(R.id.btn_lihat)
        val btnTambah = findViewById<Button>(R.id.btn_tambah)
        val btnHome = findViewById<LinearLayout>(R.id.btn_home)
        val btnSettings = findViewById<LinearLayout>(R.id.btn_settings)

        // Klik pada tombol Edit
        btnEdit?.setOnClickListener {
            if (isSuperAdmin) {
                showToast("Super Admin: Mengedit histori")
                // Implementasi edit histori
            } else {
                showToast("Anda tidak memiliki akses untuk mengedit histori")
            }
        }

        // Klik pada tombol Mulai
        btnMulai?.setOnClickListener {
            showToast("Memulai analisis histori")
            // Implementasi mulai analisis
        }

        // Klik pada tombol Lihat
        btnLihat?.setOnClickListener {
            showToast("Menampilkan histori lengkap")
            // Implementasi lihat histori
        }

        // Klik pada tombol Tambah
        btnTambah?.setOnClickListener {
            if (isSuperAdmin) {
                showToast("Super Admin: Menambah data histori")
                // Implementasi tambah histori
            } else {
                showToast("Anda tidak memiliki akses untuk menambah histori")
            }
        }

        // Klik pada Home Button
        btnHome?.setOnClickListener {
            navigateBackToHome()
        }

        // Klik pada Settings Button
        btnSettings?.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", isSuperAdmin)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun navigateBackToHome() {
        val intent = if (isSuperAdmin) {
            Intent(this, HomeDev::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // Handle tombol back
    override fun onBackPressed() {
        navigateBackToHome()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}