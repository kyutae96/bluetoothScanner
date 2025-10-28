package com.kyutae.applicationtest.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kyutae.applicationtest.database.DeviceHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 데이터 내보내기 유틸리티
 */
object DataExporter {

    /**
     * CSV 형식으로 내보내기
     */
    suspend fun exportToCsv(
        context: Context,
        devices: List<DeviceHistoryEntity>,
        onComplete: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "ble_devices_${System.currentTimeMillis()}.csv")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                file.bufferedWriter().use { writer ->
                    // CSV 헤더
                    writer.write("Address,Name,RSSI,First Seen,Last Seen,Scan Count,Favorite,Manufacturer,Service UUIDs\n")

                    // 데이터 행
                    devices.forEach { device ->
                        writer.write("${device.address},")
                        writer.write("\"${device.name ?: "Unknown"}\",")
                        writer.write("${device.lastRssi},")
                        writer.write("\"${dateFormat.format(Date(device.firstSeen))}\",")
                        writer.write("\"${dateFormat.format(Date(device.lastSeen))}\",")
                        writer.write("${device.scanCount},")
                        writer.write("${device.isFavorite},")
                        writer.write("\"${device.manufacturer ?: ""}\",")
                        writer.write("\"${device.lastServiceUuids ?: ""}\"\n")
                    }
                }

                withContext(Dispatchers.Main) {
                    onComplete(file)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    /**
     * JSON 형식으로 내보내기
     */
    suspend fun exportToJson(
        context: Context,
        devices: List<DeviceHistoryEntity>,
        onComplete: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "ble_devices_${System.currentTimeMillis()}.json")
                val gson: Gson = GsonBuilder().setPrettyPrinting().create()

                val exportData = mapOf(
                    "exportTime" to System.currentTimeMillis(),
                    "deviceCount" to devices.size,
                    "devices" to devices.map { device ->
                        mapOf(
                            "address" to device.address,
                            "name" to (device.name ?: "Unknown"),
                            "lastRssi" to device.lastRssi,
                            "firstSeen" to device.firstSeen,
                            "lastSeen" to device.lastSeen,
                            "scanCount" to device.scanCount,
                            "isFavorite" to device.isFavorite,
                            "manufacturer" to (device.manufacturer ?: ""),
                            "serviceUuids" to (device.lastServiceUuids ?: "")
                        )
                    }
                )

                file.writeText(gson.toJson(exportData))

                withContext(Dispatchers.Main) {
                    onComplete(file)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    /**
     * 파일 공유
     */
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.name.endsWith(".csv") -> "text/csv"
                file.name.endsWith(".json") -> "application/json"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "데이터 공유"))
    }
}
