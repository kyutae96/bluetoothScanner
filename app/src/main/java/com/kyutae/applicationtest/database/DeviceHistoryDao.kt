package com.kyutae.applicationtest.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * 장치 히스토리 데이터 접근 객체
 */
@Dao
interface DeviceHistoryDao {

    /**
     * 장치 정보 삽입 또는 업데이트
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceHistoryEntity)

    /**
     * 장치 정보 업데이트
     */
    @Update
    suspend fun update(device: DeviceHistoryEntity)

    /**
     * 장치 삭제
     */
    @Delete
    suspend fun delete(device: DeviceHistoryEntity)

    /**
     * 주소로 장치 조회
     */
    @Query("SELECT * FROM device_history WHERE address = :address")
    suspend fun getDeviceByAddress(address: String): DeviceHistoryEntity?

    /**
     * 모든 장치 조회 (마지막 발견 시간 기준 내림차순)
     */
    @Query("SELECT * FROM device_history ORDER BY lastSeen DESC")
    fun getAllDevices(): LiveData<List<DeviceHistoryEntity>>

    /**
     * 즐겨찾기 장치 조회
     */
    @Query("SELECT * FROM device_history WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteDevices(): LiveData<List<DeviceHistoryEntity>>

    /**
     * 즐겨찾기 토글
     */
    @Query("UPDATE device_history SET isFavorite = :isFavorite WHERE address = :address")
    suspend fun updateFavoriteStatus(address: String, isFavorite: Boolean)

    /**
     * 마지막 연결 시간 업데이트
     */
    @Query("UPDATE device_history SET lastConnectedTime = :timestamp WHERE address = :address")
    suspend fun updateLastConnectedTime(address: String, timestamp: Long)

    /**
     * 스캔 횟수 증가
     */
    @Query("UPDATE device_history SET scanCount = scanCount + 1, lastSeen = :timestamp, lastRssi = :rssi WHERE address = :address")
    suspend fun incrementScanCount(address: String, timestamp: Long, rssi: Int)

    /**
     * 오래된 히스토리 삭제 (30일 이상 안 본 장치)
     */
    @Query("DELETE FROM device_history WHERE isFavorite = 0 AND lastSeen < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)

    /**
     * 모든 히스토리 삭제 (즐겨찾기 제외)
     */
    @Query("DELETE FROM device_history WHERE isFavorite = 0")
    suspend fun clearAllHistory()

    /**
     * 이름으로 검색
     */
    @Query("SELECT * FROM device_history WHERE name LIKE '%' || :query || '%' ORDER BY lastSeen DESC")
    fun searchDevicesByName(query: String): LiveData<List<DeviceHistoryEntity>>
}
