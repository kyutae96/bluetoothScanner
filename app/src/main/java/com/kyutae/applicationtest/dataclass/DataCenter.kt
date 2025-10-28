package com.kyutae.applicationtest.dataclass

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.kyutae.applicationtest.BluesCanApplication
import java.util.UUID

object DataCenter {
    var level = 0
    var play = false

    val item = JsonObject()


    fun load() {
        val prefs: SharedPreferences = BluesCanApplication.instance.getSharedPreferences("bluetoothInfo", 0)
        prefs.getString("bluetoothInfo", null)?.let {
            val json = Parser.default().parse(StringBuilder(it)) as JsonObject
            item.putAll(json)
        }

    }


    fun put(k: String, v: List<String>) {
        item[k] = v
        val prefs: SharedPreferences = BluesCanApplication.instance.getSharedPreferences("bluetoothInfo", 0)
        prefs.edit().apply {
            putString("bluetoothInfo", item.toJsonString())
        }.apply()
    }
    fun putC(k: String, v: MutableMap<UUID, ArrayList<String>>) {
        item[k] = v
        val prefs: SharedPreferences = BluesCanApplication.instance.getSharedPreferences("bluetoothInfo", 0)
        prefs.edit().apply {
            putString("bluetoothInfo", item.toJsonString())
        }.apply()
    }
    fun del(k: String) {
        item.remove(k)
        val prefs: SharedPreferences = BluesCanApplication.instance.getSharedPreferences("bluetoothInfo", 0)
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



}