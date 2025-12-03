package com.example.projektpq

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelesaiUjianActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMA_SANTRI = "nama_santri"
        const val EXTRA_NO_INDUK = "no_induk"
        const val EXTRA_NILAI = "nilai"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selesai_ujian)

        val namaSantri = intent.getStringExtra(EXTRA_NAMA_SANTRI) ?: ""
        val noInduk = intent.getStringExtra(EXTRA_NO_INDUK) ?: ""
        val nilai = intent.getDoubleExtra(EXTRA_NILAI, 0.0)

        findViewById<TextView>(R.id.nama_text).text = namaSantri
        findViewById<TextView>(R.id.id_text).text = "No. Induk: $noInduk"
        findViewById<TextView>(R.id.nilai_text).text = "Nilai: ${"%.2f".format(nilai)}"

        findViewById<Button>(R.id.button_selesai).setOnClickListener {
            finish()
        }
    }
}