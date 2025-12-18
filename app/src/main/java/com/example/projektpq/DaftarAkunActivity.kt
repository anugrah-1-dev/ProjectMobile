package com.example.projektpq

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.projektpq.models.User

class DaftarAkunActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnRefresh: ImageView
    private lateinit var etSearch: EditText
    private lateinit var rvAkun: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var tvTotalAkun: TextView
    private lateinit var tvSuperAdmin: TextView
    private lateinit var tvAdmin: TextView

    private lateinit var akunAdapter: AkunAdapter
    private var userList = mutableListOf<User>()

    companion object {
        private const val TAG = "DaftarAkunActivity"
        private const val API_URL = "https://kampunginggrisori.com/api/get_users.php"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_akun)

        Log.d(TAG, "=== DaftarAkunActivity onCreate START ===")

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        loadUsers()
    }

    private fun initializeViews() {
        try {
            btnBack = findViewById(R.id.btn_back)
            btnRefresh = findViewById(R.id.btn_refresh)
            etSearch = findViewById(R.id.et_search)
            rvAkun = findViewById(R.id.rv_akun)
            progressBar = findViewById(R.id.progress_bar)
            emptyState = findViewById(R.id.empty_state)
            tvTotalAkun = findViewById(R.id.tv_total_akun)
            tvSuperAdmin = findViewById(R.id.tv_super_admin)
            tvAdmin = findViewById(R.id.tv_admin)

            Log.d(TAG, "✓ All views initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing views", e)
            throw e
        }
    }

    private fun setupRecyclerView() {
        akunAdapter = AkunAdapter(userList) { user ->
            showUserDetail(user)
        }
        rvAkun.apply {
            layoutManager = LinearLayoutManager(this@DaftarAkunActivity)
            adapter = akunAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnRefresh.setOnClickListener {
            loadUsers()
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                akunAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUsers() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val responseString = response.toString()
                    Log.d(TAG, "Response: $responseString")

                    val jsonResponse = JSONObject(responseString)
                    val success = jsonResponse.getBoolean("success")

                    withContext(Dispatchers.Main) {
                        showLoading(false)

                        if (success) {
                            val dataArray = jsonResponse.getJSONArray("data")
                            userList.clear()

                            for (i in 0 until dataArray.length()) {
                                val userObj = dataArray.getJSONObject(i)
                                val user = User(
                                    id_user = userObj.getInt("id_user"),
                                    username = userObj.getString("username"),
                                    role = userObj.getString("role"),
                                    email = userObj.optString("email", ""),
                                    nomor_telepon = userObj.optString("nomor_telepon", "")
                                )
                                userList.add(user)
                            }

                            Log.d(TAG, "✓ Loaded ${userList.size} users")
                            akunAdapter.updateData(userList)
                            updateStatistics()
                            showEmptyState(userList.isEmpty())
                        } else {
                            val message = jsonResponse.getString("message")
                            Toast.makeText(
                                this@DaftarAkunActivity,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                            showEmptyState(true)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(
                            this@DaftarAkunActivity,
                            "Server error: $responseCode",
                            Toast.LENGTH_SHORT
                        ).show()
                        showEmptyState(true)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "✗ Network error", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)

                    val errorMessage = when {
                        e.message?.contains("Unable to resolve host") == true ->
                            "Tidak dapat terhubung ke server"
                        e.message?.contains("timeout") == true ->
                            "Koneksi timeout"
                        else -> "Error: ${e.message}"
                    }

                    Toast.makeText(
                        this@DaftarAkunActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    showEmptyState(true)
                }
            }
        }
    }

    private fun updateStatistics() {
        val total = userList.size
        val superAdminCount = userList.count { it.role == "SUPER ADMIN" }
        val adminCount = userList.count { it.role == "admin biasa" }

        tvTotalAkun.text = total.toString()
        tvSuperAdmin.text = superAdminCount.toString()
        tvAdmin.text = adminCount.toString()

        Log.d(TAG, "Statistics - Total: $total, Super Admin: $superAdminCount, Admin: $adminCount")
    }

    private fun showUserDetail(user: User) {
        val message = """
            ID: ${user.id_user}
            Username: ${user.username}
            Role: ${user.role}
            Email: ${if (user.email.isEmpty()) "-" else user.email}
            Telepon: ${if (user.nomor_telepon.isEmpty()) "-" else user.nomor_telepon}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Detail Akun")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvAkun.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
        rvAkun.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DaftarAkunActivity destroyed")
    }
}