package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.FirebaseDatabaseService
import com.example.projektpq.service.MySQLApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseService: FirebaseDatabaseService
    private lateinit var mysqlApiService: MySQLApiService

    // Deklarasi views
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePassword: ImageButton
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var googleSignInButton: com.google.android.gms.common.SignInButton
    private lateinit var loginModeSwitch: Switch
    private lateinit var usernameLabel: TextView
    private lateinit var dividerContainer: LinearLayout

    private var isPasswordVisible = false
    private var isGoogleLoginMode = true

    companion object {
        private const val TAG = "LoginActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_LOGIN_MODE = "login_mode"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"

        // Role constants untuk konsistensi
        private const val ROLE_SUPER_ADMIN = "SUPER ADMIN"
        private const val ROLE_ADMIN_BIASA = "admin biasa"
        private const val ROLE_USER = "user"
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let {
                Log.d(TAG, "✓ Google Sign In berhasil, memproses authentication...")
                firebaseAuthWithGoogle(it)
            } ?: run {
                Log.e(TAG, "✗ Google idToken is null")
                Toast.makeText(this, "Gagal mendapatkan credential Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            val errorMsg = when(e.statusCode) {
                10 -> "Konfigurasi Google Sign In salah. Periksa SHA-1 fingerprint."
                12501 -> "Login dibatalkan"
                12500 -> "Konfigurasi project Firebase salah"
                else -> "Error: ${e.statusCode}"
            }
            Log.e(TAG, "✗ Google sign in gagal - $errorMsg", e)
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        try {
            auth = FirebaseAuth.getInstance()
            databaseService = FirebaseDatabaseService()
            mysqlApiService = MySQLApiService()

            initializeViews()
            setupGoogleSignIn()
            setupClickListeners()
            loadLoginMode()

            Log.d(TAG, "✓ LoginActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error in onCreate", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        togglePassword = findViewById(R.id.toggle_password)
        loginButton = findViewById(R.id.login_button)
        forgotPassword = findViewById(R.id.forgot_password)
        googleSignInButton = findViewById(R.id.google_sign_in_button)
        loginModeSwitch = findViewById(R.id.login_mode_switch)
        usernameLabel = findViewById(R.id.username_label)
        dividerContainer = findViewById(R.id.divider_container)
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "✓ Google Sign In client initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error setting up Google Sign In", e)
            Toast.makeText(this, "Google Sign In tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        togglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        loginButton.setOnClickListener {
            if (isGoogleLoginMode) {
                performEmailPasswordLogin()
            } else {
                performMySQLLogin()
            }
        }

        forgotPassword.setOnClickListener {
            navigateToResetPassword()
        }

        googleSignInButton.setOnClickListener {
            Log.d(TAG, "→ Google Sign In button clicked")
            signInWithGoogle()
        }

        loginModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isGoogleLoginMode = isChecked
            updateUIForLoginMode()
            saveLoginMode(isChecked)
        }
    }

    private fun navigateToResetPassword() {
        try {
            val intent = Intent(this, ResetPasswordActivity::class.java)

            if (isGoogleLoginMode) {
                val email = usernameInput.text.toString().trim()
                intent.putExtra(ResetPasswordActivity.EXTRA_EMAIL, email)
                intent.putExtra(ResetPasswordActivity.EXTRA_IS_GOOGLE, true)
            } else {
                val username = usernameInput.text.toString().trim()
                intent.putExtra(ResetPasswordActivity.EXTRA_USERNAME, username)
                intent.putExtra(ResetPasswordActivity.EXTRA_IS_GOOGLE, false)
            }

            startActivity(intent)
            Log.d(TAG, "✓ Navigated to ResetPasswordActivity")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error navigating to ResetPassword", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLoginMode() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isGoogleLoginMode = prefs.getBoolean(KEY_LOGIN_MODE, true)
        loginModeSwitch.isChecked = isGoogleLoginMode
        updateUIForLoginMode()
    }

    private fun saveLoginMode(isGoogle: Boolean) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOGIN_MODE, isGoogle)
            .apply()
    }

    private fun saveUserSession(userId: String, username: String, role: String, isGoogle: Boolean) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_ROLE, role)
            putBoolean(KEY_LOGIN_MODE, isGoogle)
            apply()
        }
        Log.d(TAG, "✓ Session saved - User: $username, Role: $role, IsGoogle: $isGoogle")
    }

    private fun updateUIForLoginMode() {
        if (isGoogleLoginMode) {
            usernameInput.hint = "Masukkan email"
            usernameLabel.text = "Email"
            googleSignInButton.visibility = View.VISIBLE
            dividerContainer.visibility = View.VISIBLE
            forgotPassword.visibility = View.VISIBLE
            forgotPassword.text = "Lupa Password?"
            loginButton.text = "Login"
            loginModeSwitch.text = "Google/Firebase"
            usernameInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            usernameInput.hint = "Masukkan username"
            usernameLabel.text = "Username"
            googleSignInButton.visibility = View.GONE
            dividerContainer.visibility = View.GONE
            forgotPassword.visibility = View.VISIBLE
            forgotPassword.text = "Ganti Password?"
            loginButton.text = "Login dengan SQL"
            loginModeSwitch.text = "SQL Database"
            usernameInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_NORMAL
        }

        usernameInput.text?.clear()
        passwordInput.text?.clear()
        usernameInput.error = null
        passwordInput.error = null
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            togglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordInput.transformationMethod = null
            togglePassword.setImageResource(R.drawable.ic_visibility_on)
        }
        isPasswordVisible = !isPasswordVisible
        passwordInput.setSelection(passwordInput.text?.length ?: 0)
    }

    // ==================== NOTIFIKASI LOGIN BERHASIL ====================

    private fun showLoginSuccessNotification() {
        try {
            val notificationView = layoutInflater.inflate(R.layout.login_berhasil, null)

            val dialog = AlertDialog.Builder(this)
                .setView(notificationView)
                .setCancelable(false)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setGravity(Gravity.TOP)

            val params = dialog.window?.attributes
            params?.y = 700
            dialog.window?.attributes = params

            dialog.show()

            Log.d(TAG, "✓ Login success notification displayed")

            lifecycleScope.launch {
                kotlinx.coroutines.delay(3000)
                dialog.dismiss()
                navigateToHomeBasedOnRole()
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error showing success notification", e)
            navigateToHomeBasedOnRole()
        }
    }

    // ==================== FIREBASE/GOOGLE LOGIN ====================

    private fun performEmailPasswordLogin() {
        val email = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (validateInputForFirebase(email, password)) {
            loginWithEmailPassword(email, password)
        }
    }

    private fun validateInputForFirebase(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            usernameInput.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameInput.error = "Format email tidak valid"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 6) {
            passwordInput.error = "Password minimal 6 karakter"
            isValid = false
        }

        return isValid
    }

    private fun loginWithEmailPassword(email: String, password: String) {
        lifecycleScope.launch {
            try {
                setButtonLoading(loginButton, true, "Loading...")
                Log.d(TAG, "→ Attempting email/password login for: $email")

                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser

                if (user != null) {
                    Log.d(TAG, "✓ Email login successful")
                    saveUserToDatabase(user)
                    saveUserSession(user.uid, user.email ?: "", ROLE_USER, true)
                    showLoginSuccessNotification()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Login gagal: ${e.message}")

                val errorMessage = when {
                    e.message?.contains("no user record") == true ||
                            e.message?.contains("user-not-found") == true -> {
                        "Email tidak terdaftar. Silakan registrasi terlebih dahulu."
                    }
                    e.message?.contains("wrong-password") == true ||
                            e.message?.contains("invalid-credential") == true -> {
                        "Password salah. Silakan coba lagi."
                    }
                    e.message?.contains("invalid-email") == true -> {
                        "Format email tidak valid."
                    }
                    e.message?.contains("network") == true -> {
                        "Koneksi internet bermasalah."
                    }
                    else -> "Login gagal: ${e.message}"
                }

                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            } finally {
                setButtonLoading(loginButton, false, "Login")
            }
        }
    }

    private fun saveUserToDatabase(user: com.google.firebase.auth.FirebaseUser) {
        lifecycleScope.launch {
            try {
                val userData = mapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "displayName" to (user.displayName ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "createdAt" to System.currentTimeMillis()
                )

                val result = databaseService.saveUserData(user.uid, userData)
                if (result.isSuccess) {
                    Log.d(TAG, "✓ User data saved to Firebase Database")
                } else {
                    Log.e(TAG, "✗ Failed to save user data", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error saving user data", e)
            }
        }
    }

    private fun signInWithGoogle() {
        try {
            Log.d(TAG, "→ Starting Google Sign In process")

            googleSignInClient.signOut().addOnCompleteListener {
                Log.d(TAG, "✓ Previous account signed out")
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error launching Google Sign In", e)
            Toast.makeText(this, "Tidak dapat membuka Google Sign In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "→ Starting Firebase authentication with Google")

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    Log.d(TAG, "✓ Firebase auth successful")
                    saveUserToDatabase(user)
                    saveUserSession(
                        userId = user.uid,
                        username = user.email ?: user.displayName ?: "Google User",
                        role = ROLE_USER,
                        isGoogle = true
                    )
                    showLoginSuccessNotification()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Google authentication gagal", e)
                Toast.makeText(this@LoginActivity, "Authentication gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ==================== MYSQL LOGIN ====================

    private fun performMySQLLogin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (validateInputForMySQL(username, password)) {
            loginWithMySQL(username, password)
        }
    }

    private fun validateInputForMySQL(username: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            usernameInput.error = "Username tidak boleh kosong"
            isValid = false
        } else if (username.length < 3) {
            usernameInput.error = "Username minimal 3 karakter"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 4) {
            passwordInput.error = "Password minimal 4 karakter"
            isValid = false
        }

        return isValid
    }

    private fun loginWithMySQL(username: String, password: String) {
        lifecycleScope.launch {
            try {
                setButtonLoading(loginButton, true, "Loading...")
                Log.d(TAG, "→ Attempting MySQL login for: $username")

                val result = mysqlApiService.login(username, password)

                if (result.isSuccess) {
                    val response = result.getOrNull()

                    Log.d(TAG, "Response received - Success: ${response?.success}, Message: ${response?.message}")

                    if (response?.success == true && response.data != null) {
                        val userData = response.data

                        Log.d(TAG, "✓ MySQL Login successful")
                        Log.d(TAG, "  User ID: ${userData.id_user}")
                        Log.d(TAG, "  Username: ${userData.username}")
                        Log.d(TAG, "  Role: ${userData.role}")

                        saveUserSession(
                            userData.id_user.toString(),
                            userData.username,
                            userData.role,
                            false
                        )

                        showLoginSuccessNotification()
                    } else {
                        val errorMsg = response?.message ?: "Login gagal"
                        Log.e(TAG, "✗ Login failed: $errorMsg")
                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val errorMsg = "Koneksi gagal: ${error?.message}"
                    Log.e(TAG, "✗ $errorMsg", error)
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ MySQL login error", e)
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setButtonLoading(loginButton, false, "Login dengan SQL")
            }
        }
    }

    // ==================== NAVIGATION HELPER ====================

    /**
     * Navigate user to appropriate home screen based on their role
     * - SUPER ADMIN -> HomeDevActivity
     * - admin biasa -> HomeActivity
     * - user (Firebase/Google) -> HomeActivity
     */
    private fun navigateToHomeBasedOnRole() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val userRole = prefs.getString(KEY_USER_ROLE, "") ?: ""
            val username = prefs.getString(KEY_USERNAME, "") ?: ""
            val isGoogleLogin = prefs.getBoolean(KEY_LOGIN_MODE, true)

            Log.d(TAG, "→ Navigating to Home based on role")
            Log.d(TAG, "  Username: $username")
            Log.d(TAG, "  Role: $userRole")
            Log.d(TAG, "  Login Mode: ${if (isGoogleLogin) "Google/Firebase" else "MySQL"}")

            val intent = when (userRole) {
                ROLE_SUPER_ADMIN -> {
                    Log.d(TAG, "  Destination: HomeDevActivity (SUPER ADMIN)")
                    Intent(this, HomeDev::class.java)
                }
                ROLE_ADMIN_BIASA, ROLE_USER -> {
                    Log.d(TAG, "  Destination: HomeActivity (Admin Biasa/User)")
                    Intent(this, HomeActivity::class.java)
                }
                else -> {
                    Log.w(TAG, "  Unknown role: $userRole, defaulting to HomeActivity")
                    Intent(this, HomeActivity::class.java)
                }
            }

            intent.apply {
                putExtra("USER_ROLE", userRole)
                putExtra("USERNAME", username)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(intent)
            finish()

            Log.d(TAG, "✓ Navigation completed")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error during navigation", e)
            Toast.makeText(this, "Error navigating: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== HELPER METHODS ====================

    private fun setButtonLoading(button: Button, isLoading: Boolean, text: String) {
        button.isEnabled = !isLoading
        button.text = text
    }

    override fun onStart() {
        super.onStart()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getString(KEY_USER_ID, null)
        val savedLoginMode = prefs.getBoolean(KEY_LOGIN_MODE, true)

        if (savedUserId != null) {
            if (savedLoginMode) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    Log.d(TAG, "✓ User sudah login dengan Firebase, redirect ke Home")
                    navigateToHomeBasedOnRole()
                    return
                } else {
                    Log.d(TAG, "✗ Firebase session expired")
                    prefs.edit().clear().apply()
                }
            } else {
                Log.d(TAG, "✓ User sudah login dengan MySQL, redirect ke Home")
                navigateToHomeBasedOnRole()
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LoginActivity destroyed")
    }
}