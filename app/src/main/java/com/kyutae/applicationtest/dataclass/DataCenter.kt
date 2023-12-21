package com.kyutae.applicationtest.dataclass

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.kyutae.applicationtest.Application
import java.util.UUID

object DataCenter {
    var level = 0
    var play = false

    val item = JsonObject()


    fun load() {
        val prefs: SharedPreferences = Application.application.getSharedPreferences("bluetoothInfo", 0)
        prefs.getString("bluetoothInfo", null)?.let {
            val json = Parser.default().parse(StringBuilder(it)) as JsonObject
            item.putAll(json)
        }

    }


    fun put(k: String, v: List<String>) {
        item[k] = v
        val prefs: SharedPreferences = Application.application.getSharedPreferences("bluetoothInfo", 0)
        prefs.edit().apply {
            putString("bluetoothInfo", item.toJsonString())
        }.apply()
    }
    fun putC(k: String, v: MutableMap<UUID, ArrayList<String>>) {
        item[k] = v
        val prefs: SharedPreferences = Application.application.getSharedPreferences("bluetoothInfo", 0)
        prefs.edit().apply {
            putString("bluetoothInfo", item.toJsonString())
        }.apply()
    }
    fun del(k: String) {
        item.remove(k)
        val prefs: SharedPreferences = Application.application.getSharedPreferences("bluetoothInfo", 0)
        prefs.edit().apply {
            putString("bluetoothInfo", item.toJsonString())
        }.apply()
    }

    fun serviceSet(service: List<String>) {
        put("serviceUUID", service)
    }
//    fun serviceGet() = item.string("serviceUUID")
    fun serviceGet() = item["serviceUUID"]
    fun serviceDel() = del("serviceUUID")

    fun charcSet(charc: MutableMap<UUID, ArrayList<String>>) {
        putC("charcUUID", charc)
    }
    fun charcGet() = item["charcUUID"]
    fun charcDel() = del("charcUUID")

    var batteryInfo: MutableLiveData<String> = MutableLiveData()

    var stateInfo: MutableLiveData<String> = MutableLiveData()

    var bleGattState: MutableLiveData<Boolean> = MutableLiveData()

    fun deviceAddress(name:String) : String? {
        return item.string("address$name")
    }

//    //초기 레벨
//    fun ivGet(): ByteArray {
//        val iv = item.string("initialIdIv")
//        Log.e(TAG, "iv : $iv")
//        val decodedData: ByteArray = try {
//            Base64.decode(iv, Base64.DEFAULT)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ByteArray(0)
//        }
//
////        if (decodedData.isNotEmpty()){
////            val id = item.string("initialId")?.decryptCBC(decodedData)
////            return id.toString()
////        }
//        return decodedData
//    }
//    fun idGet() = item.string("initialId")?.toString()
//
//
//    fun idSet(id: String, iv: String) {
////        DataCenter.id = id
//        putIV("initialId", id.toString())
//        putIV("initialIdIv", iv.toString())
//        apply { }
//    }
//
//    fun idDel(id: String) {
//        item.remove(id)
//        val prefs: SharedPreferences = Application.application.getSharedPreferences("initialId", 0)
//        prefs.edit().apply {
//            putString("initialId", item.toJsonString())
//        }.apply()
//    }
//
//
//    //초기 레벨
//    fun levelGet() = item.string("initialLevel")?.toIntOrNull()
//    fun levelSet(level: Int) {
//        DataCenter.level = level
//        put("initialLevel", level.toString())
//    }
//
//    //작동시간
//    fun opTimePut(min: Int) {
//        put("opTimeMin", min.toString())
//        opTimeMin = min
//    }
//
//    //작동 모드
//    fun modeGet() = item.string("lastMode")
//    fun modeSet(mode: String) {
//        if (mode != modeGet()) {
//            put("lastMode", mode)
//        }
//    }
//
//    //한번 연결한 디바이스는 등록해서 계속 연결한다.
//    fun registerDeviceAddress(side: String, address: String) {
//        if (side != "L" && side != "R")
//            throw Error("")
//        put("address$side", address)
//    }
//
//    //
//    fun unregisterDevice(side: String) {
//        if (side != "L" && side != "R")
//            throw Error("")
//        del("address$side")
//    }
//
//
//    fun deviceAddresses(): HashSet<String> {
//        val addresses = HashSet<String>()
//        deviceAddress("L")?.let { addresses.add(it) }
//        deviceAddress("R")?.let { addresses.add(it) }
//        return addresses
//    }
//
//    fun deviceAddress(side: String): String? {
//        return item.string("address$side")
//    }
//
//
//    var batteryR: MutableLiveData<String> = MutableLiveData()
//    var batteryL: MutableLiveData<String> = MutableLiveData()
//
//    class DeviceInfo {
//        var manufacturerName = ""
//        var serialNumber = ""
//    }
//
//    var deviceInfo = DeviceInfo()

}