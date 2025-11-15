package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard) // XML yang Anda berikan

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        startButton = findViewById(R.id.start_button)
    }

    private fun setupClickListeners() {
        startButton.setOnClickListener {
            // Aksi ketika tombol START ditekan
            // Bisa navigasi ke activity lain atau fungsi aplikasi utama
            navigateToNextActivity()
        }
    }

    private fun navigateToNextActivity() {
        // Contoh: navigasi ke activity berikutnya
        // val intent = Intent(this, HomeActivity::class.java)
        // startActivity(intent)

        // Untuk sekarang, tampilkan pesan atau tutup
        // finish() // Jika ingin kembali ke MainActivity
    }
}