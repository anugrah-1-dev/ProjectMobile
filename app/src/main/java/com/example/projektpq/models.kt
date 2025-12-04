package com.example.projektpq.models

import java.util.Date

// ==================== SANTRI DETAIL ====================
data class SantriDetail(
    val no_induk: String,
    val nama: String,
    val tempat_lahir: String? = null,
    val tanggal_lahir: String? = null,
    val alamat: String? = null,
    val jilid: String? = null,
    val nik: String? = null,
    val no_kk: String? = null,
    val tahun_masuk: String? = null,
    val keterangan: String? = null
)

// ==================== SANTRI RESPONSE ====================
data class SantriResponse(
    val success: Boolean,
    val message: String,
    val data: SantriDetail? = null
)

// ==================== JILID ====================
data class Jilid(
    val id_jilid: Int,
    val nama_jilid: String,
    val deskripsi: String? = null
)

// ==================== SOAL ====================
data class Soal(
    val id_soal: Int,
    val id_jilid: Int,
    val nomor_soal: Int,
    val isi_soal: String,
    val tipe_soal: String = "praktek",
    val bobot_nilai: Int = 25
)

// ==================== UJIAN (DIPERBAIKI) ====================
// PERUBAHAN: Ganti id_santri dengan no_induk untuk sesuai dengan database
data class Ujian(
    val id_ujian: Int = 0,
    val no_induk: String,  // GANTI: dari id_santri: Int ke no_induk: String
    val id_jilid: Int,
    val tanggal_ujian: Date,
    val nilai_total: Double = 0.0,
    val status: String = "belum_selesai"
)

// ==================== JAWABAN UJIAN ====================
data class JawabanUjian(
    val id_jawaban: Int = 0,
    val id_ujian: Int,
    val id_soal: Int,
    val nilai: Double,
    val catatan: String? = null
)

// ==================== RESPONSE CLASSES ====================
data class JilidResponse(
    val success: Boolean,
    val message: String,
    val data: List<Jilid>? = null
)

data class SoalResponse(
    val success: Boolean,
    val message: String,
    val data: List<Soal>? = null
)

data class UjianResponse(
    val success: Boolean,
    val message: String,
    val data: Ujian? = null
)

data class JawabanResponse(
    val success: Boolean,
    val message: String
)

// ==================== HASIL UJIAN ====================
data class HasilUjian(
    val id_ujian: Int,
    val id_santri: Int,
    val nama_santri: String,
    val NO_KK: String,
    val nama_jilid: String,
    val tanggal_ujian: Date,
    val nilai_total: Double,
    val status: String,
    val jumlah_soal_dijawab: Int,
    val total_soal: Int
)

// ==================== GRAFIK DATA ====================
data class GrafikNilai(
    val nama_santri: String,
    val nama_jilid: String,
    val bulan: Int,
    val tahun: Int,
    val rata_rata_nilai: Double
)