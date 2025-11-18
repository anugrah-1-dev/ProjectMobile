package com.example.projektpq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.FirebaseDatabaseService
import com.example.projektpq.service.MySQLApiService
import com.example.projektpq.HomeActivity
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
    private lateinit var registerButton: Button
    private lateinit var usernameLabel: TextView
    private lateinit var dividerContainer: LinearLayout

    private var isPasswordVisible = false
    private var isGoogleLoginMode = true // true = Google/Firebase, false = MySQL

    companion object {
        private const val TAG = "LoginActivity"
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_LOGIN_MODE = "login_mode"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
    }

    // Modern way untuk handle Google Sign In result
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let {
                Log.d(TAG, "Google Sign In berhasil, idToken obtained")
                firebaseAuthWithGoogle(it)
            } ?: run {
                Log.e(TAG, "Google idToken is null")
                Toast.makeText(this, "Gagal mendapatkan credential", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in gagal - Status Code: ${e.statusCode}, Message: ${e.message}", e)
            Toast.makeText(this, "Google sign in gagal: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        try {
            // Initialize services
            auth = FirebaseAuth.getInstance()
            databaseService = FirebaseDatabaseService()
            mysqlApiService = MySQLApiService()

            initializeViews()
            setupGoogleSignIn()
            setupClickListeners()

            // Load saved login mode
            loadLoginMode()

            Log.d(TAG, "LoginActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        try {
            usernameInput = findViewById(R.id.username_input)
            passwordInput = findViewById(R.id.password_input)
            togglePassword = findViewById(R.id.toggle_password)
            loginButton = findViewById(R.id.login_button)
            forgotPassword = findViewById(R.id.forgot_password)
            googleSignInButton = findViewById(R.id.google_sign_in_button)
            loginModeSwitch = findViewById(R.id.login_mode_switch)
            registerButton = findViewById(R.id.register_button)
            usernameLabel = findViewById(R.id.username_label)
            dividerContainer = findViewById(R.id.divider_container)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Error: Ada komponen yang tidak ditemukan di layout", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "Google Sign In client initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Google Sign In", e)
            Toast.makeText(this, "Google Sign In tidak tersedia: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        // Toggle password visibility
        togglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login button - pilih mode
        loginButton.setOnClickListener {
            if (isGoogleLoginMode) {
                performEmailPasswordLogin()
            } else {
                performMySQLLogin()
            }
        }

        // Register button untuk MySQL
        registerButton.setOnClickListener {
            if (!isGoogleLoginMode) {
                performMySQLRegister()
            } else {
                Toast.makeText(this, "Registrasi hanya untuk mode SQL", Toast.LENGTH_SHORT).show()
            }
        }

        // Forgot password
        forgotPassword.setOnClickListener {
            if (isGoogleLoginMode) {
                showForgotPasswordDialog()
            } else {
                Toast.makeText(this, "Reset password untuk SQL belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        // Google sign in
        googleSignInButton.setOnClickListener {
            Log.d(TAG, "Google Sign In button clicked")
            signInWithGoogle()
        }

        // Switch mode login
        loginModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isGoogleLoginMode = isChecked
            updateUIForLoginMode()
            saveLoginMode(isChecked)
        }
    }

    private fun loadLoginMode() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isGoogleLoginMode = prefs.getBoolean(KEY_LOGIN_MODE, true)

        loginModeSwitch.isChecked = isGoogleLoginMode
        updateUIForLoginMode()
    }

    private fun saveLoginMode(isGoogle: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LOGIN_MODE, isGoogle).apply()
    }

    private fun saveUserSession(userId: String, username: String, role: String, isGoogle: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_ROLE, role)
            putBoolean(KEY_LOGIN_MODE, isGoogle)
            apply()
        }

        Log.d(TAG, "Session saved - User: $username, Role: $role, IsGoogle: $isGoogle")
    }

    private fun updateUIForLoginMode() {
        if (isGoogleLoginMode) {
            // Mode Google/Firebase
            usernameInput.hint = "Masukkan email"
            usernameLabel.text = "Email"
            googleSignInButton.visibility = View.VISIBLE
            dividerContainer.visibility = View.VISIBLE
            forgotPassword.visibility = View.VISIBLE
            loginButton.text = "Login"
            registerButton.visibility = View.GONE
            loginModeSwitch.text = "Google/Firebase"

            // Ubah input type ke email
            usernameInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            // Mode MySQL
            usernameInput.hint = "Masukkan username"
            usernameLabel.text = "Username"
            googleSignInButton.visibility = View.GONE
            dividerContainer.visibility = View.GONE
            forgotPassword.visibility = View.GONE
            loginButton.text = "Login dengan SQL"
            registerButton.visibility = View.VISIBLE
            loginModeSwitch.text = "SQL Database"

            // Ubah input type ke text biasa
            usernameInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_NORMAL
        }

        // Clear input fields saat ganti mode
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
                Log.d(TAG, "Attempting email/password login for: $email")

                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser

                if (user != null) {
                    Log.d(TAG, "Email login successful for: ${user.email}")
                    saveUserToDatabase(user)
                    // Default role untuk Firebase user
                    saveUserSession(user.uid, user.email ?: "", "user", true)
                    Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login gagal, mencoba registrasi", e)
                registerWithEmailPassword(email, password)
            } finally {
                setButtonLoading(loginButton, false, "Login")
            }
        }
    }

    private fun registerWithEmailPassword(email: String, password: String) {
        lifecycleScope.launch {
            try {
                setButtonLoading(loginButton, true, "Mendaftarkan...")
                Log.d(TAG, "Attempting registration for: $email")

                auth.createUserWithEmailAndPassword(email, password).await()
                val user = auth.currentUser

                if (user != null) {
                    Log.d(TAG, "Registration successful for: ${user.email}")
                    saveUserToDatabase(user)
                    saveUserSession(user.uid, user.email ?: "", "user", true)
                    Toast.makeText(this@LoginActivity, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication gagal", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Authentication gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
                    Log.d(TAG, "User data saved successfully to Firebase Database")
                } else {
                    Log.e(TAG, "Failed to save user data", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user data", e)
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val email = usernameInput.text.toString().trim()

        if (email.isEmpty()) {
            usernameInput.error = "Masukkan email untuk reset password"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameInput.error = "Format email tidak valid"
            return
        }

        lifecycleScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                Toast.makeText(
                    this@LoginActivity,
                    "Email reset password telah dikirim ke $email",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengirim email reset", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Gagal mengirim email: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun signInWithGoogle() {
        try {
            Log.d(TAG, "Starting Google Sign In process")

            // Sign out dari akun sebelumnya untuk memastikan pemilihan akun
            googleSignInClient.signOut().addOnCompleteListener {
                Log.d(TAG, "Previous Google account signed out")
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Google Sign In", e)
            Toast.makeText(this, "Tidak dapat membuka Google Sign In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting Firebase authentication with Google credential")

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    Log.d(TAG, "Firebase auth successful - UID: ${user.uid}, Email: ${user.email}")

                    saveUserToDatabase(user)

                    // Simpan session dengan role default "user"
                    saveUserSession(
                        userId = user.uid,
                        username = user.email ?: user.displayName ?: "Google User",
                        role = "user",
                        isGoogle = true
                    )

                    Toast.makeText(this@LoginActivity, "Google sign in berhasil!", Toast.LENGTH_SHORT).show()

                    // Delay sedikit untuk memastikan Toast muncul
                    kotlinx.coroutines.delay(500)
                    navigateToHome()
                } else {
                    Log.e(TAG, "Firebase user is null after authentication")
                    Toast.makeText(this@LoginActivity, "Gagal mendapatkan data user", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google authentication gagal", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Authentication gagal: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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

                val result = mysqlApiService.login(username, password)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response?.success == true && response.data != null) {
                        val userData = response.data
                        saveUserSession(
                            userData.id_user.toString(),
                            userData.username,
                            userData.role,
                            false
                        )
                        Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            response?.message ?: "Login gagal",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Toast.makeText(
                        this@LoginActivity,
                        "Koneksi gagal: ${error?.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "MySQL login failed", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "MySQL login error", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setButtonLoading(loginButton, false, "Login dengan SQL")
            }
        }
    }

    private fun performMySQLRegister() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (validateInputForMySQL(username, password)) {
            registerWithMySQL(username, password)
        }
    }

    private fun registerWithMySQL(username: String, password: String) {
        lifecycleScope.launch {
            try {
                setButtonLoading(registerButton, true, "Loading...")

                val result = mysqlApiService.register(username, password)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response?.success == true && response.data != null) {
                        val userData = response.data
                        saveUserSession(
                            userData.id_user.toString(),
                            userData.username,
                            userData.role,
                            false
                        )
                        Toast.makeText(this@LoginActivity, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            response?.message ?: "Registrasi gagal",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Toast.makeText(
                        this@LoginActivity,
                        "Koneksi gagal: ${error?.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "MySQL register failed", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "MySQL register error", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setButtonLoading(registerButton, false, "Register Akun Baru")
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private fun setButtonLoading(button: Button, isLoading: Boolean, text: String) {
        button.isEnabled = !isLoading
        button.text = text
    }

    private fun navigateToHome() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val userRole = prefs.getString(KEY_USER_ROLE, "") ?: ""
            val username = prefs.getString(KEY_USERNAME, "") ?: ""

            Log.d(TAG, "Navigating to Home - User: $username, Role: $userRole")

            // Untuk semua user, gunakan HomeActivity
            // DashboardActivity hanya untuk admin panel yang berbeda
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("USER_ROLE", userRole)
                putExtra("USERNAME", username)
                // Clear back stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(intent)
            finish()

            Log.d(TAG, "Navigation to HomeActivity completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during navigation", e)
            Toast.makeText(this, "Error navigating: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getString(KEY_USER_ID, null)
        val savedLoginMode = prefs.getBoolean(KEY_LOGIN_MODE, true)

        // Cek apakah user sudah login
        if (savedUserId != null) {
            if (savedLoginMode) {
                // Cek Firebase
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    Log.d(TAG, "User sudah login dengan Firebase: ${currentUser.email}")
                    navigateToHome()
                } else {
                    // Session expired, clear saved data
                    Log.d(TAG, "Firebase session expired, clearing data")
                    prefs.edit().clear().apply()
                }
            } else {
                // MySQL login - langsung navigate
                val username = prefs.getString(KEY_USERNAME, "")
                val role = prefs.getString(KEY_USER_ROLE, "")
                Log.d(TAG, "User sudah login dengan MySQL - User: $username, Role: $role")
                navigateToHome()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
        Log.d(TAG, "LoginActivity destroyed")
    }
}