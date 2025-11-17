package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        startButton = findViewById(R.id.start_button)
    }

    private fun setupClickListeners() {
        startButton.setOnClickListener {
            navigateToLoginActivity()
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        // Optional: finish() jika ingin menutup DashboardActivity setelah pindah
        // finish()
    }
}