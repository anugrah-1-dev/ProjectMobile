package com.example.tpqapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class SoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soal) // Anda perlu membuat layout untuk ini

        // Ambil data jilid dari intent
        val jilid = intent.getStringExtra("JILID")

        // Tampilkan judul jilid
        val tvJudulJilid = findViewById<TextView>(R.id.tv_judul_jilid)
        tvJudulJilid?.text = "Manajemen Soal - $jilid"

        // Tampilkan toast konfirmasi
        Toast.makeText(this, "Anda memilih: $jilid", Toast.LENGTH_SHORT).show()

        // TODO: Implementasi UI untuk form soal sesuai jilid yang dipilih
        // Anda perlu membuat layout activity_soal.xml untuk menampilkan form soal

        // Inisialisasi tombol-tombol
        setupButtons(jilid)
    }

    private fun setupButtons(jilid: String?) {
        // Implementasi tombol-tombol sesuai kebutuhan
        // Contoh: tombol tambah soal, edit soal, lihat soal, dll.
    }

    // Handle tombol back
    override fun onBackPressed() {
        super.onBackPressed()
        // Kembali ke PilihJilidActivity dengan animasi
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}