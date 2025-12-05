package com.example.projektpq // SESUAIKAN DENGAN PACKAGE NAME PROJEK ANDA

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.cardview.widget.CardView
import android.widget.ImageButton
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import com.example.projektpq.models.Jilid
import androidx.activity.OnBackPressedCallback

class PilihJilidActivity : AppCompatActivity() {

    // Data holder untuk jilid dari database
    private val jilidList = mutableListOf<Jilid>()

    // Deklarasi variabel untuk CardView dan TextView
    private lateinit var jilid1Card: CardView
    private lateinit var jilid2Card: CardView
    private lateinit var jilid3Card: CardView
    private lateinit var jilid4Card: CardView
    private lateinit var jilid5Card: CardView
    private lateinit var jilid6Card: CardView
    private lateinit var alquranCard: CardView

    private lateinit var jilid1Text: TextView
    private lateinit var jilid2Text: TextView
    private lateinit var jilid3Text: TextView
    private lateinit var jilid4Text: TextView
    private lateinit var jilid5Text: TextView
    private lateinit var jilid6Text: TextView
    private lateinit var alquranText: TextView

    private lateinit var btnHome: ImageButton
    private lateinit var btnSettings: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_jilid)

        // Inisialisasi elemen UI
        initializeViews()

        // Set click listener untuk tombol
        setupButtonListeners()

        // Setup back press handler menggunakan OnBackPressedDispatcher
        setupBackPressHandler()

        // Ambil data jilid dari database
        fetchJilidFromDatabase()
    }

    private fun initializeViews() {
        // Inisialisasi CardView
        jilid1Card = findViewById(R.id.jilid_1)
        jilid2Card = findViewById(R.id.jilid_2)
        jilid3Card = findViewById(R.id.jilid_3)
        jilid4Card = findViewById(R.id.jilid_4)
        jilid5Card = findViewById(R.id.jilid_5)
        jilid6Card = findViewById(R.id.jilid_6)
        alquranCard = findViewById(R.id.alquran)

        // Inisialisasi TextView
        jilid1Text = findViewById(R.id.tv_jilid1)
        jilid2Text = findViewById(R.id.tv_jilid2)
        jilid3Text = findViewById(R.id.tv_jilid3)
        jilid4Text = findViewById(R.id.tv_jilid4)
        jilid5Text = findViewById(R.id.tv_jilid5)
        jilid6Text = findViewById(R.id.tv_jilid6)
        alquranText = findViewById(R.id.tv_alquran)

        // Inisialisasi ImageButton
        btnHome = findViewById(R.id.btn_home)
        btnSettings = findViewById(R.id.btn_settings)
    }

    private fun setupButtonListeners() {
        // Klik pada Home Button
        btnHome.setOnClickListener {
            navigateBackToHome()
        }

        // Klik pada Settings Button
        btnSettings.setOnClickListener {
            val intent = Intent(this, PengaturanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBackPressHandler() {
        // Menggunakan OnBackPressedDispatcher yang compatible
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToHome()
            }
        })
    }

    private fun fetchJilidFromDatabase() {
        // URL untuk mengambil data jilid dari database
        // GANTI DENGAN URL API ANDA YANG SESUAI
        val url = "https://kampunginggrisori.com/api/jilid.php"

        // Tampilkan loading indicator jika ada
        Toast.makeText(this, "Memuat data jilid...", Toast.LENGTH_SHORT).show()

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    jilidList.clear()

                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)

                        // PERHATIKAN: Sesuaikan dengan field di database Anda
                        // Dari gambar: id_ijild, nama_ijild, deskripsi
                        // Dari model Jilid: id_ijild, nama_ijild, deskripsi
                        val id = jsonObject.getInt("id_jilid")
                        val nama = jsonObject.getString("nama_jilid")
                        val deskripsi = jsonObject.optString("deskripsi", "")

                        jilidList.add(Jilid(id, nama, deskripsi))
                    }

                    // Update UI dengan data dari database
                    updateJilidUI()

                    Toast.makeText(this, "Data berhasil dimuat", Toast.LENGTH_SHORT).show()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Fallback ke data statis jika parsing gagal
                    loadStaticJilidData()
                    updateJilidUI()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    loadStaticJilidData()
                    updateJilidUI()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Toast.makeText(this, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                // Fallback ke data statis jika gagal mengambil dari database
                loadStaticJilidData()
                updateJilidUI()
            }
        )

        // Tambahkan request ke queue
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun loadStaticJilidData() {
        // Kosongkan list terlebih dahulu
        jilidList.clear()

        // Data statis sebagai fallback (dari gambar)
        jilidList.add(Jilid(1, "JILID I", "Jilid pertama - pengenalan huruf hijaiyah dasar"))
        jilidList.add(Jilid(2, "JILID II", "Jilid kedua - lanjutan huruf hijaiyah"))
        jilidList.add(Jilid(3, "JILID III", "Jilid ketiga - huruf bersambung"))
        jilidList.add(Jilid(4, "JILID IV", "Jilid keempat - bacaan panjang pendek"))
        jilidList.add(Jilid(5, "JILID V", "Jilid kelima - hukum tajwid dasar"))
        jilidList.add(Jilid(6, "JILID VI", "Jilid keenam - hukum tajwid lanjutan"))
        jilidList.add(Jilid(7, "AL-QUR'AN", "Membaca Al-Qur'an"))
    }

    private fun updateJilidUI() {
        runOnUiThread {
            // Buat list TextView
            val textViews = listOf(
                jilid1Text,
                jilid2Text,
                jilid3Text,
                jilid4Text,
                jilid5Text,
                jilid6Text,
                alquranText
            )

            // Update teks pada setiap TextView
            for (i in textViews.indices) {
                if (i < jilidList.size) {
                    // PERHATIKAN: Gunakan nama_ijild bukan nama_jilid
                    textViews[i].text = jilidList[i].nama_jilid
                } else {
                    // Jika tidak ada data, tampilkan placeholder
                    textViews[i].text = "JILID ${i + 1}"
                }
            }

            // Buat list CardView
            val cardViews = listOf(
                jilid1Card,
                jilid2Card,
                jilid3Card,
                jilid4Card,
                jilid5Card,
                jilid6Card,
                alquranCard
            )

            // Update click listeners dengan data yang benar
            cardViews.forEachIndexed { index, cardView ->
                cardView.setOnClickListener {
                    if (index < jilidList.size) {
                        val jilid = jilidList[index]
                        navigateToManajemenSoal(jilid.id_jilid, jilid.nama_jilid)
                    } else {
                        Toast.makeText(
                            this@PilihJilidActivity,
                            "Data jilid belum tersedia",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            // Jika ada lebih dari 7 jilid, sembunyikan yang tidak terpakai
            // (Opsional, tergantung layout Anda)
            if (jilidList.size < 7) {
                for (i in jilidList.size until 7) {
                    cardViews[i].visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun navigateToManajemenSoal(idJilid: Int, namaJilid: String) {
        val intent = Intent(this, ManajemenSoalActivity::class.java)
        intent.putExtra("ID_JILID", idJilid)
        intent.putExtra("NAMA_JILID", namaJilid)
        startActivity(intent)
    }

    private fun navigateBackToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Tutup activity saat ini
    }
}