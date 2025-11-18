package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "MainActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LOGIN_MODE = "login_mode"
        private const val KEY_USER_ROLE = "user_role"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
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
            Log.d(TAG, "Firebase Auth berhasil diinisialisasi")

            // Navigasi berdasarkan status login
            navigateBasedOnAuthStatus()

        } catch (e: Exception) {
            Log.e(TAG, "Error di onCreate: ${e.message}", e)
            e.printStackTrace()
            // Jika error, langsung ke DashboardActivity (Welcome Screen)
            navigateToDashboard()
        }
    }

    private fun navigateBasedOnAuthStatus() {
        try {
            findViewById<android.view.View>(R.id.main).postDelayed({
                try {
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val savedUserId = prefs.getString(KEY_USER_ID, null)
                    val savedLoginMode = prefs.getBoolean(KEY_LOGIN_MODE, true)
                    val savedUserRole = prefs.getString(KEY_USER_ROLE, "")

                    Log.d(TAG, "Saved User ID: $savedUserId")
                    Log.d(TAG, "Saved Login Mode: $savedLoginMode")
                    Log.d(TAG, "Saved User Role: $savedUserRole")

                    val intent = when {
                        // Jika sudah ada session yang tersimpan
                        savedUserId != null -> {
                            if (savedLoginMode) {
                                // Firebase/Google login - cek apakah masih valid
                                val currentUser = auth.currentUser
                                if (currentUser != null) {
                                    Log.d(TAG, "User Firebase masih login, langsung ke Home")
                                    Intent(this, HomeActivity::class.java)
                                } else {
                                    Log.d(TAG, "Session Firebase expired, ke Dashboard")
                                    prefs.edit().clear().apply()
                                    Intent(this, DashboardActivity::class.java)
                                }
                            } else {
                                // MySQL login - langsung ke Home
                                Log.d(TAG, "User MySQL sudah login, langsung ke Home")

                                // Navigate berdasarkan role
                                when (savedUserRole) {
                                    "SUPER ADMIN" -> Intent(this, DashboardActivity::class.java).apply {
                                        putExtra("USER_ROLE", savedUserRole)
                                        putExtra("FROM_MAIN", true)
                                    }
                                    else -> Intent(this, HomeActivity::class.java)
                                }
                            }
                        }
                        // Jika belum login sama sekali
                        else -> {
                            Log.d(TAG, "Belum ada session, ke Dashboard welcome screen")
                            Intent(this, DashboardActivity::class.java)
                        }
                    }

                    startActivity(intent)
                    finish()
                    applyTransition()

                } catch (e: Exception) {
                    Log.e(TAG, "Error saat navigasi: ${e.message}", e)
                    navigateToDashboard()
                }
            }, 1500) // Delay 1.5 detik untuk splash screen
        } catch (e: Exception) {
            Log.e(TAG, "Error di navigateBasedOnAuthStatus: ${e.message}", e)
            navigateToDashboard()
        }
    }

    private fun applyTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun navigateToDashboard() {
        try {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigasi ke Dashboard: ${e.message}", e)
        }
    }
}