package com.example.tpqapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import android.widget.ImageButton
import android.widget.Toast

class PilihJilidActivity : AppCompatActivity() {

    private var isSuperAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_jilid) // Layout yang Anda berikan

        // Cek apakah user adalah super admin
        isSuperAdmin = intent.getBooleanExtra("IS_SUPER_ADMIN", false)

        // Inisialisasi elemen UI
        val jilid1 = findViewById<CardView>(R.id.jilid_1)
        val jilid2 = findViewById<CardView>(R.id.jilid_2)
        val jilid3 = findViewById<CardView>(R.id.jilid_3)
        val jilid4 = findViewById<CardView>(R.id.jilid_4)
        val jilid5 = findViewById<CardView>(R.id.jilid_5)
        val jilid6 = findViewById<CardView>(R.id.jilid_6)
        val alquran = findViewById<CardView>(R.id.alquran)
        val btnHome = findViewById<ImageButton>(R.id.btn_home)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)

        // Fungsi untuk navigasi ke ManajemenSoalActivity
        fun navigateToManajemenSoal(jilid: String) {
            val intent = Intent(this, ManajemenSoalActivity::class.java)
            intent.putExtra("JILID", jilid)
            intent.putExtra("IS_SUPER_ADMIN", isSuperAdmin)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Set click listeners untuk setiap jilid
        jilid1.setOnClickListener { navigateToManajemenSoal("JILID I") }
        jilid2.setOnClickListener { navigateToManajemenSoal("JILID II") }
        jilid3.setOnClickListener { navigateToManajemenSoal("JILID III") }
        jilid4.setOnClickListener { navigateToManajemenSoal("JILID IV") }
        jilid5.setOnClickListener { navigateToManajemenSoal("JILID V") }
        jilid6.setOnClickListener { navigateToManajemenSoal("JILID VI") }
        alquran.setOnClickListener { navigateToManajemenSoal("AL-QUR'AN") }

        // Klik pada Home Button
        btnHome.setOnClickListener {
            navigateBackToHome()
        }

        // Klik pada Settings Button
        btnSettings.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", isSuperAdmin)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun navigateBackToHome() {
        // Kembali ke home sesuai tipe user
        val intent = if (isSuperAdmin) {
            Intent(this, HomeDev::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
        finish() // Tutup activity saat ini
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // Handle tombol back
    override fun onBackPressed() {
        navigateBackToHome()
    }
}