package com.kyutae.applicationtest.database

import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 장치 데이터 저장소
 */
class DeviceRepository(private val deviceHistoryDao: DeviceHistoryDao) {

    val allDevices: LiveData<List<DeviceHistoryEntity>> = deviceHistoryDao.getAllDevices()
    val favoriteDevices: LiveData<List<DeviceHistoryEntity>> = deviceHistoryDao.getFavoriteDevices()

    /**
     * 스캔 결과를 히스토리에 저장 또는 업데이트
     */
    suspend fun addOrUpdateDevice(scanResult: ScanResult) {
        withContext(Dispatchers.IO) {
            val address = scanResult.device.address
            val existingDevice = deviceHistoryDao.getDeviceByAddress(address)

            if (existingDevice != null) {
                // 기존 장치 업데이트 (스캔 횟수 증가)
                deviceHistoryDao.incrementScanCount(
                    address = address,
                    timestamp = System.currentTimeMillis(),
                    rssi = scanResult.rssi
                )
            } else {
                // 새로운 장치 추가
                val serviceUuids = scanResult.scanRecord?.serviceUuids?.joinToString(",") { it.toString() }
                val manufacturerData = scanResult.scanRecord?.manufacturerSpecificData?.let {
                    if (it.size() > 0) {
                        val key = it.keyAt(0)
                        "ID: $key"
                    } else null
                }

                val newDevice = DeviceHistoryEntity(
                    address = address,
                    name = scanResult.device.name,
                    lastRssi = scanResult.rssi,
                    lastSeen = System.currentTimeMillis(),
                    firstSeen = System.currentTimeMillis(),
                    scanCount = 1,
                    lastServiceUuids = serviceUuids,
                    manufacturer = manufacturerData
                )
                deviceHistoryDao.insert(newDevice)
            }
        }
    }

    /**
     * 즐겨찾기 토글
     */
    suspend fun toggleFavorite(address: String) {
        withContext(Dispatchers.IO) {
            val device = deviceHistoryDao.getDeviceByAddress(address)
            device?.let {
                deviceHistoryDao.updateFavoriteStatus(address, !it.isFavorite)
            }
        }
    }

    /**
     * 즐겨찾기 상태 확인
     */
    suspend fun isFavorite(address: String): Boolean {
        return withContext(Dispatchers.IO) {
            deviceHistoryDao.getDeviceByAddress(address)?.isFavorite ?: false
        }
    }

    /**
     * 연결 시간 업데이트
     */
    suspend fun updateConnectionTime(address: String) {
        withContext(Dispatchers.IO) {
            deviceHistoryDao.updateLastConnectedTime(address, System.currentTimeMillis())
        }
    }

    /**
     * 오래된 히스토리 삭제 (30일)
     */
    suspend fun cleanOldHistory() {
        withContext(Dispatchers.IO) {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            deviceHistoryDao.deleteOldHistory(thirtyDaysAgo)
        }
    }

    /**
     * 모든 히스토리 삭제
     */
    suspend fun clearAllHistory() {
        withContext(Dispatchers.IO) {
            deviceHistoryDao.clearAllHistory()
        }
    }

    /**
     * 장치 검색
     */
    fun searchDevices(query: String): LiveData<List<DeviceHistoryEntity>> {
        return deviceHistoryDao.searchDevicesByName(query)
    }
}
