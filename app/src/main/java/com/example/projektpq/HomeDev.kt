package com.example.projektpq

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

class HomeDev : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_dev)

        // Inisialisasi elemen UI sesuai dengan XML
        val cardKelolaAkun = findViewById<LinearLayout>(R.id.card_kelola_akun) // Ubah dari card_manajemen_soal
        val cardActivity = findViewById<LinearLayout>(R.id.card_activity) // Ubah dari card_manajemen_murid
        val btnHome = findViewById<LinearLayout>(R.id.btn_home)
        val btnSettings = findViewById<LinearLayout>(R.id.btn_settings)

        // Klik pada Card Kelola Akun
        cardKelolaAkun.setOnClickListener {
            val intent = Intent(this, PilihJilidActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
        }

        // Klik pada Card Activity
        cardActivity.setOnClickListener {
            val intent = Intent(this, ManajemenMuridActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
        }

        // Tombol home dan settings
        btnHome.setOnClickListener {
            Toast.makeText(this, "Super Admin - Halaman Utama", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            intent.putExtra("IS_SUPER_ADMIN", true)
            startActivity(intent)
        }
    }
}