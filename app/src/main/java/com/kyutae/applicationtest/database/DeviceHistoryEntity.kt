package com.kyutae.applicationtest.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 장치 히스토리 및 즐겨찾기 정보를 저장하는 Entity
 */
@Entity(tableName = "device_history")
data class DeviceHistoryEntity(
    @PrimaryKey
    val address: String,  // MAC 주소 (Primary Key)

    val name: String?,    // 장치 이름

    val lastRssi: Int,    // 마지막 RSSI 값

    val lastSeen: Long,   // 마지막 발견 시간 (timestamp)

    val firstSeen: Long,  // 처음 발견 시간 (timestamp)

    val scanCount: Int,   // 스캔된 횟수

    val isFavorite: Boolean = false,  // 즐겨찾기 여부

    val lastServiceUuids: String? = null,  // 마지막 서비스 UUID들 (JSON 형식)

    val manufacturer: String? = null,  // 제조사 정보

    val lastConnectedTime: Long? = null  // 마지막 연결 시간
)
