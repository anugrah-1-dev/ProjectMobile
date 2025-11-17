package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var backPressedTime: Long = 0

    // Deklarasi views
    private lateinit var cardManajemenSoal: LinearLayout
    private lateinit var cardManajemenMurid: LinearLayout
    private lateinit var cardAkun: LinearLayout
    private lateinit var cardHistori: LinearLayout
    private lateinit var homeButton: LinearLayout
    private lateinit var settingsButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Cek apakah user sudah login
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupClickListeners()
        setupBackPressHandler()
    }

    private fun initializeViews() {
        cardManajemenSoal = findViewById(R.id.card_manajemen_soal)
        cardManajemenMurid = findViewById(R.id.card_manajemen_murid)
        cardAkun = findViewById(R.id.card_akun)
        cardHistori = findViewById(R.id.card_histori)
        homeButton = findViewById(R.id.home)
        settingsButton = findViewById(R.id.btn_settings)
    }

    private fun setupClickListeners() {
        // Card Manajemen Soal
        cardManajemenSoal.setOnClickListener {
            Toast.makeText(this, "Manajemen Soal", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ManajemenSoalActivity
            // val intent = Intent(this, ManajemenSoalActivity::class.java)
            // startActivity(intent)
        }

        // Card Manajemen Murid
        cardManajemenMurid.setOnClickListener {
            Toast.makeText(this, "Manajemen Murid", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ManajemenMuridActivity
            // val intent = Intent(this, ManajemenMuridActivity::class.java)
            // startActivity(intent)
        }

        // Card Akun
        cardAkun.setOnClickListener {
            Toast.makeText(this, "Akun", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to AkunActivity
            // val intent = Intent(this, AkunActivity::class.java)
            // startActivity(intent)
        }

        // Card Histori
        cardHistori.setOnClickListener {
            Toast.makeText(this, "Histori", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to HistoriActivity
            // val intent = Intent(this, HistoriActivity::class.java)
            // startActivity(intent)
        }

        // Home Button (sudah di home, tidak perlu action)
        homeButton.setOnClickListener {
            Toast.makeText(this, "Anda sudah di Home", Toast.LENGTH_SHORT).show()
        }

        // Settings Button
        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to SettingsActivity
            // val intent = Intent(this, SettingsActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    // Jika tombol back ditekan 2x dalam 2 detik, keluar dari aplikasi
                    finish()
                } else {
                    // Tampilkan toast
                    Toast.makeText(this@HomeActivity, "Tekan back sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        })
    }
}