package com.example.projektpq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projektpq.models.Soal
import com.example.projektpq.service.SoalApiService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MulaiUjianActivity : AppCompatActivity() {

    private lateinit var soalApiService: SoalApiService
    private var soalList: List<Soal> = emptyList()
    private var currentPage = 0
    private val soalPerPage = 4

    // Menyimpan nilai untuk setiap soal
    private val nilaiMap = mutableMapOf<Int, Double>()

    private var idUjian: Int = 0
    private var idJilid: Int = 0
    private var namaJilid: String = ""
    private var idSantri: Int = 0
    private var namaSantri: String = ""

    // Views
    private lateinit var tvJilid: TextView
    private lateinit var tvSoal1: TextView
    private lateinit var tvSoal2: TextView
    private lateinit var tvSoal3: TextView
    private lateinit var tvSoal4: TextView
    private lateinit var etNilai1: EditText
    private lateinit var etNilai2: EditText
    private lateinit var etNilai3: EditText
    private lateinit var etNilai4: EditText
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button

    // Di MulaiUjianActivity.kt tambahkan:
    companion object {
        private const val TAG = "MulaiUjianActivity"
        const val EXTRA_ID_UJIAN = "id_ujian"
        const val EXTRA_ID_JILID = "id_jilid"
        const val EXTRA_NAMA_JILID = "nama_jilid"
        const val EXTRA_ID_SANTRI = "id_santri"
        const val EXTRA_NAMA_SANTRI = "nama_santri"
        const val EXTRA_NO_INDUK = "no_induk" // Tambahkan ini
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mulai_ujian)

        // Get data from intent
        idUjian = intent.getIntExtra(EXTRA_ID_UJIAN, 0)
        idJilid = intent.getIntExtra(EXTRA_ID_JILID, 0)
        namaJilid = intent.getStringExtra(EXTRA_NAMA_JILID) ?: ""
        idSantri = intent.getIntExtra(EXTRA_ID_SANTRI, 0)
        namaSantri = intent.getStringExtra(EXTRA_NAMA_SANTRI) ?: ""

        if (idUjian == 0 || idJilid == 0) {
            Toast.makeText(this, "Data ujian tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        soalApiService = SoalApiService()

        setupViews()
        loadSoalData()
    }

    private fun setupViews() {
        tvJilid = findViewById(R.id.jilid_i)
        tvJilid.text = namaJilid

        tvSoal1 = findViewById(R.id.bacakan_kal_1)
        tvSoal2 = findViewById(R.id.bacakan_kal_2)
        tvSoal3 = findViewById(R.id.bacakan_kal_3)
        tvSoal4 = findViewById(R.id.bacakan_kal_4)

        etNilai1 = findViewById(R.id.berikan_nil_1)
        etNilai2 = findViewById(R.id.berikan_nil_2)
        etNilai3 = findViewById(R.id.berikan_nil_3)
        etNilai4 = findViewById(R.id.berikan_nil_4)

        // Make EditText editable
        etNilai1.isFocusable = true
        etNilai2.isFocusable = true
        etNilai3.isFocusable = true
        etNilai4.isFocusable = true

        btnNext = findViewById(R.id.next)
        btnBack = findViewById(R.id.back)

        btnNext.setOnClickListener {
            saveCurrentPageValues()

            if (hasMorePages()) {
                currentPage++
                displayCurrentPage()
            } else {
                finishUjian()
            }
        }

        btnBack.setOnClickListener {
            if (currentPage > 0) {
                saveCurrentPageValues()
                currentPage--
                displayCurrentPage()
            }
        }

        // Cancel button
        findViewById<View>(R.id.cancel).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Batalkan Ujian")
                .setMessage("Apakah Anda yakin ingin membatalkan ujian?")
                .setPositiveButton("Ya") { _, _ ->
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    private fun loadSoalData() {
        lifecycleScope.launch {
            try {
                val result = soalApiService.getSoalByJilid(idJilid)

                result.onSuccess { response ->
                    if (response.success && response.data != null) {
                        soalList = response.data
                        Log.d(TAG, "Loaded ${soalList.size} soal")
                        displayCurrentPage()
                    } else {
                        Toast.makeText(
                            this@MulaiUjianActivity,
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error loading soal", error)
                    Toast.makeText(
                        this@MulaiUjianActivity,
                        "Gagal memuat soal: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception loading soal", e)
                Toast.makeText(
                    this@MulaiUjianActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun displayCurrentPage() {
        val startIndex = currentPage * soalPerPage
        val endIndex = minOf(startIndex + soalPerPage, soalList.size)

        // Reset all views
        tvSoal1.text = ""
        tvSoal2.text = ""
        tvSoal3.text = ""
        tvSoal4.text = ""
        etNilai1.setText("")
        etNilai2.setText("")
        etNilai3.setText("")
        etNilai4.setText("")

        // Display soal
        val textViews = listOf(tvSoal1, tvSoal2, tvSoal3, tvSoal4)
        val editTexts = listOf(etNilai1, etNilai2, etNilai3, etNilai4)

        for (i in 0 until minOf(soalPerPage, endIndex - startIndex)) {
            val soal = soalList[startIndex + i]
            textViews[i].text = "${soal.nomor_soal}. ${soal.isi_soal}"

            // Load saved nilai if exists
            nilaiMap[soal.id_soal]?.let { nilai ->
                editTexts[i].setText(nilai.toString())
            }
        }

        // Update button visibility
        btnBack.isEnabled = currentPage > 0
        btnNext.text = if (hasMorePages()) "Next" else "Selesai"
    }

    private fun hasMorePages(): Boolean {
        return (currentPage + 1) * soalPerPage < soalList.size
    }

    private fun saveCurrentPageValues() {
        val startIndex = currentPage * soalPerPage
        val editTexts = listOf(etNilai1, etNilai2, etNilai3, etNilai4)

        for (i in 0 until minOf(soalPerPage, soalList.size - startIndex)) {
            val soal = soalList[startIndex + i]
            val nilaiText = editTexts[i].text.toString()

            if (nilaiText.isNotEmpty()) {
                try {
                    val nilai = nilaiText.toDouble()
                    if (nilai in 0.0..100.0) {
                        nilaiMap[soal.id_soal] = nilai
                    } else {
                        Toast.makeText(
                            this,
                            "Nilai soal ${soal.nomor_soal} harus antara 0-100",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        this,
                        "Format nilai soal ${soal.nomor_soal} tidak valid",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun finishUjian() {
        saveCurrentPageValues()

        // Check if all soal have nilai
        if (nilaiMap.size < soalList.size) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Peringatan")
                .setMessage("Masih ada soal yang belum dinilai. Lanjutkan menyelesaikan ujian?")
                .setPositiveButton("Ya") { _, _ ->
                    submitAllNilai()
                }
                .setNegativeButton("Tidak", null)
                .show()
        } else {
            submitAllNilai()
        }
    }

    private fun submitAllNilai() {
        lifecycleScope.launch {
            try {
                var allSuccess = true

                // Submit each jawaban
                for (soal in soalList) {
                    val nilai = nilaiMap[soal.id_soal] ?: 0.0

                    val result = soalApiService.submitJawaban(
                        idUjian = idUjian,
                        idSoal = soal.id_soal,
                        nilai = nilai
                    )

                    result.onFailure {
                        allSuccess = false
                        Log.e(TAG, "Failed to submit soal ${soal.id_soal}")
                    }
                }

                if (allSuccess) {
                    // Finish ujian
                    val finishResult = soalApiService.finishUjian(idUjian)

                    finishResult.onSuccess { response ->
                        if (response.success && response.data != null) {
                            val ujian = response.data

                            // Navigate to SelesaiUjianActivity
                            val intent = Intent(this@MulaiUjianActivity, SelesaiUjianActivity::class.java)
                            intent.putExtra(SelesaiUjianActivity.EXTRA_NAMA_SANTRI, namaSantri)
                            intent.putExtra(SelesaiUjianActivity.EXTRA_NO_INDUK, idSantri.toString())
                            intent.putExtra(SelesaiUjianActivity.EXTRA_NILAI, ujian.nilai_total)
                            startActivity(intent)
                            finish()

                        } else {
                            Toast.makeText(
                                this@MulaiUjianActivity,
                                response.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    finishResult.onFailure { error ->
                        Toast.makeText(
                            this@MulaiUjianActivity,
                            "Gagal menyelesaikan ujian: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@MulaiUjianActivity,
                        "Gagal menyimpan beberapa jawaban",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception submitting nilai", e)
                Toast.makeText(
                    this@MulaiUjianActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}