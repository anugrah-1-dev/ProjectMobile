package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        setupViews()
    }

    private fun setupViews() {
        // Card Manajemen Soal - Navigate to ManajemenSoalActivity
        findViewById<LinearLayout>(R.id.card_manajemen_soal).setOnClickListener {
            val intent = Intent(this, PilihJilidActivity::class.java)
            startActivity(intent)
        }

        // Card Manajemen Murid
        findViewById<LinearLayout>(R.id.card_manajemen_murid).setOnClickListener {
            Toast.makeText(this, "Manajemen Murid", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ManajemenMuridActivity
        }

        // Card Akun
        findViewById<LinearLayout>(R.id.card_akun).setOnClickListener {
            Toast.makeText(this, "Akun", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to AkunActivity
        }

        // Card Histori
        findViewById<LinearLayout>(R.id.card_histori).setOnClickListener {
            Toast.makeText(this, "Histori", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to HistoriActivity
        }

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.btn_home).setOnClickListener {
            // Already on home, do nothing or refresh
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btn_settings).setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to SettingsActivity
        }
    }
}