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

        // Card Manajemen Murid
        findViewById<LinearLayout>(R.id.card_manajemen_murid).setOnClickListener {
            Toast.makeText(this, "Manajemen Murid", Toast.LENGTH_SHORT).show()
            // Navigate to ManajemenMuridActivity
            val intent = Intent(this, ManajemenMuridActivity::class.java)
            startActivity(intent)
        }

        // Card Manajemen Soal
        findViewById<LinearLayout>(R.id.card_manajemen_soal).setOnClickListener {
            Toast.makeText(this, "Manajemen Soal", Toast.LENGTH_SHORT).show()
            // Navigate to ManajemenSoalActivity
            val intent = Intent(this, PilihJilidActivity::class.java)
            startActivity(intent)
        }

        // Card Akun
        findViewById<LinearLayout>(R.id.card_akun).setOnClickListener {
            Toast.makeText(this, "Akun", Toast.LENGTH_SHORT).show()
            // Navigate to AkunActivity
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
        }

        // Card Histori
        findViewById<LinearLayout>(R.id.card_histori).setOnClickListener {
            Toast.makeText(this, "Histori", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to HistoriActivity
             val intent = Intent(this, HistoriActivity::class.java)
             startActivity(intent)
        }

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.btn_home).setOnClickListener {
            // Already on home, do nothing or refresh
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btn_settings).setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to SettingsActivity
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }
    }
}