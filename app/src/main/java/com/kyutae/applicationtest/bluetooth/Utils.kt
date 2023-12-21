package com.kyutae.applicationtest.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.kyutae.applicationtest.Application
import com.kyutae.applicationtest.MainFragment
import com.kyutae.applicationtest.MainFragment.Companion.devicesArr
import com.kyutae.applicationtest.MainFragment.Companion.userAdapter
//import com.kyutae.applicationtest.MainActivity.Companion.devicesArr
//import com.kyutae.applicationtest.MainActivity.Companion.userAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Utils {
    var bluetoothManager = Application.ApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let{
                // results is not null
                for (result in it){
                    val deviceAddress = result.device.address
                    // 중복 체크
                    if (!devicesArr.any { it.device.address == deviceAddress }) {
                        devicesArr.add(result)
                    }
//                    if (!devicesArr.contains(result)) devicesArr.add(result)
//                    if (!devicesArr.contains(result.device) && result.device.name!=null) devicesArr.add(result.device)
                }

            }
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val deviceAddress = result.device.address
                // 중복 체크
                if (!devicesArr.any { it.device.address == deviceAddress }) {
                    devicesArr.add(result)
                    Log.i("KYUTAE" , "result : ${devicesArr}")
                }

//                if (!devicesArr.contains(it)) devicesArr.add(it)
//                Log.i("KYUTAE" , "RSSI : ${it.rssi}")
            }
            userAdapter.notifyDataSetChanged()
        }

    }
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun scanDevice(state:Boolean) = if(state){
        CoroutineScope(Dispatchers.Main).launch {
            delay(10000L)
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
            MainFragment.isScanning.value = false
        }
        devicesArr.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
        MainFragment.isScanning.value = true
    }else{
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        MainFragment.isScanning.value = false
    }


    fun manufacureID(manufacturerId : Int) : String{
        return when (manufacturerId) {
            0 -> "Ericsson AB"
            2 -> "IntelCorp"
            6 -> "Microsoft"
            76 -> "Apple Inc"
            86 -> "Sony Ericsson Mobile Communications AB"
            184 -> "Qualcomm Innovation Center,Inc."
            89 -> "Nordic Semiconductor ASA"
            117 -> "Samsung Electronics Co., Ltd"
            133 -> "Bose Corporation"
            211 -> "Fitbit, Inc"
            224 -> "Google LLC"
            256 -> "Samsung Semiconductor, Inc"
            305 -> "Garmin International, Inc"
            else -> "$manufacturerId"
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v shr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }



}