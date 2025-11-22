package com.example.projektpq

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.MySQLApiService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mysqlApiService: MySQLApiService

    // Views
    private lateinit var identifierInput: EditText
    private lateinit var identifierLabel: TextView
    private lateinit var currentPasswordInput: EditText
    private lateinit var currentPasswordLabel: TextView
    private lateinit var currentPasswordContainer: RelativeLayout
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var toggleCurrentPassword: ImageButton
    private lateinit var toggleNewPassword: ImageButton
    private lateinit var toggleConfirmPassword: ImageButton
    private lateinit var resetPasswordButton: Button
    private lateinit var resetModeSwitch: Switch
    private lateinit var infoText: TextView

    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isGoogleMode = true

    companion object {
        private const val TAG = "ResetPasswordActivity"
        const val EXTRA_EMAIL = "user_email"
        const val EXTRA_USERNAME = "username"
        const val EXTRA_IS_GOOGLE = "is_google"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password)

        try {
            auth = FirebaseAuth.getInstance()
            mysqlApiService = MySQLApiService()

            initializeViews()
            loadIntentData()
            setupClickListeners()
            setupBackPressHandler()
            updateUIForMode()

            Log.d(TAG, "✓ ResetPasswordActivity initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this) {
            // Handle back press
            finish()
        }
    }

    private fun initializeViews() {
        identifierInput = findViewById(R.id.identifier_input)
        identifierLabel = findViewById(R.id.identifier_label)
        currentPasswordInput = findViewById(R.id.current_password_input)
        currentPasswordLabel = findViewById(R.id.current_password_label)
        currentPasswordContainer = findViewById(R.id.current_password_container)
        newPasswordInput = findViewById(R.id.new_password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        toggleCurrentPassword = findViewById(R.id.toggle_current_password)
        toggleNewPassword = findViewById(R.id.toggle_new_password)
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password)
        resetPasswordButton = findViewById(R.id.reset_password_button)
        resetModeSwitch = findViewById(R.id.reset_mode_switch)
        infoText = findViewById(R.id.info_text)
    }

    private fun loadIntentData() {
        val email = intent.getStringExtra(EXTRA_EMAIL)
        val username = intent.getStringExtra(EXTRA_USERNAME)
        isGoogleMode = intent.getBooleanExtra(EXTRA_IS_GOOGLE, true)

        resetModeSwitch.isChecked = isGoogleMode

        if (isGoogleMode && !email.isNullOrEmpty()) {
            identifierInput.setText(email)
        } else if (!isGoogleMode && !username.isNullOrEmpty()) {
            identifierInput.setText(username)
        }
    }

    private fun setupClickListeners() {
        toggleCurrentPassword.setOnClickListener {
            togglePasswordVisibility(
                currentPasswordInput,
                toggleCurrentPassword,
                isCurrentPasswordVisible
            ) { isCurrentPasswordVisible = it }
        }

        toggleNewPassword.setOnClickListener {
            togglePasswordVisibility(
                newPasswordInput,
                toggleNewPassword,
                isNewPasswordVisible
            ) { isNewPasswordVisible = it }
        }

        toggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(
                confirmPasswordInput,
                toggleConfirmPassword,
                isConfirmPasswordVisible
            ) { isConfirmPasswordVisible = it }
        }

        resetModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isGoogleMode = isChecked
            updateUIForMode()
            clearInputs()
        }

        resetPasswordButton.setOnClickListener {
            performPasswordReset()
        }
    }

    private fun updateUIForMode() {
        if (isGoogleMode) {
            // Mode Google/Firebase
            identifierLabel.text = "Email:"
            identifierInput.hint = "Masukkan email"
            identifierInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            currentPasswordLabel.visibility = View.GONE
            currentPasswordContainer.visibility = View.GONE

            resetModeSwitch.text = "Google/Firebase"
            resetPasswordButton.text = "Kirim Email Reset"

            infoText.text = "Email reset password akan dikirim ke alamat yang Anda masukkan"
            infoText.visibility = View.VISIBLE
        } else {
            // Mode SQL
            identifierLabel.text = "Username:"
            identifierInput.hint = "Masukkan username"
            identifierInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_NORMAL

            currentPasswordLabel.visibility = View.VISIBLE
            currentPasswordContainer.visibility = View.VISIBLE

            resetModeSwitch.text = "SQL Database"
            resetPasswordButton.text = "Ubah Password"

            infoText.text = "Password akan diubah langsung di database SQL"
            infoText.visibility = View.VISIBLE
        }
    }

    private fun clearInputs() {
        identifierInput.text?.clear()
        currentPasswordInput.text?.clear()
        newPasswordInput.text?.clear()
        confirmPasswordInput.text?.clear()

        identifierInput.error = null
        currentPasswordInput.error = null
        newPasswordInput.error = null
        confirmPasswordInput.error = null
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        button: ImageButton,
        isVisible: Boolean,
        updateVisibility: (Boolean) -> Unit
    ) {
        if (isVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility_off)
            updateVisibility(false)
        } else {
            editText.transformationMethod = null
            button.setImageResource(R.drawable.ic_visibility_on)
            updateVisibility(true)
        }
        editText.setSelection(editText.text?.length ?: 0)
    }

    private fun performPasswordReset() {
        if (isGoogleMode) {
            performGooglePasswordReset()
        } else {
            performSQLPasswordReset()
        }
    }

    // ==================== GOOGLE/FIREBASE RESET ====================

    private fun performGooglePasswordReset() {
        val email = identifierInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (!validateEmailInput(email)) return
        if (!validatePasswords(newPassword, confirmPassword)) return

        lifecycleScope.launch {
            try {
                setButtonLoading(true)
                Log.d(TAG, "→ Starting Google password reset for: $email")

                val currentUser = auth.currentUser

                if (currentUser != null && currentUser.email == email) {
                    // User sedang login, update password langsung
                    currentUser.updatePassword(newPassword).await()

                    Log.d(TAG, "✓ Password updated successfully")
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Password berhasil diubah!",
                        Toast.LENGTH_SHORT
                    ).show()

                    kotlinx.coroutines.delay(500)
                    finish()
                } else {
                    // User tidak login, kirim email reset
                    auth.sendPasswordResetEmail(email).await()

                    Log.d(TAG, "✓ Password reset email sent to: $email")
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Email reset password telah dikirim ke $email. Silakan cek inbox Anda.",
                        Toast.LENGTH_LONG
                    ).show()

                    kotlinx.coroutines.delay(1500)
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Google password reset failed", e)
                handleGoogleResetError(e)
            } finally {
                setButtonLoading(false)
            }
        }
    }

    private fun validateEmailInput(email: String): Boolean {
        if (email.isEmpty()) {
            identifierInput.error = "Email tidak boleh kosong"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            identifierInput.error = "Format email tidak valid"
            return false
        }

        return true
    }

    private fun handleGoogleResetError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("requires-recent-login") == true -> {
                "Sesi Anda sudah kadaluarsa. Silakan login ulang."
            }
            e.message?.contains("user-not-found") == true -> {
                "Email tidak terdaftar"
            }
            e.message?.contains("invalid-email") == true -> {
                "Format email tidak valid"
            }
            e.message?.contains("weak-password") == true -> {
                "Password terlalu lemah"
            }
            e.message?.contains("network") == true -> {
                "Koneksi internet bermasalah"
            }
            else -> "Gagal reset password: ${e.message}"
        }

        Toast.makeText(
            this@ResetPasswordActivity,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    // ==================== SQL RESET ====================

    private fun performSQLPasswordReset() {
        val username = identifierInput.text.toString().trim()
        val currentPassword = currentPasswordInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (!validateSQLInput(username, currentPassword)) return
        if (!validatePasswords(newPassword, confirmPassword)) return

        lifecycleScope.launch {
            try {
                setButtonLoading(true)
                Log.d(TAG, "→ Starting SQL password reset for: $username")

                val result = mysqlApiService.changePassword(username, currentPassword, newPassword)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response?.success == true) {
                        Log.d(TAG, "✓ SQL password changed successfully")
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            response.message ?: "Password berhasil diubah!",
                            Toast.LENGTH_SHORT
                        ).show()

                        kotlinx.coroutines.delay(500)
                        finish()
                    } else {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            response?.message ?: "Gagal mengubah password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "✗ SQL password change failed", error)
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Koneksi gagal: ${error?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ SQL password reset error", e)
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setButtonLoading(false)
            }
        }
    }

    private fun validateSQLInput(username: String, currentPassword: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            identifierInput.error = "Username tidak boleh kosong"
            isValid = false
        } else if (username.length < 3) {
            identifierInput.error = "Username minimal 3 karakter"
            isValid = false
        }

        if (currentPassword.isEmpty()) {
            currentPasswordInput.error = "Password lama tidak boleh kosong"
            isValid = false
        } else if (currentPassword.length < 4) {
            currentPasswordInput.error = "Password minimal 4 karakter"
            isValid = false
        }

        return isValid
    }

    // ==================== COMMON VALIDATION ====================

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        var isValid = true

        // Validasi password baru
        when {
            newPassword.isEmpty() -> {
                newPasswordInput.error = "Password baru tidak boleh kosong"
                isValid = false
            }
            newPassword.length < 6 -> {
                newPasswordInput.error = "Password minimal 6 karakter"
                isValid = false
            }
            !newPassword.matches(Regex(".*[A-Za-z].*")) -> {
                newPasswordInput.error = "Password harus mengandung huruf"
                isValid = false
            }
            !newPassword.matches(Regex(".*[0-9].*")) -> {
                newPasswordInput.error = "Password harus mengandung angka"
                isValid = false
            }
        }

        // Validasi konfirmasi password
        when {
            confirmPassword.isEmpty() -> {
                confirmPasswordInput.error = "Konfirmasi password tidak boleh kosong"
                isValid = false
            }
            newPassword != confirmPassword -> {
                confirmPasswordInput.error = "Password tidak cocok"
                isValid = false
            }
        }

        return isValid
    }

    // ==================== HELPER METHODS ====================

    private fun setButtonLoading(isLoading: Boolean) {
        resetPasswordButton.isEnabled = !isLoading
        resetPasswordButton.text = if (isLoading) "Loading..." else {
            if (isGoogleMode) "Kirim Email Reset" else "Ubah Password"
        }

        identifierInput.isEnabled = !isLoading
        currentPasswordInput.isEnabled = !isLoading
        newPasswordInput.isEnabled = !isLoading
        confirmPasswordInput.isEnabled = !isLoading
        resetModeSwitch.isEnabled = !isLoading
    }
}