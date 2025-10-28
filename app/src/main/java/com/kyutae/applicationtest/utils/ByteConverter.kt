package com.kyutae.applicationtest.utils

/**
 * 바이트 데이터 변환 유틸리티
 */
object ByteConverter {

    /**
     * 바이트 배열을 Hex 문자열로 변환
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }

    /**
     * Hex 문자열을 바이트 배열로 변환
     */
    fun hexToBytes(hex: String): ByteArray? {
        return try {
            val cleanHex = hex.replace(" ", "").replace("0x", "")
            if (cleanHex.length % 2 != 0) return null

            ByteArray(cleanHex.length / 2) { i ->
                cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 바이트 배열을 ASCII 문자열로 변환
     */
    fun bytesToAscii(bytes: ByteArray): String {
        return bytes.map { byte ->
            if (byte in 32..126) byte.toInt().toChar()
            else '.'
        }.joinToString("")
    }

    /**
     * ASCII 문자열을 바이트 배열로 변환
     */
    fun asciiToBytes(ascii: String): ByteArray {
        return ascii.toByteArray(Charsets.US_ASCII)
    }

    /**
     * 바이트 배열을 Decimal 문자열로 변환
     */
    fun bytesToDecimal(bytes: ByteArray): String {
        return bytes.joinToString(" ") { it.toUByte().toString() }
    }

    /**
     * 바이트 배열을 Int로 변환 (Little Endian)
     */
    fun bytesToIntLE(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() and 0xFF shl (8 * i))
        }
        return result
    }

    /**
     * 바이트 배열을 Int로 변환 (Big Endian)
     */
    fun bytesToIntBE(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() and 0xFF shl (8 * (bytes.size - 1 - i)))
        }
        return result
    }

    /**
     * Int를 바이트 배열로 변환 (Little Endian)
     */
    fun intToBytesLE(value: Int, size: Int = 4): ByteArray {
        return ByteArray(size) { i ->
            ((value shr (8 * i)) and 0xFF).toByte()
        }
    }

    /**
     * Int를 바이트 배열로 변환 (Big Endian)
     */
    fun intToBytesBE(value: Int, size: Int = 4): ByteArray {
        return ByteArray(size) { i ->
            ((value shr (8 * (size - 1 - i))) and 0xFF).toByte()
        }
    }

    /**
     * 바이트 배열을 Binary 문자열로 변환
     */
    fun bytesToBinary(bytes: ByteArray): String {
        return bytes.joinToString(" ") { byte ->
            byte.toUByte().toString(2).padStart(8, '0')
        }
    }

    /**
     * 데이터 포맷 열거형
     */
    enum class DataFormat {
        HEX,
        ASCII,
        DECIMAL,
        BINARY
    }

    /**
     * 바이트 배열을 지정된 포맷으로 변환
     */
    fun convert(bytes: ByteArray, format: DataFormat): String {
        return when (format) {
            DataFormat.HEX -> bytesToHex(bytes)
            DataFormat.ASCII -> bytesToAscii(bytes)
            DataFormat.DECIMAL -> bytesToDecimal(bytes)
            DataFormat.BINARY -> bytesToBinary(bytes)
        }
    }
}
