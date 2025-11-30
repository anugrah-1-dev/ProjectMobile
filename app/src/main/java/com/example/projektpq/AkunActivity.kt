package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.MySQLApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AkunActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mysqlApiService: MySQLApiService

    // View elements
    private lateinit var backIcon: ImageView
    private lateinit var namaText: TextView
    private lateinit var emailText: TextView
    private lateinit var teleponText: TextView
    private lateinit var editButton: Button
    private lateinit var homeIcon: ImageView
    private lateinit var settingsIcon: ImageView

    companion object {
        private const val TAG = "AkunActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_LOGIN_MODE = "login_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_akun)

        Log.d(TAG, "=== AkunActivity onCreate START ===")

        try {
            auth = FirebaseAuth.getInstance()
            mysqlApiService = MySQLApiService()

            initializeViews()
            setupClickListeners()
            loadUserProfile()

            Log.d(TAG, "=== AkunActivity onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in onCreate", e)
            Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        backIcon = findViewById(R.id.back_icon)
        namaText = findViewById(R.id.nama_text)
        emailText = findViewById(R.id.email_text)
        teleponText = findViewById(R.id.telepon_text)
        editButton = findViewById(R.id.edit_button)
        homeIcon = findViewById(R.id.home_icon)
        settingsIcon = findViewById(R.id.settings_icon)

        Log.d(TAG, "All views initialized successfully")
    }

    private fun setupClickListeners() {
        backIcon.setOnClickListener {
            finish()
        }

        editButton.setOnClickListener {
            showEditProfileDialog()
        }

        homeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        settingsIcon.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

        Log.d(TAG, "Loading profile - isGoogleLogin: $isGoogleLogin")

        if (isGoogleLogin) {
            loadFirebaseProfile()
        } else {
            val userId = prefs.getString(KEY_USER_ID, null)
            Log.d(TAG, "User ID from prefs: $userId")

            if (userId != null) {
                loadMySQLProfile(userId.toInt())
            } else {
                Log.e(TAG, "User ID not found in SharedPreferences")
                Toast.makeText(this, "Error: User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFirebaseProfile() {
        val user = auth.currentUser
        if (user != null) {
            namaText.text = user.displayName ?: "Tidak ada nama"
            emailText.text = user.email ?: "Tidak ada email"
            teleponText.text = user.phoneNumber ?: "Tidak ada telepon"

            Log.d(TAG, "✓ Firebase profile loaded successfully")
        } else {
            Log.e(TAG, "✗ Firebase user is null")
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMySQLProfile(userId: Int) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "→ Loading MySQL profile for user ID: $userId")

                val result = mysqlApiService.getUserProfile(userId)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    Log.d(TAG, "Response received - success: ${response?.success}, message: ${response?.message}")

                    if (response?.success == true && response.data != null) {
                        val userData = response.data

                        namaText.text = userData.username
                        emailText.text = if (userData.email.isNullOrEmpty()) "Belum diisi" else userData.email
                        teleponText.text = if (userData.nomor_telepon.isNullOrEmpty()) "Belum diisi" else userData.nomor_telepon

                        Log.d(TAG, "✓ Profile loaded successfully")
                        Log.d(TAG, "  - Username: ${userData.username}")
                        Log.d(TAG, "  - Email: ${userData.email}")
                        Log.d(TAG, "  - Phone: ${userData.nomor_telepon}")
                        Log.d(TAG, "  - Role: ${userData.role}")
                    } else {
                        Log.e(TAG, "✗ Failed to load profile: ${response?.message}")
                        Toast.makeText(this@AkunActivity, response?.message ?: "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "✗ Error loading profile", error)
                    Toast.makeText(this@AkunActivity, "Koneksi gagal: ${error?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Exception loading profile", e)
                Toast.makeText(this@AkunActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

        if (isGoogleLogin) {
            Toast.makeText(this, "Edit profil untuk akun Google tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val usernameInput = dialogView.findViewById<EditText>(R.id.edit_username)
        val emailInput = dialogView.findViewById<EditText>(R.id.edit_email)
        val phoneInput = dialogView.findViewById<EditText>(R.id.edit_phone)

        // Set current values
        usernameInput.setText(namaText.text.toString())
        emailInput.setText(if (emailText.text.toString() == "Belum diisi") "" else emailText.text.toString())
        phoneInput.setText(if (teleponText.text.toString() == "Belum diisi") "" else teleponText.text.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newUsername = usernameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()
                val newPhone = phoneInput.text.toString().trim()

                if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newUsername.length < 3) {
                    Toast.makeText(this, "Username minimal 3 karakter", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateProfile(newUsername, newEmail, newPhone)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateProfile(username: String, email: String, phone: String) {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val userId = prefs.getString(KEY_USER_ID, null)?.toInt()

                if (userId == null) {
                    Toast.makeText(this@AkunActivity, "Error: User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                editButton.isEnabled = false
                editButton.text = "Menyimpan..."

                Log.d(TAG, "→ Updating profile for user ID: $userId")
                Log.d(TAG, "  - New username: $username")
                Log.d(TAG, "  - New email: $email")
                Log.d(TAG, "  - New phone: $phone")

                val result = mysqlApiService.updateUserProfile(userId, username, email, phone)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    Log.d(TAG, "Update response - success: ${response?.success}, message: ${response?.message}")

                    if (response?.success == true && response.data != null) {
                        val userData = response.data

                        // Update UI
                        namaText.text = userData.username
                        emailText.text = if (userData.email.isNullOrEmpty()) "Belum diisi" else userData.email
                        teleponText.text = if (userData.nomor_telepon.isNullOrEmpty()) "Belum diisi" else userData.nomor_telepon

                        // Update SharedPreferences
                        prefs.edit().putString(KEY_USERNAME, userData.username).apply()

                        Toast.makeText(this@AkunActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "✓ Profile updated successfully")
                    } else {
                        Toast.makeText(this@AkunActivity, response?.message ?: "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "✗ Update failed: ${response?.message}")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Toast.makeText(this@AkunActivity, "Koneksi gagal: ${error?.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "✗ Update profile failed", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Exception updating profile", e)
                Toast.makeText(this@AkunActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                editButton.isEnabled = true
                editButton.text = "Edit"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AkunActivity destroyed")
    }
}