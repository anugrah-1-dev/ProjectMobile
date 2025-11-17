package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Navigasi berdasarkan status login
        navigateBasedOnAuthStatus()
    }

    private fun navigateBasedOnAuthStatus() {
        findViewById<android.view.View>(R.id.main).postDelayed({
            val currentUser = auth.currentUser

            val intent = if (currentUser != null) {
                // Jika sudah login, langsung ke Dashboard
                Intent(this, DashboardActivity::class.java)
            } else {
                // Jika belum login, ke LoginActivity
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2000)
    }
}