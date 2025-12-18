package com.example.projektpq

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class TambahAkunActivity : AppCompatActivity() {

    // UI Components
    private lateinit var btnBack: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var rbSuperAdmin: RadioButton
    private lateinit var rbAdmin: RadioButton
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnCancel: AppCompatButton
    private lateinit var btnSubmit: AppCompatButton

    companion object {
        private const val TAG = "TambahAkunActivity"

        // API Configuration - SESUAIKAN dengan server Anda
        private const val API_URL = "https://kampunginggrisori.com/api/register.php" // Untuk emulator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_akun)

        Log.d(TAG, "=== TambahAkunActivity onCreate START ===")

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        try {
            btnBack = findViewById(R.id.btn_back)
            etUsername = findViewById(R.id.et_username)
            etPassword = findViewById(R.id.et_password)
            etConfirmPassword = findViewById(R.id.et_confirm_password)
            rgRole = findViewById(R.id.rg_role)
            rbSuperAdmin = findViewById(R.id.rb_super_admin)
            rbAdmin = findViewById(R.id.rb_admin)
            etEmail = findViewById(R.id.et_email)
            etPhone = findViewById(R.id.et_phone)
            btnCancel = findViewById(R.id.btn_cancel)
            btnSubmit = findViewById(R.id.btn_submit)

            Log.d(TAG, "✓ All views initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing views", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            showCancelConfirmation()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Batalkan pembuatan akun? Data yang diisi akan hilang.")
            .setPositiveButton("Ya") { _, _ ->
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun validateAndSubmit() {
        // Get input values
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Get selected role
        val selectedRoleId = rgRole.checkedRadioButtonId
        val role = when (selectedRoleId) {
            R.id.rb_super_admin -> "SUPER ADMIN"
            R.id.rb_admin -> "admin biasa"
            else -> ""
        }

        // Validation
        if (username.isEmpty()) {
            etUsername.error = "Username tidak boleh kosong"
            etUsername.requestFocus()
            return
        }

        if (username.length < 3) {
            etUsername.error = "Username minimal 3 karakter"
            etUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            etPassword.requestFocus()
            return
        }

        if (password.length < 4) {
            etPassword.error = "Password minimal 4 karakter"
            etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Password tidak cocok"
            etConfirmPassword.requestFocus()
            return
        }

        // Email dan phone opsional, tapi validasi jika diisi
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            etEmail.requestFocus()
            return
        }

        if (phone.isNotEmpty() && phone.length < 10) {
            etPhone.error = "Nomor telepon minimal 10 digit"
            etPhone.requestFocus()
            return
        }

        if (role.isEmpty()) {
            Toast.makeText(this, "Pilih role terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        showSubmitConfirmation(username, password, role, email, phone)
    }

    private fun showSubmitConfirmation(
        username: String,
        password: String,
        role: String,
        email: String,
        phone: String
    ) {
        val emailDisplay = if (email.isEmpty()) "-" else email
        val phoneDisplay = if (phone.isEmpty()) "-" else phone

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembuatan Akun")
            .setMessage("""
                Username: $username
                Role: $role
                Email: $emailDisplay
                Telepon: $phoneDisplay
                
                Apakah data sudah benar?
            """.trimIndent())
            .setPositiveButton("Ya, Simpan") { _, _ ->
                registerAccount(username, password, role, email, phone)
            }
            .setNegativeButton("Periksa Kembali", null)
            .show()
    }

    private fun registerAccount(
        username: String,
        password: String,
        role: String,
        email: String,
        phone: String
    ) {
        // Show loading
        btnSubmit.isEnabled = false
        btnSubmit.text = "Menyimpan..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                // Prepare JSON data
                val jsonObject = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                    put("role", role)
                    put("email", email)
                    put("nomor_telepon", phone)
                }

                Log.d(TAG, "Sending data: $jsonObject")

                // Send data
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonObject.toString())
                writer.flush()
                writer.close()

                // Read response
                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val responseString = response.toString()
                Log.d(TAG, "Response: $responseString")

                // Parse JSON response
                val jsonResponse = JSONObject(responseString)
                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                withContext(Dispatchers.Main) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Simpan"

                    if (success) {
                        Log.d(TAG, "✓ Account created successfully")
                        Toast.makeText(
                            this@TambahAkunActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        // Tampilkan dialog success
                        AlertDialog.Builder(this@TambahAkunActivity)
                            .setTitle("Berhasil")
                            .setMessage("Akun berhasil dibuat!\n\nUsername: $username\nRole: $role")
                            .setPositiveButton("OK") { _, _ ->
                                finish()
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        Log.e(TAG, "✗ Failed: $message")
                        Toast.makeText(
                            this@TambahAkunActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "✗ Network error", e)
                withContext(Dispatchers.Main) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Simpan"

                    val errorMessage = when {
                        e.message?.contains("Unable to resolve host") == true ->
                            "Tidak dapat terhubung ke server. Periksa koneksi internet."
                        e.message?.contains("timeout") == true ->
                            "Koneksi timeout. Coba lagi."
                        else -> "Error: ${e.message}"
                    }

                    Toast.makeText(
                        this@TambahAkunActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "TambahAkunActivity destroyed")
    }
}