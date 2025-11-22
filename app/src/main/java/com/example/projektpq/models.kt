package com.example.projektpq

// Data class untuk list santri
data class Santri(
    val no_induk: String,
    val nama: String
)

// Data class untuk detail santri
data class SantriDetail(
    val no_induk: String,
    val nama: String,
    val tempat_tanggal_lahir: String,
    val alamat: String,
    val jilid: String,
    val nik: String,
    val no_kk: String,
    val tahun_masuk: String,
    val keterangan: String
)