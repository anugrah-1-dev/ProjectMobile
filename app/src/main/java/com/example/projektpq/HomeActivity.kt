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

    // Deklarasi views untuk cards
    private lateinit var cardManajemenSoal: LinearLayout
    private lateinit var cardManajemenMurid: LinearLayout
    private lateinit var cardAkun: LinearLayout
    private lateinit var cardHistori: LinearLayout

    // Deklarasi views untuk bottom navigation (DISESUAIKAN dengan ID di XML)
    private lateinit var btnHome: LinearLayout
    private lateinit var btnSettings: LinearLayout

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
                Log.d(TAG, "User tidak login, redirect ke Login")
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
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HomeActivity onResume - User returned to home screen")
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
            Log.d(TAG, "→ Initializing views")

            // Initialize card views
            cardManajemenSoal = findViewById(R.id.card_manajemen_soal)
            Log.d(TAG, "✓ card_manajemen_soal found")

            cardManajemenMurid = findViewById(R.id.card_manajemen_murid)
            Log.d(TAG, "✓ card_manajemen_murid found")

            cardAkun = findViewById(R.id.card_akun)
            Log.d(TAG, "✓ card_akun found")

            cardHistori = findViewById(R.id.card_histori)
            Log.d(TAG, "✓ card_histori found")

            // Initialize bottom navigation views (DISESUAIKAN dengan ID di XML)
            btnHome = findViewById(R.id.btn_home)
            Log.d(TAG, "✓ btn_home found")

            btnSettings = findViewById(R.id.btn_settings)
            Log.d(TAG, "✓ btn_settings found")

            Log.d(TAG, "✓ All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "→ Setting up click listeners")

        // Card Manajemen Soal
        cardManajemenSoal.setOnClickListener {
            Log.d(TAG, "Card Manajemen Soal clicked")
            Toast.makeText(this, "Manajemen Soal", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to ManajemenSoalActivity
        }

        // Card Manajemen Murid
        cardManajemenMurid.setOnClickListener {
            Log.d(TAG, "Card Manajemen Murid clicked")
            try {
                val intent = Intent(this, ManajemenMuridActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "✓ Navigating to ManajemenMuridActivity")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error opening ManajemenMuridActivity", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Card Akun - Navigate to AkunActivity (BUKAN PengaturanActivity)
        cardAkun.setOnClickListener {
            Log.d(TAG, "Card Akun clicked")
            navigateToAkun()
        }

        // Card Histori
        cardHistori.setOnClickListener {
            Log.d(TAG, "Card Histori clicked")
            Toast.makeText(this, "Histori", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to HistoriActivity
        }

        // Bottom Navigation - Home (sudah di halaman ini)
        btnHome.setOnClickListener {
            Log.d(TAG, "Home button clicked (already on this page)")
            Toast.makeText(this, "Sudah di halaman Home", Toast.LENGTH_SHORT).show()
        }

        // Bottom Navigation - Settings -> Navigate to PengaturanActivity
        btnSettings.setOnClickListener {
            Log.d(TAG, "Settings button clicked from navbar")
            navigateToPengaturan()
        }

        Log.d(TAG, "✓ Click listeners setup completed")
    }

    // Fungsi untuk navigasi ke AkunActivity
    private fun navigateToAkun() {
        try {
            Log.d(TAG, "→ Navigating to AkunActivity")
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✓ Successfully started AkunActivity")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error opening AkunActivity", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk navigasi ke PengaturanActivity
    private fun navigateToPengaturan() {
        try {
            Log.d(TAG, "→ Navigating to PengaturanActivity")
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "✓ Successfully started PengaturanActivity")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error opening PengaturanActivity", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        Log.d(TAG, "✓ Back press handler setup completed")
    }

    private fun showWelcomeMessage() {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error showing welcome message", e)
        }
    }

    private fun performLogout() {
        try {
            Log.d(TAG, "→ Starting logout process")
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

            if (isGoogleLogin) {
                // Logout Firebase
                auth.signOut()
                Log.d(TAG, "✓ Firebase signed out")
            }

            // Clear SharedPreferences
            prefs.edit().clear().apply()
            Log.d(TAG, "✓ SharedPreferences cleared")

            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error during logout", e)
            Toast.makeText(this, "Error logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Log.d(TAG, "✓ Navigated to MainActivity")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error navigating to MainActivity", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "HomeActivity destroyed")
    }
}