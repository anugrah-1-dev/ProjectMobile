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

class PengaturanDev : AppCompatActivity() {

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
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_PHONE = "user_phone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "=== PengaturanActivity onCreate START ===")

        try {
            setContentView(R.layout.pengaturan)
            Log.d(TAG, "Layout 'pengaturan' set successfully")

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
        val savedUsername = prefs.getString(KEY_USERNAME, null)

        Log.d(TAG, "Checking login status - UserID: $savedUserId, Username: $savedUsername")
        return !savedUserId.isNullOrEmpty() && !savedUsername.isNullOrEmpty()
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
            val username = prefs.getString(KEY_USERNAME, "username")
            val email = prefs.getString(KEY_USER_EMAIL, "")

            // Tampilkan email jika ada, jika tidak tampilkan username
            textEmail.text = if (!email.isNullOrEmpty()) email else username

            Log.d(TAG, "Loaded user data - Username: $username, Email: $email")
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

        // Click listener untuk Email/Username - Menampilkan detail akun
        menuEmailContainer.setOnClickListener {
            Log.d(TAG, "Menu Email clicked")
            showAccountDetails()
        }

        // Click listener untuk Ubah Kata Sandi
        menuPasswordContainer.setOnClickListener {
            Log.d(TAG, "Menu Ubah Password clicked")
            navigateToResetPassword()
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

    private fun showAccountDetails() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val userId = prefs.getString(KEY_USER_ID, "-")
            val username = prefs.getString(KEY_USERNAME, "-")
            val email = prefs.getString(KEY_USER_EMAIL, "-")
            val role = prefs.getString(KEY_USER_ROLE, "user")
            val phone = prefs.getString(KEY_USER_PHONE, "-")

            val message = """
                User ID: $userId
                Username: $username
                Email: $email
                No. Telepon: $phone
                Role: $role
                Login Type: MySQL Database
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Informasi Akun")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing account details", e)
            Toast.makeText(this, "Error menampilkan detail akun", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToResetPassword() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val intent = Intent(this, ResetPasswordActivity::class.java)

            // Kirim data user ke ResetPasswordActivity
            intent.putExtra("user_id", prefs.getString(KEY_USER_ID, ""))
            intent.putExtra("username", prefs.getString(KEY_USERNAME, ""))
            intent.putExtra("email", prefs.getString(KEY_USER_EMAIL, ""))
            intent.putExtra("login_type", "mysql")
            intent.putExtra("is_google", false)

            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening ResetPasswordActivity", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Setup OnBackPressedDispatcher
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
            val intent = Intent(this, HomeDev::class.java)
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
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val username = prefs.getString(KEY_USERNAME, "User")
            val email = prefs.getString(KEY_USER_EMAIL, "")

            val accountInfo = if (!email.isNullOrEmpty()) {
                "Username: $username\nEmail: $email"
            } else {
                "Username: $username"
            }

            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar?\n\n$accountInfo")
                .setPositiveButton("Ya, Logout") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout dialog", e)
            performLogout() // Tetap lakukan logout jika ada error
        }
    }

    // Fungsi untuk melakukan logout
    private fun performLogout() {
        try {
            Log.d(TAG, "→ Starting logout process")

            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val username = prefs.getString(KEY_USERNAME, "")

            // Clear semua data di SharedPreferences
            prefs.edit().clear().apply()
            Log.d(TAG, "✓ SharedPreferences cleared for user: $username")

            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            navigateToLogin()

            Log.d(TAG, "✓ Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error during logout", e)
            Toast.makeText(this, "Error logout: ${e.message}", Toast.LENGTH_SHORT).show()
            // Tetap navigasi ke login meski ada error
            navigateToLogin()
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

        // Cek ulang status login
        if (!isUserLoggedIn()) {
            Log.d(TAG, "User session expired, redirect to login")
            navigateToLogin()
            return
        }

        // Refresh user data saat kembali ke activity ini
        loadUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PengaturanActivity destroyed")
    }
}