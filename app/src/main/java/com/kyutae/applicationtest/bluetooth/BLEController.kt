package com.kyutae.applicationtest.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.kyutae.applicationtest.dataclass.DataCenter
import com.kyutae.bluetoothsearch.static.BATTERY_UUID
import com.kyutae.bluetoothsearch.static.DEVICE_INFO_UUID
import com.kyutae.bluetoothsearch.static.TODOC_SERVICE_UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class BLEController(
    private val mContext: Context?,
    private var mBluetoothGatt: BluetoothGatt?
) {
    private var mBluetoothDevice: BluetoothDevice? = null
    var gattD0C0Service: BluetoothGattService? = null
    var gattD0C0Characteristic: UUID? = null
    var gattD0C0Descriptor: UUID? = null
    var gattBatteryServices: BluetoothGattService? = null
    var gattBatteryCharacteristic: UUID? = null
    var gattBatteryDescriptor: UUID? = null
    var gattDeviceInfoServices: BluetoothGattService? = null
    private val TAG = "BLEController"
    var services = arrayListOf<String>()
    var characMap = mutableMapOf<UUID, ArrayList<String>>()
    companion object{
        val gattConnect = MutableLiveData<Boolean>()
    }


    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT Server")
                    Log.i(
                        TAG, "Attempting to start service discovery: " +
                                mBluetoothGatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e(TAG, "Disconnected to GATT Server")
                    disconnectGattServer()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "Connected to GATT_SUCCESS.")
                    Log.i(TAG, "createBond Result : Complete")
                    CoroutineScope(Dispatchers.Main).launch {
                        gattConnect.value = true
                        Log.i(TAG, "gattConnect LIVEDATA : Complete")
                    }
                    services.clear()
                    characMap.clear()
//                    characArray.clear()
                    for (i in gatt!!.services.indices) {
                        val serviceUuid = gatt.services[i].uuid
                        Log.i(TAG, "gattService : $serviceUuid")
                        services.add(serviceUuid.toString())

                        val characArray = arrayListOf<String>()

                        for (j in gatt.services[i].characteristics.indices) {
                            val characUuid = gatt.services[i].characteristics[j].uuid
                            Log.i(TAG, "gattServiceCharac : $characUuid")
                            characArray.add(characUuid.toString())
                        }
                        characMap[serviceUuid] = characArray
                    }
                    DataCenter.charcSet(characMap)
                    DataCenter.serviceSet(services)
                    checkGattServices(gatt.services)
                    Log.i(TAG, "characMap : ${characMap}")
                    Log.i(TAG, "gatt.device.name : ${gatt.device.name}")
                    broadcastUpdate("Connected " + mBluetoothDevice?.name)
                }

                else -> {
                    Log.w(TAG, "Device service discovery failed, status: $status")
                    CoroutineScope(Dispatchers.Main).launch {
                        gattConnect.value = false
                    }
                    broadcastUpdate("Fail Connect " + mBluetoothDevice?.name)
                }
            }
        }

        private fun checkGattServices(gattServices: List<BluetoothGattService>?) {
            gattServices ?: return

            //토닥서비스
            gattD0C0Service = gattServices.firstOrNull {
                it.uuid.toString().lowercase().startsWith(TODOC_SERVICE_UUID)
            }

            //토닥서비스 characteristic uuid 값
            val d0c0Characteristic = gattD0C0Service?.characteristics
            gattD0C0Characteristic = gattD0C0Service?.characteristics?.get(0)?.uuid


            //토닥서비스 descriptor uuid 값
            gattD0C0Descriptor =
                d0c0Characteristic!![0].service!!.characteristics[0].descriptors[0].uuid

            //배터리 값 읽어오기
            gattBatteryServices =
                gattServices.firstOrNull { it.uuid.toString().lowercase().startsWith(BATTERY_UUID) }

            //배터리 characteristic uuid 값
            val batteryCharacteristic = gattBatteryServices?.characteristics
            gattBatteryCharacteristic = gattBatteryServices?.characteristics!![0]?.uuid

            //배터리 descriptor uuid 값
            gattBatteryDescriptor =
                batteryCharacteristic!![0]?.service!!.characteristics[0].descriptors[0].uuid


            //device info 값 읽어오기
            gattDeviceInfoServices = gattServices.firstOrNull {
                it.uuid.toString().lowercase().startsWith(DEVICE_INFO_UUID)
            }


            Log.e(TAG, "gattD0C0Service UUID : ${gattD0C0Service!!.uuid}")
            Log.e(TAG, "gattBatteryCharacteristic : $gattBatteryCharacteristic")


        }

        @SuppressLint("MissingPermission")
        private fun broadcastUpdate(str: String) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show()
                println("name : ${mBluetoothDevice!!.name}")
                println("address : ${mBluetoothDevice!!.address}")
                println("type : ${mBluetoothDevice!!.type}")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.e(TAG, "characteristic  :  ${characteristic}")

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.i(TAG, "onCharacteristicChanged uuid : ${characteristic.uuid}")
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.i(TAG, "onCharacteristicRead uuid : ${characteristic.uuid}")

        }
    }


    @SuppressLint("MissingPermission")
    fun connectGatt(device: BluetoothDevice): BluetoothGatt? {
        this.mBluetoothDevice = device

        mBluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(
                mContext, false, mGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            device.connectGatt(mContext, false, mGattCallback)
        }
        return mBluetoothGatt
    }

    @SuppressLint("MissingPermission")
    private fun disconnectGattServer() {
        Log.d(TAG, "Closing Gatt connection")
        // disconnect and close the gatt
        if (mBluetoothGatt != null) {
            mBluetoothGatt?.disconnect()
            mBluetoothGatt?.close()
            mBluetoothGatt = null
        }
    }
}