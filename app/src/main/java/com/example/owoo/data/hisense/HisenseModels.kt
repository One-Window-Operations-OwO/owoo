package com.example.owoo.data.hisense

import com.google.gson.annotations.SerializedName

data class HisenseData(
    val isGreen: Boolean,
    val nextPath: String? = null,
    val schoolInfo: HisenseSchoolInfo? = null,
    val images: Map<String, String>? = null,
    val processHistory: List<HisenseProcessHistory>? = null,
    val q: String? = null,
    val npsn: String? = null,
    val iprop: String? = null,
    val ikab: String? = null,
    val ikec: String? = null,
    val iins: String? = null,
    val ijenjang: String? = null,
    val ibp: String? = null,
    val iss: String? = null,
    val isf: String? = null,
    val istt: String? = null,
    val itgl: String? = null,
    val itgla: String? = null,
    val itgle: String? = null,
    val ipet: String? = null,
    val ihnd: String? = null,
    val error: String? = null,
)

data class HisenseSchoolInfo(
    @SerializedName("NPSN")
    val npsn: String? = null,
    @SerializedName("Nama")
    val nama: String? = null,
    @SerializedName("Alamat")
    val alamat: String? = null,
    @SerializedName("Provinsi")
    val provinsi: String? = null,
    @SerializedName("Kabupaten")
    val kabupaten: String? = null,
    @SerializedName("Kecamatan")
    val kecamatan: String? = null,
    @SerializedName("Kelurahan/Desa")
    val kelurahanDesa: String? = null,
    @SerializedName("Jenjang")
    val jenjang: String? = null,
    @SerializedName("Bentuk")
    val bentuk: String? = null,
    @SerializedName("Sekolah")
    val sekolah: String? = null,
    @SerializedName("Formal")
    val formal: String? = null,
    @SerializedName("PIC")
    val pic: String? = null,
    @SerializedName("Telp PIC")
    val telpPic: String? = null,
    @SerializedName("Resi Pengiriman")
    val resiPengiriman: String? = null,
    @SerializedName("Serial Number")
    val serialNumber: String? = null,
    @SerializedName("Status")
    val status: String? = null
)

data class HisenseProcessHistory(
    val tanggal: String? = null,
    val status: String? = null,
    val keterangan: String? = null
)