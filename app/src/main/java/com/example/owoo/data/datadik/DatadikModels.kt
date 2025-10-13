package com.example.owoo.data.datadik

import com.google.gson.annotations.SerializedName

data class Ptk(
    @SerializedName("ptk_terdaftar_id")
    val ptkTerdaftarId: String? = null,
    @SerializedName("ptk_id")
    val ptkId: String? = null,
    val nama: String? = null,
    @SerializedName("jenis_kelamin")
    val jenisKelamin: String? = null,
    @SerializedName("tanggal_lahir")
    val tanggalLahir: String? = null,
    val nik: String? = null,
    val nuptk: String? = null,
    val nip: String? = null,
    val nrg: String? = null,
    val kepegawaian: String? = null,
    @SerializedName("jenis_ptk")
    val jenisPtk: String? = null,
    @SerializedName("jabatan_ptk")
    val jabatanPtk: String? = null,
    @SerializedName("nomor_surat_tugas")
    val nomorSuratTugas: String? = null,
    @SerializedName("tanggal_surat_tugas")
    val tanggalSuratTugas: String? = null,
    @SerializedName("tmt_tugas")
    val tmtTugas: String? = null,
    @SerializedName("ptk_induk")
    val ptkInduk: String? = null,
    @SerializedName("last_update")
    val lastUpdate: String? = null
)

data class DatadikData(
    val id: String? = null,
    val name: String? = null,
    val address: String? = null,
    val kecamatan: String? = null,
    val kabupaten: String? = null,
    val provinsi: String? = null,
    val kepalaSekolah: String? = null,
    val ptk: List<Ptk>? = null,
    val error: String? = null
)