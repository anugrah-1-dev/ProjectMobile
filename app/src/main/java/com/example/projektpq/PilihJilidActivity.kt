package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.models.Jilid
import com.example.projektpq.service.SoalApiService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class PilihJilidActivity : AppCompatActivity() {

    private lateinit var soalApiService: SoalApiService
    private var jilidList: List<Jilid> = emptyList()
    private var selectedJilid: Jilid? = null

    // Santri data (optional)
    private var noInduk: String = ""
    private var namaSantri: String = ""

    // Mode: "ujian" atau "manajemen"
    private var mode: String = "manajemen"

    companion object {
        private const val TAG = "PilihJilidActivity"
        const val EXTRA_NO_INDUK = "no_induk"
        const val EXTRA_NAMA_SANTRI = "nama_santri"
        const val EXTRA_MODE = "mode" // Tambahkan ini
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_jilid)

        // Get mode
        mode = intent.getStringExtra(EXTRA_MODE) ?: "manajemen"

        // Get santri data from intent (optional)
        noInduk = intent.getStringExtra(EXTRA_NO_INDUK) ?: ""
        namaSantri = intent.getStringExtra(EXTRA_NAMA_SANTRI) ?: ""

        // Validasi hanya jika mode ujian
        if (mode == "ujian" && noInduk.isEmpty()) {
            Toast.makeText(this, "Data santri tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Mode: $mode")
        if (mode == "ujian") {
            Log.d(TAG, "Santri: $namaSantri (No. Induk: $noInduk)")
        }

        soalApiService = SoalApiService()

        setupViews()
        loadJilidData()
    }

    private fun setupViews() {
        // Jilid Cards
        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(0)
        }

        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(1)
        }

        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(2)
        }

        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(3)
        }

        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(4)
        }

        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(5)
        }

        findViewById<CardView>(R.id.jilid).setOnClickListener {
            onJilidSelected(6)
        }

        // Bottom Navigation
        findViewById<ImageButton>(R.id.btn_home).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadJilidData() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading jilid data...")
                val result = soalApiService.getAllJilid()

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        jilidList = response.data
                        Log.d(TAG, "✓ Loaded ${jilidList.size} jilid")

                        // Log detail
                        jilidList.forEach { jilid ->
                            Log.d(TAG, "- ${jilid.nama_jilid} (ID: ${jilid.id_jilid})")
                        }
                    } else {
                        Log.w(TAG, "✗ Response: ${response.message}")
                        Toast.makeText(
                            this@PilihJilidActivity,
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "✗ Error loading jilid: ${error.message}", error)
                    Toast.makeText(
                        this@PilihJilidActivity,
                        "Gagal memuat data jilid: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "✗ Exception loading jilid", e)
                Toast.makeText(
                    this@PilihJilidActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun onJilidSelected(index: Int) {
        Log.d(TAG, "Jilid selected: index=$index, total=${jilidList.size}")

        if (jilidList.isEmpty()) {
            Toast.makeText(this, "Data jilid belum dimuat. Silakan tunggu...", Toast.LENGTH_SHORT).show()
            loadJilidData() // Retry loading
            return
        }

        if (index < jilidList.size) {
            selectedJilid = jilidList[index]
            Log.d(TAG, "Selected: ${selectedJilid?.nama_jilid}")

            // Berbeda berdasarkan mode
            if (mode == "ujian") {
                showUjianConfirmation()
            } else {
                // Mode manajemen - langsung ke ManajemenSoalActivity
                navigateToManajemenSoal()
            }
        } else {
            Log.w(TAG, "Index out of bounds: $index >= ${jilidList.size}")
            Toast.makeText(this, "Data jilid belum tersedia untuk pilihan ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUjianConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi Ujian")
            .setMessage("Mulai ujian untuk ${selectedJilid?.nama_jilid}?\n\nSantri: $namaSantri\nNo. Induk: $noInduk")
            .setPositiveButton("Mulai") { _, _ ->
                startUjian()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateToManajemenSoal() {
        val jilid = selectedJilid ?: return

        // TODO: Ganti dengan activity manajemen soal yang sebenarnya
        val intent = Intent(this, MulaiUjianActivity::class.java)
        intent.putExtra("id_jilid", jilid.id_jilid)
        intent.putExtra("nama_jilid", jilid.nama_jilid)
        startActivity(intent)
    }

    private fun startUjian() {
        val jilid = selectedJilid
        if (jilid == null) {
            Toast.makeText(this, "Jilid tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Creating ujian: no_induk=$noInduk, id_jilid=${jilid.id_jilid}")

        lifecycleScope.launch {
            try {
                // Create ujian session with no_induk
                val result = soalApiService.createUjian(noInduk, jilid.id_jilid)

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        val ujian = response.data
                        Log.d(TAG, "✓ Ujian created: id=${ujian.id_ujian}")

                        // Navigate to MulaiUjianActivity
                        val intent = Intent(this@PilihJilidActivity, MulaiUjianActivity::class.java)
                        intent.putExtra(MulaiUjianActivity.EXTRA_ID_UJIAN, ujian.id_ujian)
                        intent.putExtra(MulaiUjianActivity.EXTRA_ID_JILID, ujian.id_jilid)
                        intent.putExtra(MulaiUjianActivity.EXTRA_NAMA_JILID, jilid.nama_jilid)

                        // Send data to MulaiUjianActivity
                        intent.putExtra("no_induk", noInduk)
                        intent.putExtra(MulaiUjianActivity.EXTRA_NAMA_SANTRI, namaSantri)

                        Log.d(TAG, "Starting MulaiUjianActivity...")
                        startActivity(intent)

                    } else {
                        Log.w(TAG, "✗ Create ujian failed: ${response.message}")
                        Toast.makeText(
                            this@PilihJilidActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "✗ Error creating ujian: ${error.message}", error)
                    Toast.makeText(
                        this@PilihJilidActivity,
                        "Gagal membuat ujian: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "✗ Exception creating ujian", e)
                Toast.makeText(
                    this@PilihJilidActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload jilid if empty (in case coming back from another activity)
        if (jilidList.isEmpty()) {
            loadJilidData()
        }
    }
}