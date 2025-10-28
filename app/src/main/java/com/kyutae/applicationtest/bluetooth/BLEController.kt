package com.kyutae.applicationtest.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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
        val serviceGet = MutableLiveData<Boolean>()

        // Characteristic Read/Write/Notify 결과를 전달하기 위한 LiveData
        val characteristicRead = MutableLiveData<Pair<String, ByteArray>>() // UUID, value
        val characteristicWritten = MutableLiveData<String>() // UUID
        val characteristicChanged = MutableLiveData<Pair<String, ByteArray>>() // UUID, value
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
                    gatt ?: run {
                        Log.e(TAG, "Gatt is null in onServicesDiscovered")
                        return
                    }

                    Log.i(TAG, "Connected to GATT_SUCCESS.")
                    Log.i(TAG, "createBond Result : Complete")
                    CoroutineScope(Dispatchers.Main).launch {
                        gattConnect.value = true
                        Log.i(TAG, "gattConnect LIVEDATA : Complete")
                    }

                    services.clear()
                    characMap.clear()

                    gatt.services.forEachIndexed { _, service ->
                        val serviceUuid = service.uuid
                        Log.i(TAG, "gattService : $serviceUuid")
                        services.add(serviceUuid.toString())

                        val characArray = arrayListOf<String>()

                        service.characteristics.forEach { characteristic ->
                            val characUuid = characteristic.uuid
                            Log.i(TAG, "gattServiceCharac : $characUuid")
                            characArray.add(characUuid.toString())
                        }
                        characMap[serviceUuid] = characArray
                    }

                    DataCenter.charcSet(characMap)
                    DataCenter.serviceSet(services)
                    checkGattServices(gatt.services)
                    Log.i(TAG, "characMap : $characMap")
                    Log.i(TAG, "gatt.device.name : ${gatt.device?.name ?: "Unknown"}")
                    broadcastUpdate("Connected " + (mBluetoothDevice?.name ?: "Unknown Device"))
                }

                else -> {
                    Log.w(TAG, "Device service discovery failed, status: $status")
                    CoroutineScope(Dispatchers.Main).launch {
                        gattConnect.value = false
                    }
                    broadcastUpdate("Fail Connect " + (mBluetoothDevice?.name ?: "Unknown Device"))
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


//            //토닥서비스 descriptor uuid 값
//            gattD0C0Descriptor =
//                d0c0Characteristic!![0].service?.characteristics?.get(0)?.descriptors?.get(0)?.uuid

            //배터리 값 읽어오기
            gattBatteryServices =
                gattServices.firstOrNull { it.uuid.toString().lowercase().startsWith(BATTERY_UUID) }

            //배터리 characteristic uuid 값
            val batteryCharacteristic = gattBatteryServices?.characteristics
            gattBatteryCharacteristic = gattBatteryServices?.characteristics?.get(0)?.uuid

            //배터리 descriptor uuid 값
            gattBatteryDescriptor =
                batteryCharacteristic?.get(0)?.service?.characteristics?.get(0)?.descriptors?.get(0)?.uuid


            //device info 값 읽어오기
            gattDeviceInfoServices = gattServices.firstOrNull {
                it.uuid.toString().lowercase().startsWith(DEVICE_INFO_UUID)
            }


//            Log.e(TAG, "gattD0C0Service UUID : ${gattD0C0Service!!.uuid}")
            Log.e(TAG, "gattBatteryCharacteristic : $gattBatteryCharacteristic")


        }

        @SuppressLint("MissingPermission")
        private fun broadcastUpdate(str: String) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show()
                mBluetoothDevice?.let { device ->
                    Log.d(TAG, "Device Info - name: ${device.name ?: "Unknown"}, address: ${device.address}, type: ${device.type}")
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.e(TAG, "onCharacteristicWrite uuid: ${characteristic?.uuid}, status: $status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        characteristicWritten.value = it.uuid.toString()
                    }
                }
            } else {
                Log.e(TAG, "Characteristic write failed with status: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.i(TAG, "onCharacteristicChanged uuid: ${characteristic.uuid}, value: ${value.joinToString(" ") { "%02X".format(it) }}")

            CoroutineScope(Dispatchers.Main).launch {
                characteristicChanged.value = Pair(characteristic.uuid.toString(), value)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.i(TAG, "onCharacteristicRead uuid: ${characteristic.uuid}, value: ${value.joinToString(" ") { "%02X".format(it) }}, status: $status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    characteristicRead.value = Pair(characteristic.uuid.toString(), value)
                }
            } else {
                Log.e(TAG, "Characteristic read failed with status: $status")
            }
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

    /**
     * Characteristic 읽기
     */
    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        mBluetoothGatt?.let { gatt ->
            Log.d(TAG, "Reading characteristic: ${characteristic.uuid}")
            return gatt.readCharacteristic(characteristic)
        } ?: run {
            Log.e(TAG, "BluetoothGatt is null, cannot read characteristic")
            return false
        }
    }

    /**
     * Characteristic 쓰기
     */
    @SuppressLint("MissingPermission")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray): Boolean {
        mBluetoothGatt?.let { gatt ->
            Log.d(TAG, "Writing characteristic: ${characteristic.uuid}, value: ${value.joinToString(" ") { "%02X".format(it) }}")

            // Android 13+ (API 33+)에서는 writeCharacteristic(characteristic, value, writeType) 사용
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val writeType = if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                } else {
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                }
                gatt.writeCharacteristic(characteristic, value, writeType) == BluetoothGatt.GATT_SUCCESS
            } else {
                // Android 12 이하
                characteristic.value = value
                characteristic.writeType = if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                } else {
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                }
                gatt.writeCharacteristic(characteristic)
            }
        } ?: run {
            Log.e(TAG, "BluetoothGatt is null, cannot write characteristic")
            return false
        }
    }

    /**
     * Characteristic Notification 설정
     */
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean): Boolean {
        mBluetoothGatt?.let { gatt ->
            Log.d(TAG, "${if (enable) "Enabling" else "Disabling"} notification for: ${characteristic.uuid}")

            // Local notification 설정
            val success = gatt.setCharacteristicNotification(characteristic, enable)
            if (!success) {
                Log.e(TAG, "Failed to set characteristic notification")
                return false
            }

            // Descriptor 설정 (Client Characteristic Configuration Descriptor)
            val descriptor = characteristic.descriptors?.firstOrNull {
                it.uuid.toString() == "00002902-0000-1000-8000-00805f9b34fb"
            }

            descriptor?.let {
                Log.d(TAG, "Setting descriptor for notification")
                val value = if (enable) {
                    if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    } else if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    } else {
                        Log.e(TAG, "Characteristic does not support notification or indication")
                        return false
                    }
                } else {
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }

                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(it, value) == BluetoothGatt.GATT_SUCCESS
                } else {
                    it.value = value
                    gatt.writeDescriptor(it)
                }
            } ?: run {
                Log.e(TAG, "Client Characteristic Configuration Descriptor not found")
                return false
            }
        } ?: run {
            Log.e(TAG, "BluetoothGatt is null, cannot set notification")
            return false
        }
    }
}