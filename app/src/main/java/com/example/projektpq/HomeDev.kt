package com.example.tpqapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

class HomeDev : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Asumsi menggunakan layout yang sama

        // Inisialisasi elemen UI yang sama
        val cardManajemenSoal = findViewById<LinearLayout>(R.id.card_manajemen_soal)
        val cardManajemenMurid = findViewById<LinearLayout>(R.id.card_manajemen_murid)
        val cardAkun = findViewById<LinearLayout>(R.id.card_akun)
        val cardHistori = findViewById<LinearLayout>(R.id.card_histori)
        val btnHome = findViewById<LinearLayout>(R.id.btn_home)
        val btnSettings = findViewById<LinearLayout>(R.id.btn_settings)

        // Klik pada Card Manajemen Soal
        cardManajemenSoal.setOnClickListener {
            val intent = Intent(this, PilihJilidActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true) // Flag untuk super admin
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Card Manajemen Murid
        cardManajemenMurid.setOnClickListener {
            val intent = Intent(this, ManajemenMuridActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Card Akun
        cardAkun.setOnClickListener {
            val intent = Intent(this, AkunActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Klik pada Card Histori
        cardHistori.setOnClickListener {
            val intent = Intent(this, HistoriActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Tombol home dan settings sama seperti HomeActivity
        btnHome.setOnClickListener {
            Toast.makeText(this, "Super Admin - Halaman Utama", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}