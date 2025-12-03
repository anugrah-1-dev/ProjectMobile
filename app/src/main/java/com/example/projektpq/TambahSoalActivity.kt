package com.example.projektpq

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.models.Jilid
import com.example.projektpq.service.SoalApiService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class TambahSoalActivity : AppCompatActivity() {

    private lateinit var soalApiService: SoalApiService
    private var jilidList: List<Jilid> = emptyList()
    private var selectedJilid: Jilid? = null

    private lateinit var etNomorSoal: EditText
    private lateinit var etSoal: EditText

    companion object {
        private const val TAG = "TambahSoalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tambah_soal)

        soalApiService = SoalApiService()

        setupViews()
        loadJilidData()
    }

    private fun setupViews() {
        etNomorSoal = findViewById(R.id.masukkan_no)
        etSoal = findViewById(R.id.masukkan_so)

        // Make EditText actually editable
        etNomorSoal.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            hint = "Masukkan Nomor Soal"
            setText("")
        }

        etSoal.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            hint = "Masukkan Soal"
            setText("")
        }

        // Jilid selection button - TextView untuk menampilkan jilid terpilih
        val tvJilidSelection = findViewById<TextView>(R.id.soal_ujian_)
        tvJilidSelection.setOnClickListener {
            showJilidSelectionDialog()
        }

        // Simpan button - group_44 kemungkinan adalah container/button untuk simpan
        val btnSimpan = findViewById<View>(R.id.group_44)
        btnSimpan.setOnClickListener {
            saveSoal()
        }

        // Bottom Navigation
        val homeButton = findViewById<View>(R.id.home_button_container)
        homeButton.setOnClickListener {
            finish()
        }

        val settingsButton = findViewById<View>(R.id.setting_button_container)
        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadJilidData() {
        lifecycleScope.launch {
            try {
                val result = soalApiService.getAllJilid()

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        jilidList = response.data
                    }
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@TambahSoalActivity,
                        "Gagal memuat data jilid: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@TambahSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showJilidSelectionDialog() {
        if (jilidList.isEmpty()) {
            Toast.makeText(this, "Data jilid belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val jilidNames = jilidList.map { it.nama_jilid }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Pilih Jilid")
            .setItems(jilidNames) { _, which ->
                selectedJilid = jilidList[which]
                findViewById<TextView>(R.id.soal_ujian_).text =
                    "Soal Ujian\n${selectedJilid?.nama_jilid}"
            }
            .show()
    }

    private fun saveSoal() {
        val nomorSoalText = etNomorSoal.text.toString().trim()
        val isiSoal = etSoal.text.toString().trim()

        when {
            selectedJilid == null -> {
                Toast.makeText(this, "Pilih jilid terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }

            nomorSoalText.isEmpty() -> {
                Toast.makeText(this, "Nomor soal tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return
            }

            isiSoal.isEmpty() -> {
                Toast.makeText(this, "Isi soal tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val nomorSoal = try {
            nomorSoalText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Nomor soal harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = soalApiService.addSoal(
                    idJilid = selectedJilid!!.id_jilid,
                    nomorSoal = nomorSoal,
                    isiSoal = isiSoal,
                    tipeSoal = "praktek",
                    bobotNilai = 25
                )

                result.onSuccess { response ->
                    if (response.success) {
                        Toast.makeText(
                            this@TambahSoalActivity,
                            "Soal berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Clear form
                        etNomorSoal.setText("")
                        etSoal.setText("")
                        selectedJilid = null
                        findViewById<TextView>(R.id.soal_ujian_).text = "Soal Ujian\nJilid I"

                    } else {
                        Toast.makeText(
                            this@TambahSoalActivity,
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                result.onFailure { error ->
                    Toast.makeText(
                        this@TambahSoalActivity,
                        "Gagal menambahkan soal: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@TambahSoalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}