package com.example.owoo.util

data class EvaluationField(
    val col: String,
    val label: String,
    val options: List<String>
)

object EvaluationConstants {

    val defaultEvaluationValues: Map<String, String> = mapOf(
        "J" to "Sesuai",
        "K" to "Sesuai",
        "L" to "Sesuai",
        "M" to "Sesuai",
        "N" to "Sesuai",
        "P" to "Sesuai",
        "Q" to "Lengkap",
        "S" to "Konsisten",
        "T" to "Sesuai",
        "U" to "Lengkap",
        "V" to "Ada",
        "W" to "Ya",
        "X" to ""
    )

    val evaluationFields: List<EvaluationField> = listOf(
        EvaluationField("J", "GEO TAGGING", listOf("Sesuai", "Tidak Sesuai")),
        EvaluationField("K", "FOTO PAPAN NAMA", listOf("Sesuai", "Tidak Sesuai")),
        EvaluationField("L", "FOTO BOX & PIC", listOf("Sesuai", "Tidak Sesuai")),
        EvaluationField("M", "FOTO KELENGKAPAN UNIT", listOf("Sesuai", "Tidak Sesuai")),
        EvaluationField(
            "N",
            "FOTO SERIAL NUMBER",
            listOf("Sesuai", "Tidak Sesuai", "Tidak Ada", "Tidak Terlihat", "Diedit")
        ),
        EvaluationField("P", "BARCODE BAPP", listOf("Sesuai", "Tidak Sesuai")),
        EvaluationField(
            "Q",
            "CEKLIS BAPP HAL 1",
            listOf(
                "Lengkap",
                "Tidak Lengkap",
                "Tidak Sesuai",
                "BAPP Tidak Jelas",
                "Surat Tugas Tidak Ada",
                "Diedit",
                "Tanggal Tidak Ada"
            )
        ),
        EvaluationField(
            "S",
            "NAMA PENANDATANGANAN BAPP",
            listOf(
                "Konsisten",
                "Tidak Konsisten",
                "Tidak Terdaftar di Datadik",
                "PIC Tidak Sama",
                "TTD Tidak Ada",
                "NIP Tidak Ada"
            )
        ),
        EvaluationField(
            "T",
            "STEMPEL",
            listOf("Sesuai", "Tidak Sesuai", "Tidak Ada", "Tidak Sesuai Tempatnya")
        ),
        EvaluationField(
            "U",
            "CEKLIS BAPP HAL 2",
            listOf(
                "Lengkap",
                "Tidak Lengkap",
                "Tidak Sesuai",
                "BAPP Tidak Jelas",
                "Diedit",
                "Tanggal Tidak Ada",
                "Tanggal Tidak Konsisten"
            )
        ),
        EvaluationField("V", "PESERTA PELATIHAN", listOf("Ada", "Tidak Ada", "Media Pelatihan")),
        EvaluationField("W", "KESIMPULAN LENGKAP", listOf("Ya", "Tidak")),
        EvaluationField(
            col = "X",
            label = "TANGGAL INSTALASI SELESAI",
            options = emptyList()
        )

    )
}
