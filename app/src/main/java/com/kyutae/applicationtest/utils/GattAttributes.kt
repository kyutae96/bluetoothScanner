package com.kyutae.applicationtest.utils

import java.util.UUID

/**
 * 표준 GATT 서비스 및 특성 UUID를 사람이 읽을 수 있는 이름으로 변환하는 유틸리티
 * 블루투스 SIG(Special Interest Group)에서 정의한 표준 UUID 기반
 */
object GattAttributes {

    /**
     * UUID를 사람이 읽을 수 있는 이름으로 변환
     * @param uuid UUID 문자열 또는 UUID 객체
     * @return 서비스 또는 특성의 이름, 알 수 없는 경우 UUID 반환
     */
    fun lookup(uuid: String): String {
        return GATT_SERVICES[uuid.lowercase()]
            ?: GATT_CHARACTERISTICS[uuid.lowercase()]
            ?: uuid
    }

    fun lookup(uuid: UUID): String {
        return lookup(uuid.toString())
    }

    /**
     * 짧은 UUID를 전체 UUID로 변환
     * Bluetooth SIG는 16비트 UUID를 128비트로 확장: 0000XXXX-0000-1000-8000-00805f9b34fb
     */
    fun from16BitUuid(shortUuid: String): String {
        return "0000${shortUuid.lowercase()}-0000-1000-8000-00805f9b34fb"
    }

    /**
     * 서비스 또는 특성이 표준 GATT인지 확인
     */
    fun isStandardGatt(uuid: String): Boolean {
        return GATT_SERVICES.containsKey(uuid.lowercase()) ||
               GATT_CHARACTERISTICS.containsKey(uuid.lowercase())
    }

    /**
     * 표준 GATT 서비스 UUID 맵핑
     * UUID -> 서비스 이름
     */
    private val GATT_SERVICES = hashMapOf(
        // Generic Services
        "00001800-0000-1000-8000-00805f9b34fb" to "Generic Access",
        "00001801-0000-1000-8000-00805f9b34fb" to "Generic Attribute",

        // Device Information
        "0000180a-0000-1000-8000-00805f9b34fb" to "Device Information",
        "0000180f-0000-1000-8000-00805f9b34fb" to "Battery Service",

        // Health & Fitness
        "0000180d-0000-1000-8000-00805f9b34fb" to "Heart Rate",
        "00001810-0000-1000-8000-00805f9b34fb" to "Blood Pressure",
        "00001808-0000-1000-8000-00805f9b34fb" to "Glucose",
        "00001809-0000-1000-8000-00805f9b34fb" to "Health Thermometer",
        "0000181c-0000-1000-8000-00805f9b34fb" to "User Data",
        "0000181d-0000-1000-8000-00805f9b34fb" to "Weight Scale",

        // Automation & Control
        "00001805-0000-1000-8000-00805f9b34fb" to "Current Time",
        "00001806-0000-1000-8000-00805f9b34fb" to "Reference Time Update",
        "00001807-0000-1000-8000-00805f9b34fb" to "Next DST Change",
        "00001811-0000-1000-8000-00805f9b34fb" to "Alert Notification",
        "00001802-0000-1000-8000-00805f9b34fb" to "Immediate Alert",
        "00001803-0000-1000-8000-00805f9b34fb" to "Link Loss",
        "00001804-0000-1000-8000-00805f9b34fb" to "Tx Power",

        // Sensors
        "00001814-0000-1000-8000-00805f9b34fb" to "Running Speed and Cadence",
        "00001816-0000-1000-8000-00805f9b34fb" to "Cycling Speed and Cadence",
        "00001818-0000-1000-8000-00805f9b34fb" to "Cycling Power",
        "00001819-0000-1000-8000-00805f9b34fb" to "Location and Navigation",
        "0000181a-0000-1000-8000-00805f9b34fb" to "Environmental Sensing",
        "0000181b-0000-1000-8000-00805f9b34fb" to "Body Composition",

        // Audio
        "0000183b-0000-1000-8000-00805f9b34fb" to "Audio Stream Control Service",
        "00001844-0000-1000-8000-00805f9b34fb" to "Volume Control",
        "00001850-0000-1000-8000-00805f9b34fb" to "Audio Input Control",

        // Custom Services (앱 특정)
        "0000d0c0-0000-1000-8000-00805f9b34fb" to "토닥 서비스",
    )

    /**
     * 표준 GATT 특성 UUID 맵핑
     * UUID -> 특성 이름
     */
    private val GATT_CHARACTERISTICS = hashMapOf(
        // Generic Access
        "00002a00-0000-1000-8000-00805f9b34fb" to "Device Name",
        "00002a01-0000-1000-8000-00805f9b34fb" to "Appearance",
        "00002a04-0000-1000-8000-00805f9b34fb" to "Peripheral Preferred Connection Parameters",
        "00002aa6-0000-1000-8000-00805f9b34fb" to "Central Address Resolution",

        // Device Information
        "00002a29-0000-1000-8000-00805f9b34fb" to "Manufacturer Name",
        "00002a24-0000-1000-8000-00805f9b34fb" to "Model Number",
        "00002a25-0000-1000-8000-00805f9b34fb" to "Serial Number",
        "00002a27-0000-1000-8000-00805f9b34fb" to "Hardware Revision",
        "00002a26-0000-1000-8000-00805f9b34fb" to "Firmware Revision",
        "00002a28-0000-1000-8000-00805f9b34fb" to "Software Revision",
        "00002a23-0000-1000-8000-00805f9b34fb" to "System ID",
        "00002a2a-0000-1000-8000-00805f9b34fb" to "IEEE 11073-20601 Regulatory Certification",
        "00002a50-0000-1000-8000-00805f9b34fb" to "PnP ID",

        // Battery
        "00002a19-0000-1000-8000-00805f9b34fb" to "Battery Level",
        "00002a1a-0000-1000-8000-00805f9b34fb" to "Battery Power State",
        "00002a1b-0000-1000-8000-00805f9b34fb" to "Battery Level State",

        // Heart Rate
        "00002a37-0000-1000-8000-00805f9b34fb" to "Heart Rate Measurement",
        "00002a38-0000-1000-8000-00805f9b34fb" to "Body Sensor Location",
        "00002a39-0000-1000-8000-00805f9b34fb" to "Heart Rate Control Point",

        // Blood Pressure
        "00002a35-0000-1000-8000-00805f9b34fb" to "Blood Pressure Measurement",
        "00002a36-0000-1000-8000-00805f9b34fb" to "Intermediate Cuff Pressure",
        "00002a49-0000-1000-8000-00805f9b34fb" to "Blood Pressure Feature",

        // Glucose
        "00002a18-0000-1000-8000-00805f9b34fb" to "Glucose Measurement",
        "00002a34-0000-1000-8000-00805f9b34fb" to "Glucose Measurement Context",
        "00002a51-0000-1000-8000-00805f9b34fb" to "Glucose Feature",
        "00002a52-0000-1000-8000-00805f9b34fb" to "Record Access Control Point",

        // Temperature
        "00002a1c-0000-1000-8000-00805f9b34fb" to "Temperature Measurement",
        "00002a1d-0000-1000-8000-00805f9b34fb" to "Temperature Type",
        "00002a1e-0000-1000-8000-00805f9b34fb" to "Intermediate Temperature",
        "00002a21-0000-1000-8000-00805f9b34fb" to "Measurement Interval",

        // Alert
        "00002a06-0000-1000-8000-00805f9b34fb" to "Alert Level",
        "00002a46-0000-1000-8000-00805f9b34fb" to "New Alert",
        "00002a47-0000-1000-8000-00805f9b34fb" to "Unread Alert Status",
        "00002a44-0000-1000-8000-00805f9b34fb" to "Alert Notification Control Point",
        "00002a45-0000-1000-8000-00805f9b34fb" to "Alert Category ID",
        "00002a48-0000-1000-8000-00805f9b34fb" to "Supported New Alert Category",
        "00002a42-0000-1000-8000-00805f9b34fb" to "Alert Category ID Bit Mask",

        // Time
        "00002a2b-0000-1000-8000-00805f9b34fb" to "Current Time",
        "00002a0f-0000-1000-8000-00805f9b34fb" to "Local Time Information",
        "00002a14-0000-1000-8000-00805f9b34fb" to "Reference Time Information",
        "00002a16-0000-1000-8000-00805f9b34fb" to "Time Update Control Point",
        "00002a17-0000-1000-8000-00805f9b34fb" to "Time Update State",

        // Cycling & Running
        "00002a5b-0000-1000-8000-00805f9b34fb" to "CSC Measurement",
        "00002a5c-0000-1000-8000-00805f9b34fb" to "CSC Feature",
        "00002a53-0000-1000-8000-00805f9b34fb" to "RSC Measurement",
        "00002a54-0000-1000-8000-00805f9b34fb" to "RSC Feature",

        // Environmental
        "00002a6e-0000-1000-8000-00805f9b34fb" to "Temperature",
        "00002a6f-0000-1000-8000-00805f9b34fb" to "Humidity",
        "00002a6d-0000-1000-8000-00805f9b34fb" to "Pressure",
        "00002a76-0000-1000-8000-00805f9b34fb" to "UV Index",

        // Generic
        "00002a05-0000-1000-8000-00805f9b34fb" to "Service Changed",
        "00002a07-0000-1000-8000-00805f9b34fb" to "Tx Power Level",
    )

    /**
     * 서비스 카테고리 가져오기
     */
    fun getServiceCategory(uuid: String): String {
        return when {
            uuid.lowercase().startsWith("00001800") || uuid.lowercase().startsWith("00001801") ->
                "Generic"
            uuid.lowercase().startsWith("0000180a") || uuid.lowercase().startsWith("0000180f") ->
                "Device Information"
            uuid.lowercase().startsWith("0000180d") || uuid.lowercase().startsWith("00001810") ||
            uuid.lowercase().startsWith("00001808") || uuid.lowercase().startsWith("00001809") ->
                "Health & Fitness"
            uuid.lowercase().startsWith("0000181") ->
                "Sensor"
            uuid.lowercase().startsWith("0000183") || uuid.lowercase().startsWith("00001844") ||
            uuid.lowercase().startsWith("00001850") ->
                "Audio"
            else -> "Custom"
        }
    }
}
