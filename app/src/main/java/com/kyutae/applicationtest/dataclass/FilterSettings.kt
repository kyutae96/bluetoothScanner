package com.kyutae.applicationtest.dataclass

/**
 * 스캔 필터 설정을 담는 데이터 클래스
 */
data class FilterSettings(
    // 이름 필터 (부분 일치)
    val nameFilter: String = "",

    // 최소 RSSI 값 (-100 ~ 0)
    val minRssi: Int = -100,

    // 특정 서비스 UUID 필터
    val serviceUuidFilter: String = "",

    // 연결 가능한 장치만 표시
    val connectableOnly: Boolean = false,

    // 필터 활성화 여부
    val enabled: Boolean = false
) {
    /**
     * 필터가 설정되어 있는지 확인
     */
    fun hasActiveFilters(): Boolean {
        return enabled && (
            nameFilter.isNotEmpty() ||
            minRssi > -100 ||
            serviceUuidFilter.isNotEmpty() ||
            connectableOnly
        )
    }
}
