package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PengaturanActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Deklarasi views
    private lateinit var menuProfilContainer: RelativeLayout
    private lateinit var menuEmailContainer: RelativeLayout
    private lateinit var textEmail: TextView
    private lateinit var menuPasswordContainer: RelativeLayout
    private lateinit var buttonLogoutContainer: RelativeLayout
    private lateinit var homeButtonContainer: RelativeLayout
    private lateinit var settingButtonContainer: RelativeLayout

    companion object {
        private const val TAG = "PengaturanActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_LOGIN_MODE = "login_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "=== PengaturanActivity onCreate START ===")

        try {
            setContentView(R.layout.pengaturan)
            Log.d(TAG, "Layout 'pengaturan' set successfully")

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Cek apakah user sudah login
            if (!isUserLoggedIn()) {
                Log.d(TAG, "User tidak login, redirect ke Login")
                navigateToLogin()
                return
            }

            initializeViews()
            loadUserData()
            setupClickListeners()
            setupBackPressHandler()

            Log.d(TAG, "=== PengaturanActivity onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in onCreate", e)
            e.printStackTrace()
            Toast.makeText(this, "Error loading Pengaturan: ${e.message}", Toast.LENGTH_LONG).show()
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
            menuProfilContainer = findViewById(R.id.menu_profil_container)
            menuEmailContainer = findViewById(R.id.menu_email_container)
            textEmail = findViewById(R.id.text_email)
            menuPasswordContainer = findViewById(R.id.menu_password_container)
            buttonLogoutContainer = findViewById(R.id.button_logout_container)
            homeButtonContainer = findViewById(R.id.home_button_container)
            settingButtonContainer = findViewById(R.id.setting_button_container)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun loadUserData() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

            if (isGoogleLogin) {
                // Ambil email dari Firebase Auth
                val email = auth.currentUser?.email ?: "Tidak ada email"
                textEmail.text = email
                Log.d(TAG, "Loaded Firebase email: $email")
            } else {
                // Ambil username dari SharedPreferences untuk MySQL
                val username = prefs.getString(KEY_USERNAME, "username")
                textEmail.text = username
                Log.d(TAG, "Loaded MySQL username: $username")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data", e)
            textEmail.text = "Error loading data"
        }
    }

    private fun setupClickListeners() {
        // Click listener untuk Profil Saya - Navigasi ke AkunActivity
        menuProfilContainer.setOnClickListener {
            Log.d(TAG, "Menu Profil clicked - Navigating to AkunActivity")
            try {
                val intent = Intent(this, AkunActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening AkunActivity", e)
                Toast.makeText(this, "Error membuka profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Click listener untuk Email/Username (bisa untuk menampilkan detail)
        menuEmailContainer.setOnClickListener {
            Log.d(TAG, "Menu Email clicked")
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)
            val username = prefs.getString(KEY_USERNAME, "")
            val role = prefs.getString(KEY_USER_ROLE, "user")

            val message = if (isGoogleLogin) {
                "Email: ${auth.currentUser?.email}\nLogin: Firebase/Google\nRole: $role"
            } else {
                "Username: $username\nLogin: MySQL Database\nRole: $role"
            }

            AlertDialog.Builder(this)
                .setTitle("Informasi Akun")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }

        // Click listener untuk Ubah Kata Sandi
        menuPasswordContainer.setOnClickListener {
            Log.d(TAG, "Menu Ubah Password clicked")
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

            if (isGoogleLogin) {
                // Untuk Firebase, redirect ke ResetPasswordActivity
                try {
                    val intent = Intent(this, ResetPasswordActivity::class.java)
                    intent.putExtra("email", auth.currentUser?.email)
                    intent.putExtra("is_google", true)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening ResetPasswordActivity", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Untuk MySQL
                Toast.makeText(this, "Fitur ubah password MySQL akan segera hadir", Toast.LENGTH_SHORT).show()
                // TODO: Buat activity untuk ubah password MySQL
            }
        }

        // Click listener untuk Logout
        buttonLogoutContainer.setOnClickListener {
            Log.d(TAG, "Logout button clicked")
            showLogoutDialog()
        }

        // Bottom Navigation - Home
        homeButtonContainer.setOnClickListener {
            Log.d(TAG, "Home button clicked")
            navigateToHome()
        }

        // Bottom Navigation - Setting (sudah di halaman ini)
        settingButtonContainer.setOnClickListener {
            Log.d(TAG, "Setting button clicked (already on this page)")
            Toast.makeText(this, "Sudah di halaman Pengaturan", Toast.LENGTH_SHORT).show()
        }
    }

    // Setup OnBackPressedDispatcher (Modern way)
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back button pressed")
                navigateToHome()
            }
        })
    }

    // Fungsi untuk navigasi ke Home
    private fun navigateToHome() {
        try {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to Home", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog konfirmasi logout
    private fun showLogoutDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, "")
        val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

        val accountInfo = if (isGoogleLogin) {
            "Google/Firebase: ${auth.currentUser?.email}"
        } else {
            "MySQL: $username"
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar?\n\nAkun: $accountInfo")
            .setPositiveButton("Ya, Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // Fungsi untuk melakukan logout
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

            Log.d(TAG, "✓ Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error during logout", e)
            Toast.makeText(this, "Error logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Log.d(TAG, "✓ Navigated to LoginActivity")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error navigating to Login", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "PengaturanActivity onResume")
        // Refresh user data saat kembali ke activity ini
        loadUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PengaturanActivity destroyed")
    }
}