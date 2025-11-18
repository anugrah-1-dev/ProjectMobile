package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var welcomeText: TextView

    companion object {
        private const val TAG = "DashboardActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_dashboard)
            Log.d(TAG, "Layout dashboard berhasil dimuat")

            initializeViews()
            checkIfSuperAdmin()
            setupClickListeners()
            setupBackPressHandler() // ✅ Tambahkan ini
        } catch (e: Exception) {
            Log.e(TAG, "Error di onCreate: ${e.message}", e)
            e.printStackTrace()
            navigateToLoginActivity()
        }
    }

    private fun initializeViews() {
        try {
            startButton = findViewById(R.id.start_button)
            welcomeText = findViewById(R.id.welcome_tpq)
            Log.d(TAG, "Views berhasil diinisialisasi")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat inisialisasi views: ${e.message}", e)
            throw e
        }
    }

    private fun checkIfSuperAdmin() {
        try {
            // Cek apakah ini Super Admin yang sudah login
            val userRole = intent.getStringExtra("USER_ROLE")
            val fromMain = intent.getBooleanExtra("FROM_MAIN", false)

            if (userRole == "SUPER ADMIN" && fromMain) {
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val username = prefs.getString(KEY_USERNAME, "") ?: ""

                Log.d(TAG, "Super Admin sudah login: $username")

                // Ubah tampilan untuk Super Admin
                welcomeText.text = "DASHBOARD\nSUPER ADMIN\n\nWelcome,\n$username"
                startButton.text = "MANAGE SYSTEM"

                Toast.makeText(
                    this,
                    "Welcome Super Admin: $username",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Super Admin: ${e.message}", e)
        }
    }

    private fun setupClickListeners() {
        try {
            startButton.setOnClickListener {
                Log.d(TAG, "Start button diklik")

                // Cek apakah Super Admin
                val userRole = intent.getStringExtra("USER_ROLE")

                if (userRole == "SUPER ADMIN") {
                    // Super Admin stays in dashboard (add admin features here)
                    Toast.makeText(
                        this,
                        "Super Admin Dashboard - Features coming soon",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Regular user -> go to Login
                    navigateToLoginActivity()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat setup click listeners: ${e.message}", e)
        }
    }

    // ✅ CARA BARU - Ganti onBackPressed() dengan ini
    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Cek apakah Super Admin
                val userRole = intent.getStringExtra("USER_ROLE")

                if (userRole == "SUPER ADMIN") {
                    // Super Admin: back = logout
                    Toast.makeText(
                        this@DashboardActivity,
                        "Logging out...",
                        Toast.LENGTH_SHORT
                    ).show()

                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Regular: close app
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun navigateToLoginActivity() {
        try {
            Log.d(TAG, "Navigasi ke LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Tidak finish() agar bisa kembali dengan tombol back
        } catch (e: Exception) {
            Log.e(TAG, "Error saat navigasi: ${e.message}", e)
            e.printStackTrace()
        }
    }
}