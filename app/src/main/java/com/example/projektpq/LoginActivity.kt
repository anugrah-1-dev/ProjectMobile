package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.service.FirebaseDatabaseService
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

    // Deklarasi views manual
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePassword: ImageButton
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var googleSignInButton: com.google.android.gms.common.SignInButton

    private val RC_SIGN_IN = 123
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        initializeViews()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        databaseService = FirebaseDatabaseService()

        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun initializeViews() {
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        togglePassword = findViewById(R.id.toggle_password)
        loginButton = findViewById(R.id.login_button)
        forgotPassword = findViewById(R.id.forgot_password)
        googleSignInButton = findViewById(R.id.google_sign_in_button)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        // Toggle password visibility
        togglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login button - Email/Password
        loginButton.setOnClickListener {
            performEmailPasswordLogin()
        }

        // Forgot password
        forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Google sign in
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
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

    private fun performEmailPasswordLogin() {
        val email = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (validateInput(email, password)) {
            loginWithEmailPassword(email, password)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                usernameInput.error = "Email tidak boleh kosong"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                usernameInput.error = "Format email tidak valid"
                false
            }
            password.isEmpty() -> {
                passwordInput.error = "Password tidak boleh kosong"
                false
            }
            password.length < 6 -> {
                passwordInput.error = "Password minimal 6 karakter"
                false
            }
            else -> true
        }
    }

    private fun loginWithEmailPassword(email: String, password: String) {
        lifecycleScope.launch {
            try {
                loginButton.isEnabled = false
                loginButton.text = "Loading..."

                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                if (user != null) {
                    saveUserToDatabase(user)
                    Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login gagal, mencoba registrasi", e)
                registerWithEmailPassword(email, password)
            } finally {
                loginButton.isEnabled = true
                loginButton.text = "Login"
            }
        }
    }

    private fun registerWithEmailPassword(email: String, password: String) {
        lifecycleScope.launch {
            try {
                loginButton.isEnabled = false
                loginButton.text = "Mendaftarkan..."

                auth.createUserWithEmailAndPassword(email, password).await()
                val user = auth.currentUser
                if (user != null) {
                    saveUserToDatabase(user)
                    Toast.makeText(this@LoginActivity, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication gagal", e)
                Toast.makeText(this@LoginActivity, "Authentication gagal: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                loginButton.isEnabled = true
                loginButton.text = "Login"
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
                    Log.d(TAG, "User data saved successfully")
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

        lifecycleScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                Toast.makeText(this@LoginActivity, "Email reset password telah dikirim", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengirim email reset", e)
                Toast.makeText(this@LoginActivity, "Gagal mengirim email reset: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in gagal", e)
                Toast.makeText(this, "Google sign in gagal: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        lifecycleScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                val user = auth.currentUser
                if (user != null) {
                    saveUserToDatabase(user)
                    Toast.makeText(this@LoginActivity, "Google sign in berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication gagal", e)
                Toast.makeText(this@LoginActivity, "Authentication gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Cek apakah user sudah login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHome()
        }
    }
}