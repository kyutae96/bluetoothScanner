package com.kyutae.applicationtest.utils

import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * iBeacon 및 Eddystone 비콘 파싱 유틸리티
 */
object BeaconParser {

    private const val TAG = "BeaconParser"

    // iBeacon 관련 상수
    private const val IBEACON_MANUFACTURER_ID = 0x004C // Apple
    private const val IBEACON_TYPE = 0x02
    private const val IBEACON_LENGTH = 0x15

    /**
     * iBeacon 데이터 클래스
     */
    data class IBeaconData(
        val uuid: String,
        val major: Int,
        val minor: Int,
        val txPower: Int
    )

    /**
     * Eddystone 데이터 클래스
     */
    data class EddystoneData(
        val frameType: String,
        val url: String? = null,
        val namespace: String? = null,
        val instance: String? = null,
        val txPower: Int? = null,
        val tlmData: String? = null
    )

    /**
     * 비콘 타입
     */
    enum class BeaconType {
        IBEACON,
        EDDYSTONE_UID,
        EDDYSTONE_URL,
        EDDYSTONE_TLM,
        UNKNOWN
    }

    /**
     * 스캔 결과에서 비콘 타입 감지
     */
    fun detectBeaconType(scanResult: ScanResult): BeaconType {
        val scanRecord = scanResult.scanRecord ?: return BeaconType.UNKNOWN

        // iBeacon 체크
        if (parseIBeacon(scanResult) != null) {
            return BeaconType.IBEACON
        }

        // Eddystone 체크
        val serviceUuids = scanRecord.serviceUuids
        if (serviceUuids != null && serviceUuids.any { it.toString().startsWith("0000feaa") }) {
            val serviceData = scanRecord.getServiceData(
                ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")
            )
            serviceData?.let {
                if (it.isNotEmpty()) {
                    return when (it[0].toInt()) {
                        0x00 -> BeaconType.EDDYSTONE_UID
                        0x10 -> BeaconType.EDDYSTONE_URL
                        0x20 -> BeaconType.EDDYSTONE_TLM
                        else -> BeaconType.UNKNOWN
                    }
                }
            }
        }

        return BeaconType.UNKNOWN
    }

    /**
     * iBeacon 데이터 파싱
     */
    fun parseIBeacon(scanResult: ScanResult): IBeaconData? {
        val scanRecord = scanResult.scanRecord ?: return null
        val manufacturerData = scanRecord.manufacturerSpecificData

        for (i in 0 until manufacturerData.size()) {
            val manufacturerId = manufacturerData.keyAt(i)
            if (manufacturerId == IBEACON_MANUFACTURER_ID) {
                val data = manufacturerData.get(manufacturerId)

                // iBeacon 데이터 검증 (최소 23바이트)
                if (data.size < 23) continue
                if (data[0].toInt() != IBEACON_TYPE || data[1].toInt() != IBEACON_LENGTH) continue

                try {
                    // UUID 파싱 (16바이트)
                    val uuidBytes = data.copyOfRange(2, 18)
                    val uuid = UUID(
                        ByteBuffer.wrap(uuidBytes.copyOfRange(0, 8)).order(ByteOrder.BIG_ENDIAN).long,
                        ByteBuffer.wrap(uuidBytes.copyOfRange(8, 16)).order(ByteOrder.BIG_ENDIAN).long
                    ).toString()

                    // Major (2바이트)
                    val major = ((data[18].toInt() and 0xFF) shl 8) or (data[19].toInt() and 0xFF)

                    // Minor (2바이트)
                    val minor = ((data[20].toInt() and 0xFF) shl 8) or (data[21].toInt() and 0xFF)

                    // TX Power (1바이트, signed)
                    val txPower = data[22].toInt()

                    return IBeaconData(uuid, major, minor, txPower)
                } catch (e: Exception) {
                    Log.e(TAG, "iBeacon parsing error: ${e.message}")
                }
            }
        }

        return null
    }

    /**
     * Eddystone 데이터 파싱
     */
    fun parseEddystone(scanResult: ScanResult): EddystoneData? {
        val scanRecord = scanResult.scanRecord ?: return null
        val serviceUuid = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")
        val serviceData = scanRecord.getServiceData(serviceUuid) ?: return null

        if (serviceData.isEmpty()) return null

        return when (serviceData[0].toInt()) {
            0x00 -> parseEddystoneUID(serviceData)
            0x10 -> parseEddystoneURL(serviceData)
            0x20 -> parseEddystoneTLM(serviceData)
            else -> null
        }
    }

    /**
     * Eddystone-UID 파싱
     */
    private fun parseEddystoneUID(data: ByteArray): EddystoneData? {
        if (data.size < 18) return null

        val txPower = data[1].toInt()
        val namespace = data.copyOfRange(2, 12).joinToString("") { "%02x".format(it) }
        val instance = data.copyOfRange(12, 18).joinToString("") { "%02x".format(it) }

        return EddystoneData(
            frameType = "UID",
            namespace = namespace,
            instance = instance,
            txPower = txPower
        )
    }

    /**
     * Eddystone-URL 파싱
     */
    private fun parseEddystoneURL(data: ByteArray): EddystoneData? {
        if (data.size < 3) return null

        val txPower = data[1].toInt()
        val urlScheme = getUrlScheme(data[2].toInt())
        val encodedUrl = data.copyOfRange(3, data.size)

        val url = urlScheme + decodeUrl(encodedUrl)

        return EddystoneData(
            frameType = "URL",
            url = url,
            txPower = txPower
        )
    }

    /**
     * Eddystone-TLM 파싱
     */
    private fun parseEddystoneTLM(data: ByteArray): EddystoneData? {
        if (data.size < 14) return null

        val version = data[1].toInt()
        val tlmData = "Version: $version"

        return EddystoneData(
            frameType = "TLM",
            tlmData = tlmData
        )
    }

    /**
     * URL Scheme 디코딩
     */
    private fun getUrlScheme(code: Int): String {
        return when (code) {
            0x00 -> "http://www."
            0x01 -> "https://www."
            0x02 -> "http://"
            0x03 -> "https://"
            else -> ""
        }
    }

    /**
     * URL 디코딩
     */
    private fun decodeUrl(encoded: ByteArray): String {
        val sb = StringBuilder()
        for (byte in encoded) {
            val code = byte.toInt() and 0xFF
            sb.append(
                when (code) {
                    0x00 -> ".com/"
                    0x01 -> ".org/"
                    0x02 -> ".edu/"
                    0x03 -> ".net/"
                    0x04 -> ".info/"
                    0x05 -> ".biz/"
                    0x06 -> ".gov/"
                    0x07 -> ".com"
                    0x08 -> ".org"
                    0x09 -> ".edu"
                    0x0a -> ".net"
                    0x0b -> ".info"
                    0x0c -> ".biz"
                    0x0d -> ".gov"
                    else -> if (code in 32..126) code.toChar() else ""
                }
            )
        }
        return sb.toString()
    }

    /**
     * 비콘 정보를 문자열로 포맷
     */
    fun formatBeaconInfo(scanResult: ScanResult): String {
        val beaconType = detectBeaconType(scanResult)

        return when (beaconType) {
            BeaconType.IBEACON -> {
                parseIBeacon(scanResult)?.let { beacon ->
                    """
                    Type: iBeacon
                    UUID: ${beacon.uuid}
                    Major: ${beacon.major}
                    Minor: ${beacon.minor}
                    TX Power: ${beacon.txPower} dBm
                    """.trimIndent()
                } ?: "iBeacon (파싱 실패)"
            }
            BeaconType.EDDYSTONE_UID, BeaconType.EDDYSTONE_URL, BeaconType.EDDYSTONE_TLM -> {
                parseEddystone(scanResult)?.let { beacon ->
                    buildString {
                        append("Type: Eddystone-${beacon.frameType}\n")
                        beacon.url?.let { append("URL: $it\n") }
                        beacon.namespace?.let { append("Namespace: $it\n") }
                        beacon.instance?.let { append("Instance: $it\n") }
                        beacon.txPower?.let { append("TX Power: $it dBm\n") }
                        beacon.tlmData?.let { append("TLM: $it\n") }
                    }.trimEnd()
                } ?: "Eddystone (파싱 실패)"
            }
            BeaconType.UNKNOWN -> "일반 BLE 장치"
        }
    }
}
