package com.example.owoo.util

object RejectionHelper {

    val rejectionReasons: Map<String, String> = mapOf(
        "J" to "(5A) Geo Tagging tidak sesuai",
        "K" to "(4A) Foto plang sekolah tidak sesuai",
        "L" to "(4C) Foto Box dan PIC tidak sesuai",
        "M" to "(2A) Foto kelengkapan IFP tidak lengkap (Kabel HDMI; USB type A to B, stylus, remote)",
        "N" to "(3B) Serial number yang diinput tidak sesuai dengan yang tertera pada IFP",
        "P" to "(1L) Data BAPP sekolah tidak sesuai (cek Barcode atas dan NPSN dengan foto sekolah atau NPSN yang diinput)",
        "Q" to "(1D) Ceklis BAPP tidak lengkap pada halaman 1",
        "S" to "(1K) Data penanda tangan pada halaman 1 dan halaman 2 BAPP tidak konsisten",
        "T" to "(1O) Stempel pada BAPP halaman 2 tidak sesuai dengan sekolahnya",
        "U" to "(1Q) Ceklis pada BAPP halaman 2 tidak lengkap",
        "V" to "(1S) Satuan Pendidikan yang Mengikuti Pelatihan, tidak ada dalam BAPP hal.2",
        "W" to "(1A) Simpulan BAPP pada hal 2 belum dipilih atau dicoret"
    )

    val specificReasons: Map<String, Map<String, String>> = mapOf(
        "N" to mapOf(
            "Tidak Terlihat" to "(3A) Foto serial number pada belakang unit IFP tidak jelas",
            "Tidak Ada" to "(3C) Foto Serial Number pada belakang unit IFP tidak ada",
            "Diedit" to "(1AB) Foto serial number tidak boleh diedit digital"
        ),
        "Q" to mapOf(
            "Tidak Sesuai" to "(1D) Ceklis BAPP tidak sesuai pada halaman 1",
            "BAPP Tidak Jelas" to "(1M) BAPP Halaman 1 tidak terlihat jelas",
            "Surat Tugas Tidak Ada" to "(1V) Nomor surat tugas pada halaman 1 tidak ada",
            "Diedit" to "(1Y) BAPP Hal 1 tidak boleh diedit digital",
            "Tanggal Tidak Ada" to "(1F) Tanggal BAPP tidak diisi"
        ),
        "S" to mapOf(
            "Tidak Terdaftar di Datadik" to "(1C) Pihak sekolah yang menandatangani BAPP tidak terdaftar dalam data Dapodik",
            "PIC Tidak Sama" to "(1U) PIC dari pihak sekolah berbeda dengan yang di BAPP",
            "TTD Tidak Ada" to "(1X) Tidak ada tanda tangan dari pihak sekolah",
            "NIP Tidak Ada" to "(1AA) NIP penandatangan pihak sekolah tidak ada"
        ),
        "T" to mapOf(
            "Tidak Ada" to "(1B) Tidak ada stempel sekolah pada BAPP",
            "Tidak Sesuai Tempatnya" to "(1W) Stempel tidak mengenai tanda tangan pihak sekolah"
        ),
        "U" to mapOf(
            "Tidak Sesuai" to "(1Q) Ceklis BAPP tidak sesuai pada halaman 2",
            "BAPP Tidak Jelas" to "(1T) BAPP Halaman 2 tidak terlihat jelas",
            "Diedit" to "(1Z) BAPP Hal 2 tidak boleh diedit digital",
            "Tanggal Tidak Ada" to "(1F) Tanggal BAPP tidak diisi"
        ),
        "V" to mapOf(
            "Media Pelatihan" to "(1AC) Harap ceklis di luar jaringan pada media pelatihan (jangan double ceklis/tidak ceklis)"
        )
    )

    /**
     * Generates a rejection message based on a main reason key and an optional specific reason.
     * @param mainKey The main rejection reason key (e.g., "N", "Q").
     * @param specificReason The specific reason string (e.g., "Tidak Terlihat", "Diedit").
     * @return The formatted rejection message, or null if not found.
     */
    fun getRejectionMessage(mainKey: String, specificReason: String?): String? {
        if (specificReason != null) {
            val specificMessage = specificReasons[mainKey]?.get(specificReason)
            if (specificMessage != null) {
                return specificMessage
            }
        }
        return rejectionReasons[mainKey]
    }
}
