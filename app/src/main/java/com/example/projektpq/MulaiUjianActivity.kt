package com.example.projektpq // SESUAIKAN DENGAN PACKAGE NAME PROJEK ANDA

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MulaiUjianActivity : AppCompatActivity() {

    // Deklarasi variabel untuk input nilai
    private lateinit var nilaiInput1: EditText
    private lateinit var nilaiInput2: EditText
    private lateinit var nilaiInput3: EditText
    private lateinit var nilaiInput4: EditText

    // Deklarasi variabel untuk soal
    private lateinit var soal1: TextView
    private lateinit var soal2: TextView
    private lateinit var soal3: TextView
    private lateinit var soal4: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mulai_ujian2) // Pastikan nama layout sesuai

        // Inisialisasi TextView soal
        soal1 = findViewById(R.id.bacakan_kal_1)
        soal2 = findViewById(R.id.bacakan_kal_2)
        soal3 = findViewById(R.id.bacakan_kal_3)
        soal4 = findViewById(R.id.bacakan_kal_4)

        // Inisialisasi EditText untuk input nilai
        // Catatan: Anda perlu mengganti TextView dengan EditText di XML
        // atau tambahkan EditText baru di dalam View background

        // Contoh jika Anda menambahkan EditText dengan id berikut di XML:
        // nilaiInput1 = findViewById(R.id.edit_nilai_1)
        // nilaiInput2 = findViewById(R.id.edit_nilai_2)
        // nilaiInput3 = findViewById(R.id.edit_nilai_3)
        // nilaiInput4 = findViewById(R.id.edit_nilai_4)

        // Untuk sementara, kita akan menggunakan TextView sebagai placeholder
        // Setelah Anda menambahkan EditText di XML, ganti dengan kode di atas

        // Muat soal dari SharedPreferences atau database
        muatSoal()

        // Tombol Back
        val backButton = findViewById<View>(R.id.back_button)
        val backText = findViewById<TextView>(R.id.back)

        backButton?.setOnClickListener {
            kembaliKeHalamanSebelumnya()
        }

        backText?.setOnClickListener {
            kembaliKeHalamanSebelumnya()
        }

        // Tombol Next
        val nextButton = findViewById<View>(R.id.next_button)
        val nextText = findViewById<TextView>(R.id.next)

        nextButton?.setOnClickListener {
            simpanNilaiDanLanjut()
        }

        nextText?.setOnClickListener {
            simpanNilaiDanLanjut()
        }

        // Tombol Choose Page (Navigasi halaman)
        val choosePage = findViewById<View>(R.id.choose_page)
        choosePage?.setOnClickListener {
            tampilkanDialogPilihHalaman()
        }

        // Tombol Cancel
        val cancelButton = findViewById<View>(R.id.cancel)
        cancelButton?.setOnClickListener {
            batalkanUjian()
        }
    }

    private fun muatSoal() {
        // Muat soal dari SharedPreferences atau database
        val sharedPref = getSharedPreferences("soal_ujian", MODE_PRIVATE)

        // Contoh: Muat soal nomor 1-4
        // Anda bisa sesuaikan dengan logika aplikasi Anda
        val soal1Text = sharedPref.getString("nomor_1", "Bacakan kalimat Istiadzah!")
        val soal2Text = sharedPref.getString("nomor_2", "Bacakan kalimat Basmallah!")
        val soal3Text = sharedPref.getString("nomor_3", "Bacakan kalimat Tahmid!")
        val soal4Text = sharedPref.getString("nomor_4", "Bacakan kalimat Tahmid!")

        // Set teks soal
        soal1.text = soal1Text ?: "Bacakan kalimat Istiadzah!"
        soal2.text = soal2Text ?: "Bacakan kalimat Basmallah!"
        soal3.text = soal3Text ?: "Bacakan kalimat Tahmid!"
        soal4.text = soal4Text ?: "Bacakan kalimat Tahmid!"
    }

    private fun simpanNilaiDanLanjut() {
        // Simpan nilai ke SharedPreferences atau database
        val sharedPref = getSharedPreferences("nilai_ujian", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Contoh: Simpan nilai (jika sudah ada EditText)
        // val nilai1 = nilaiInput1.text.toString()
        // val nilai2 = nilaiInput2.text.toString()
        // val nilai3 = nilaiInput3.text.toString()
        // val nilai4 = nilaiInput4.text.toString()

        // Simpan nilai dummy untuk saat ini
        editor.putString("nilai_1", "0")
        editor.putString("nilai_2", "0")
        editor.putString("nilai_3", "0")
        editor.putString("nilai_4", "0")
        editor.apply()

        Toast.makeText(this, "Nilai disimpan!", Toast.LENGTH_SHORT).show()

        // Navigasi ke halaman berikutnya atau selesaikan ujian
        // Contoh: val intent = Intent(this, HalamanBerikutnyaActivity::class.java)
        // startActivity(intent)
    }

    private fun kembaliKeHalamanSebelumnya() {
        // Tampilkan konfirmasi jika ada data yang belum disimpan
        // Untuk sementara langsung kembali
        finish()
    }

    private fun tampilkanDialogPilihHalaman() {
        // Implement dialog untuk memilih halaman soal
        Toast.makeText(this, "Pilih halaman soal", Toast.LENGTH_SHORT).show()

        // Contoh implementasi AlertDialog
        /*
        val items = arrayOf("Halaman 1", "Halaman 2", "Halaman 3", "Halaman 4")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Halaman Soal")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> scrollToQuestion(1)
                    1 -> scrollToQuestion(2)
                    2 -> scrollToQuestion(3)
                    3 -> scrollToQuestion(4)
                }
            }
            .show()
        */
    }

    private fun batalkanUjian() {
        // Tampilkan konfirmasi pembatalan
        /*
        AlertDialog.Builder(this)
            .setTitle("Batalkan Ujian")
            .setMessage("Apakah Anda yakin ingin membatalkan ujian?")
            .setPositiveButton("Ya") { dialog, which ->
                // Hapus data sementara dan kembali ke halaman utama
                val sharedPref = getSharedPreferences("nilai_ujian", MODE_PRIVATE)
                sharedPref.edit().clear().apply()
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
        */

        // Untuk sementara langsung kembali
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}