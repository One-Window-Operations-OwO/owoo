package com.example.owoo.util

import android.content.Context
import com.example.owoo.network.RowWithIndex
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class CachedData(
    val header: List<String>,
    val rows: List<RowWithIndex>
)

class CacheManager(context: Context) {

    private val cacheFile = File(context.cacheDir, "pending_rows.json")
    private val gson = Gson()

    fun savePendingRows(cachedData: CachedData) {
        val json = gson.toJson(cachedData)
        cacheFile.writeText(json)
    }

    fun loadPendingRows(): CachedData? {
        if (!cacheFile.exists()) {
            return null
        }
        val json = cacheFile.readText()
        val type = object : TypeToken<CachedData>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearCache() {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }
}