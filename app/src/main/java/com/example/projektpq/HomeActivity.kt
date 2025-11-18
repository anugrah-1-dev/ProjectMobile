package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

    companion object {
        private const val TAG = "HomeActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_LOGIN_MODE = "login_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "=== HomeActivity onCreate START ===")

        try {
            setContentView(R.layout.home)
            Log.d(TAG, "Layout 'home' set successfully")

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Cek apakah user sudah login (Firebase atau MySQL)
            if (!isUserLoggedIn()) {
                Log.d(TAG, "User tidak login, redirect ke Dashboard")
                navigateToLogin()
                return
            }

            initializeViews()
            setupClickListeners()
            setupBackPressHandler()
            showWelcomeMessage()

            Log.d(TAG, "=== HomeActivity onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in onCreate", e)
            e.printStackTrace()
            Toast.makeText(this, "Error loading Home: ${e.message}", Toast.LENGTH_LONG).show()
            navigateToLogin()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getString(KEY_USER_ID, null)
        val savedLoginMode = prefs.getBoolean(KEY_LOGIN_MODE, true)

        return if (savedLoginMode) {
            // Firebase login - cek Firebase Auth
            auth.currentUser != null
        } else {
            // MySQL login - cek SharedPreferences
            savedUserId != null
        }
    }

    private fun initializeViews() {
        try {
            cardManajemenSoal = findViewById(R.id.card_manajemen_soal)
            cardManajemenMurid = findViewById(R.id.card_manajemen_murid)
            cardAkun = findViewById(R.id.card_akun)
            cardHistori = findViewById(R.id.card_histori)
            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Card Manajemen Soal
        cardManajemenSoal.setOnClickListener {
            Toast.makeText(this, "Manajemen Soal", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ManajemenSoalActivity
        }

        // Card Manajemen Murid
        cardManajemenMurid.setOnClickListener {
            Toast.makeText(this, "Manajemen Murid", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ManajemenMuridActivity
        }

        // Card Akun
        cardAkun.setOnClickListener {
            Toast.makeText(this, "Akun", Toast.LENGTH_SHORT).show()
            showAccountInfo()
        }

        // Card Histori
        cardHistori.setOnClickListener {
            Toast.makeText(this, "Histori", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to HistoriActivity
        }
    }

    private fun setupBackPressHandler() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    // Logout dan keluar
                    performLogout()
                } else {
                    Toast.makeText(
                        this@HomeActivity,
                        "Tekan back sekali lagi untuk logout",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun showWelcomeMessage() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null)
        val userRole = prefs.getString(KEY_USER_ROLE, null)
        val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

        val welcomeMessage = when {
            username != null -> "Selamat datang, $username!\nRole: $userRole"
            isGoogleLogin && auth.currentUser?.displayName != null ->
                "Selamat datang, ${auth.currentUser?.displayName}!"
            else -> "Selamat datang di TPQ Roudlotul Ilmi!"
        }

        Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show()
        Log.d(TAG, "Welcome message shown: $welcomeMessage")
    }

    private fun showAccountInfo() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, "Unknown")
        val userRole = prefs.getString(KEY_USER_ROLE, "Unknown")
        val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

        val loginType = if (isGoogleLogin) "Firebase/Google" else "MySQL"

        val message = "Account Info:\n\nUsername: $username\nRole: $userRole\nLogin Type: $loginType"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun performLogout() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

            if (isGoogleLogin) {
                // Logout Firebase
                auth.signOut()
                Log.d(TAG, "Firebase signed out")
            }

            // Clear SharedPreferences
            prefs.edit().clear().apply()
            Log.d(TAG, "SharedPreferences cleared")

            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Toast.makeText(this, "Error logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}