package com.example.tpqapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home) // Sesuaikan dengan layout home Anda

        // Inisialisasi elemen UI dari layout yang Anda berikan
        val cardManajemenSoal = findViewById<LinearLayout>(R.id.card_manajemen_soal)
        val cardManajemenMurid = findViewById<LinearLayout>(R.id.card_manajemen_murid)
        val cardAkun = findViewById<LinearLayout>(R.id.card_akun)
        val cardHistori = findViewById<LinearLayout>(R.id.card_histori)
        val btnHome = findViewById<LinearLayout>(R.id.btn_home)
        val btnSettings = findViewById<LinearLayout>(R.id.btn_settings)

        // Klik pada Card Manajemen Soal
        cardManajemenSoal.setOnClickListener {
            // Navigasi ke PilihJilidActivity
            val intent = Intent(this, PilihJilidActivity::class.java)
            startActivity(intent)
            // Tambahkan animasi jika diperlukan
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Card Manajemen Murid
        cardManajemenMurid.setOnClickListener {
            // Navigasi ke ManajemenMuridActivity yang sudah ada
            val intent = Intent(this, ManajemenMuridActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Card Akun
        cardAkun.setOnClickListener {
            // Navigasi ke AkunActivity yang sudah ada
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Card Histori
        cardHistori.setOnClickListener {
            // Navigasi ke HistoriActivity
            val intent = Intent(this, HistoriActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Home Button (sudah di home, tidak perlu navigasi)
        btnHome.setOnClickListener {
            // Jika sudah di home, refresh atau tidak lakukan apa-apa
            Toast.makeText(this, "Anda sudah berada di halaman utama", Toast.LENGTH_SHORT).show()
        }

        // Klik pada Settings Button
        btnSettings.setOnClickListener {
            // Navigasi ke PengaturanActivity yang sudah ada
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    // Handle tombol back
    override fun onBackPressed() {
        // Konfirmasi keluar aplikasi
        showExitConfirmation()
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Ya") { _, _ ->
                // Tutup aplikasi
                finishAffinity()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}