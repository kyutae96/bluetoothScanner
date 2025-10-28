package com.kyutae.applicationtest.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kyutae.applicationtest.BluesCanApplication
import com.kyutae.applicationtest.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BLE 유틸리티 클래스
 * 블루투스 스캔 및 장치 발견을 담당합니다
 */
object Utils {
    var bluetoothManager = BluesCanApplication.ApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var scanJob: Job? = null
    private var viewModel: MainViewModel? = null
    private var onDeviceFoundCallback: ((ScanResult) -> Unit)? = null

    /**
     * ViewModel 설정 (스캔 상태 관리를 위해)
     */
    fun setViewModel(vm: MainViewModel) {
        viewModel = vm
    }

    /**
     * 장치 발견 콜백 설정
     */
    fun setOnDeviceFoundCallback(callback: (ScanResult) -> Unit) {
        onDeviceFoundCallback = callback
    }

    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("scanCallback", "BLE Scan Failed : $errorCode")
            viewModel?.setScanning(false)
            viewModel?.setError("스캔 실패: 에러 코드 $errorCode")
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { result ->
                viewModel?.addDevice(result)
                onDeviceFoundCallback?.invoke(result)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                viewModel?.addDevice(it)
                onDeviceFoundCallback?.invoke(it)
                Log.i("KYUTAE", "Device found: ${it.device.address}")
            }
        }
    }

    /**
     * BLE 스캔 시작/중지
     * @param state true: 스캔 시작, false: 스캔 중지
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun scanDevice(state: Boolean) {
        if (state) {
            // 스캔 시작
            viewModel?.clearDevices()
            viewModel?.setScanning(true)

            bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)

            // 10초 후 자동 중지
            scanJob?.cancel()
            scanJob = CoroutineScope(Dispatchers.Main).launch {
                delay(10000L)
                stopScan()
            }
        } else {
            // 스캔 중지
            stopScan()
        }
    }

    /**
     * 스캔 중지
     */
    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        viewModel?.setScanning(false)
        scanJob?.cancel()
        scanJob = null
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