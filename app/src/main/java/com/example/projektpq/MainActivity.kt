package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class MainActivity : AppCompatActivity() {

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

        // Navigasi ke DashboardActivity setelah delay atau aksi tertentu
        navigateToDashboardAfterDelay()
    }

    private fun navigateToDashboardAfterDelay() {
        // Contoh: pindah ke Dashboard setelah 2 detik (seperti splash screen)
        findViewById<android.view.View>(R.id.main).postDelayed({
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish() // Tutup MainActivity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2000) // Delay 2 detik
    }
}