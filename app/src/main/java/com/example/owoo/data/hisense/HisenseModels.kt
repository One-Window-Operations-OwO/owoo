package com.example.owoo.data.hisense

data class HisenseData(
    val isGreen: Boolean,
    val schoolInfo: Map<String, String>,
    val images: Map<String, String>,
    val processHistory: List<ProcessHistoryItem>,
    val q: String,
    val npsn: String,
    val iprop: String,
    val ikab: String,
    val ikec: String,
    val iins: String,
    val ijenjang: String,
    val ibp: String,
    val iss: String,
    val isf: String,
    val istt: String,
    val itgl: String,
    val itgla: String,
    val itgle: String,
    val ipet: String,
    val ihnd: String
)

data class ProcessHistoryItem(
    val tanggal: String,
    val status: String,
    val keterangan: String
)
