package com.example.projektpq

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class HomeDev : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // View elements
    private lateinit var cardKelolaAkun: LinearLayout
    private lateinit var cardActivity: LinearLayout
    private lateinit var btnHome: LinearLayout
    private lateinit var btnSettings: LinearLayout
    private lateinit var tpqTitle: TextView

    companion object {
        private const val TAG = "HomeDev"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_LOGIN_MODE = "login_mode"
        private const val ROLE_SUPER_ADMIN = "SUPER ADMIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_dev)

        Log.d(TAG, "=== HomeDev onCreate START ===")

        try {
            auth = FirebaseAuth.getInstance()

            initializeViews()
            loadUserInfo()
            setupClickListeners()
            verifyUserAccess()

            Log.d(TAG, "=== HomeDev onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        try {
            // Cards yang ada di layout
            cardKelolaAkun = findViewById(R.id.card_kelola_akun)
            cardActivity = findViewById(R.id.card_activity)

            // Bottom navigation
            btnHome = findViewById(R.id.btn_home)
            btnSettings = findViewById(R.id.btn_settings)

            // TPQ Title - gunakan untuk menampilkan info user
            tpqTitle = findViewById(R.id.tpq_roudlot)

            Log.d(TAG, "✓ All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing views", e)
            throw e
        }
    }

    private fun loadUserInfo() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val username = prefs.getString(KEY_USERNAME, "Super Admin") ?: "Super Admin"
            val userRole = prefs.getString(KEY_USER_ROLE, ROLE_SUPER_ADMIN) ?: ROLE_SUPER_ADMIN
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, false)

            Log.d(TAG, "User Info:")
            Log.d(TAG, "  - Username: $username")
            Log.d(TAG, "  - Role: $userRole")
            Log.d(TAG, "  - Login Mode: ${if (isGoogleLogin) "Google/Firebase" else "MySQL"}")

            // Update TPQ title dengan info user
            tpqTitle.text = "TPQ ROUDLOTUL ILMI\n$userRole"

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user info", e)
        }
    }

    private fun verifyUserAccess() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userRole = prefs.getString(KEY_USER_ROLE, "") ?: ""

        if (userRole != ROLE_SUPER_ADMIN) {
            Log.e(TAG, "❌ UNAUTHORIZED ACCESS - Role: $userRole")
            Toast.makeText(this, "Akses ditolak! Hanya untuk Super Admin.", Toast.LENGTH_LONG).show()

            // Redirect ke HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Log.d(TAG, "✓ Access verified - SUPER ADMIN")
        }
    }

    private fun setupClickListeners() {
        // Card Kelola Akun - Mengelola profil akun Super Admin
        cardKelolaAkun.setOnClickListener {
            Log.d(TAG, "→ Navigating to AkunActivity")
            try {
                val intent = Intent(this, AkunActivity::class.java)
                intent.putExtra("IS_SUPER_ADMIN", true)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to AkunActivity", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Card Activity - Melihat histori aktivitas
        cardActivity.setOnClickListener {
            Log.d(TAG, "→ Navigating to HistoriActivity")
            try {
                val intent = Intent(this, HistoriActivity::class.java)
                intent.putExtra("IS_SUPER_ADMIN", true)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "HistoriActivity not found", e)
                Toast.makeText(this, "Fitur Histori - Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol Home - Refresh atau stay
        btnHome.setOnClickListener {
            Log.d(TAG, "Home button clicked")
            Toast.makeText(this, "Super Admin - Halaman Utama", Toast.LENGTH_SHORT).show()
        }

        // Tombol Settings - Buka menu pengaturan atau logout
        btnSettings.setOnClickListener {
            Log.d(TAG, "Settings button clicked")
            showSettingsMenu()
        }
    }

    private fun showSettingsMenu() {
        val options = arrayOf("Pengaturan", "Logout")

        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Pengaturan
                        try {
                            val intent = Intent(this, PengaturanActivity::class.java)
                            intent.putExtra("IS_SUPER_ADMIN", true)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "PengaturanActivity not found", e)
                            Toast.makeText(this, "Pengaturan - Coming Soon", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        // Logout
                        showLogoutConfirmation()
                    }
                }
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun performLogout() {
        try {
            Log.d(TAG, "→ Performing logout")

            // Clear SharedPreferences
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, false)

            prefs.edit().clear().apply()

            // Sign out dari Firebase jika login dengan Google
            if (isGoogleLogin) {
                auth.signOut()
                Log.d(TAG, "✓ Firebase sign out successful")
            }

            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Log.d(TAG, "✓ Logout completed")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error during logout", e)
            Toast.makeText(this, "Error logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Tampilkan konfirmasi sebelum keluar dari app
        AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Ya") { _, _ ->
                super.onBackPressed()
                finishAffinity() // Tutup semua activity
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HomeDev onResume")

        // Refresh user info saat kembali ke halaman ini
        loadUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "HomeDev destroyed")
    }
}