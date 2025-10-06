package com.example.owoo.data.datadik

data class DatadikData(
    val id: String,
    val name: String,
    val address: String,
    val kecamatan: String,
    val kabupaten: String,
    val provinsi: String,
    val kepalaSekolah: String,
    val ptk: List<PtkItem>
)

data class PtkItem(
    val ptk_id: String,
    val nama: String,
    val jenis_ptk: String,
    val jabatan_ptk: String
)
