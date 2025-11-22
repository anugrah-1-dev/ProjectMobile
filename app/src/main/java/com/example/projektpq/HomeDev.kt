package com.example.projektpq


import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeDev : AppCompatActivity() {

    private lateinit var cardKelolaAkun: LinearLayout
    private lateinit var cardActivity: LinearLayout
    private lateinit var btnHome: LinearLayout
    private lateinit var btnSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_dev)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        cardKelolaAkun = findViewById(R.id.card_kelola_akun)
        cardActivity = findViewById(R.id.card_activity)
        btnHome = findViewById(R.id.btn_home)
        btnSettings = findViewById(R.id.btn_settings)
    }

    private fun setupClickListeners() {
        // Card Kelola Akun
        cardKelolaAkun.setOnClickListener {
            val intent = Intent(this, KelolaAkunActivity::class.java)
            startActivity(intent)
        }

        // Card Activity
        cardActivity.setOnClickListener {
            val intent = Intent(this, ActivityListActivity::class.java)
            startActivity(intent)
        }

        // Button Home
        btnHome.setOnClickListener {
            // Sudah di home, bisa refresh atau tidak melakukan apa-apa
        }

        // Button Settings
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}