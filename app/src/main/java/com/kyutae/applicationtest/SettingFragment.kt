package com.kyutae.applicationtest

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import android.bluetooth.BluetoothGattCharacteristic
import com.kyutae.applicationtest.MainFragment.Companion.bleGatt
import com.kyutae.applicationtest.MainFragment.Companion.bluetoothDataClass
import com.kyutae.applicationtest.adapters.CharacteristicsAdapter
import com.kyutae.applicationtest.adapters.ServiceAdapter
import com.kyutae.applicationtest.bluetooth.BLEController
import com.kyutae.applicationtest.databinding.FragmentSettingBinding
import com.kyutae.applicationtest.dataclass.DataCenter
import com.kyutae.applicationtest.utils.AutoReconnectManager
import com.kyutae.applicationtest.viewmodel.ConnectionState
import com.kyutae.applicationtest.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast

class SettingFragment : Fragment() {
    lateinit var bind: FragmentSettingBinding
    private val TAG = "SettingFragment"
    lateinit var serviceAdapter: ServiceAdapter
    lateinit var mContext: Context
    private lateinit var callback: OnBackPressedCallback
    private var bleController: BLEController? = null

    // ViewModel 사용 (Activity 범위로 공유)
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentSettingBinding.inflate(inflater, container, false)


        return bind.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                removeFragment()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        // BLE Characteristic Read/Write/Notify 결과 관찰
        setupBLEObservers()

        println("bluetoothDataClass : $bluetoothDataClass")

        bind.nameTxt.text = bluetoothDataClass?.deviceName ?: "N/A"
        bind.addressTxt.text = bluetoothDataClass?.address ?: "N/A"
        bind.rssiTxt.text = bluetoothDataClass?.rssi ?: "N/A"
        bind.typeTxt.text = bluetoothDataClass?.type ?: "N/A"
        bind.companyKeyTxt.text = bluetoothDataClass?.company ?: "N/A"
        bind.companyValueTxt.text = bluetoothDataClass?.companyValue ?: "N/A"

        // Service UUID 처리 (광고 패킷에 포함되지 않을 수 있음)
        val uuid = bluetoothDataClass?.uuid ?: "N/A"
        val uuidValue = bluetoothDataClass?.uuidValue ?: "N/A"

        if (uuid == "N/A" || uuid.isBlank()) {
            bind.serviceUuidTxt.text = getString(R.string.not_advertised)
            bind.serviceDataTxt.text = getString(R.string.service_not_advertised)
        } else {
            bind.serviceUuidTxt.text = uuid
            bind.serviceDataTxt.text = if (uuidValue != "N/A") uuidValue else getString(R.string.no_data)
        }

        // 연결 상태 초기 설정
        viewModel.setConnectionState(ConnectionState.CONNECTING)

        // 연결 상태 관찰
        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            updateConnectionStatus(state)
        }

        bind.backBtn.setOnClickListener {
            removeFragment()
        }

        // 자동 재연결 스위치 초기화
        setupAutoReconnectSwitch()

        BLEController.gattConnect.observe(requireActivity()) {
            if (it) {
                // GATT 연결 성공
                viewModel.setConnectionState(ConnectionState.CONNECTED)
                Log.e(TAG, "LIVEDATA TRUE")
                Log.e(TAG, "$bleGatt")
                if (bleGatt != null) {
                    // BLEController 인스턴스 생성
                    bleController = BLEController(mContext, bleGatt)

                    Log.e(TAG, "${DataCenter.serviceGet()}")
                    CoroutineScope(Dispatchers.Main).launch {
                        bind.progressBar.visibility = View.VISIBLE
                        delay(5000L)
                        bind.progressBar.visibility = View.GONE
                        if (DataCenter.serviceGet() != null && bleGatt != null) {
                            val listservice = DataCenter.serviceGet() as List<*>

                            // bleGatt에서 실제 Characteristic 객체를 가져와서 Map 생성
                            val mapCharc = mutableMapOf<java.util.UUID, List<BluetoothGattCharacteristic>>()

                            bleGatt!!.services.forEach { service ->
                                val serviceUuid = service.uuid
                                val characteristics = service.characteristics
                                mapCharc[serviceUuid] = characteristics
                            }

                            Log.e(TAG, "listservice : ${listservice}")
                            Log.e(TAG, "mapCharc : ${mapCharc}")

                            for (i in listservice.indices) {
                                Log.e(TAG, "listservice$i : ${listservice[i]}")
                            }

                            // ServiceAdapter에 characteristics map 전달 (실제 객체)
                            serviceAdapter = ServiceAdapter(mContext, listservice, mapCharc)

                            // Characteristic 액션 리스너 설정
                            serviceAdapter.characteristicActionListener = object : CharacteristicsAdapter.OnCharacteristicActionListener {
                                override fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic) {
                                    Log.d(TAG, "Read requested for: ${characteristic.uuid}")
                                    bleController?.readCharacteristic(characteristic)
                                }

                                override fun onWriteCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
                                    Log.d(TAG, "Write requested for: ${characteristic.uuid}, value: ${value.joinToString(" ") { "%02X".format(it) }}")
                                    bleController?.writeCharacteristic(characteristic, value)
                                }

                                override fun onNotifyCharacteristic(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
                                    Log.d(TAG, "${if (enable) "Enable" else "Disable"} notification for: ${characteristic.uuid}")
                                    bleController?.setCharacteristicNotification(characteristic, enable)
                                }
                            }

                            serviceAdapter.notifyDataSetChanged()

                            bind.recyclerview.apply {
                                adapter = serviceAdapter
                                layoutManager =
                                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            }
                            bind.serviceLinear.visibility = View.VISIBLE
                        } else {
                            bind.serviceLinear.visibility = View.GONE
                        }
                    }
                }else{
                    bind.serviceLinear.visibility = View.GONE
                }
            } else {
                // GATT 연결 실패 또는 연결 끊김
                viewModel.setConnectionState(ConnectionState.DISCONNECTED)
            }
        }

    }


    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "ONDETACH!!!!!!!!!!!!!!!!!!!")
        DataCenter.serviceDel()
        DataCenter.charcDel()
    }

    /**
     * 자동 재연결 스위치 설정
     */
    private fun setupAutoReconnectSwitch() {
        // 현재 자동 재연결 상태 불러오기
        val isEnabled = AutoReconnectManager.isAutoReconnectEnabled(mContext)
        bind.autoReconnectSwitch.isChecked = isEnabled

        // 스위치 변경 리스너
        bind.autoReconnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            val deviceAddress = bluetoothDataClass?.address

            if (deviceAddress != null) {
                if (isChecked) {
                    // 자동 재연결 활성화
                    AutoReconnectManager.enableAutoReconnect(mContext, deviceAddress)
                    Toast.makeText(mContext, R.string.auto_reconnect_enabled, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Auto-reconnect enabled for device: $deviceAddress")
                } else {
                    // 자동 재연결 비활성화
                    AutoReconnectManager.disableAutoReconnect(mContext)
                    Toast.makeText(mContext, R.string.auto_reconnect_disabled, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Auto-reconnect disabled")
                }
            } else {
                // 장치 주소가 없으면 스위치를 다시 끔
                bind.autoReconnectSwitch.isChecked = false
                Toast.makeText(mContext, R.string.device_info_not_available, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Device address is null, cannot enable auto-reconnect")
            }
        }
    }

    /**
     * 연결 상태 UI 업데이트
     */
    private fun updateConnectionStatus(state: ConnectionState) {
        val statusText: String
        val statusColor: Int
        val indicatorColor: Int

        when (state) {
            ConnectionState.CONNECTED -> {
                statusText = getString(R.string.state_connected)
                statusColor = ContextCompat.getColor(requireContext(), R.color.ewhagreen)
                indicatorColor = ContextCompat.getColor(requireContext(), R.color.ewhagreen)
            }
            ConnectionState.CONNECTING -> {
                statusText = getString(R.string.state_connecting)
                statusColor = ContextCompat.getColor(requireContext(), R.color.ewhayellow)
                indicatorColor = ContextCompat.getColor(requireContext(), R.color.ewhayellow)
            }
            ConnectionState.DISCONNECTED -> {
                statusText = getString(R.string.state_disconnected)
                statusColor = ContextCompat.getColor(requireContext(), R.color.ewhacoral)
                indicatorColor = ContextCompat.getColor(requireContext(), R.color.ewhacoral)
            }
            ConnectionState.DISCONNECTING -> {
                statusText = getString(R.string.state_disconnecting)
                statusColor = ContextCompat.getColor(requireContext(), R.color.gray)
                indicatorColor = ContextCompat.getColor(requireContext(), R.color.gray)
            }
        }

        bind.connectionStatusText.text = statusText
        bind.connectionStatusText.setTextColor(statusColor)

        // 상태 표시 원 색상 변경
        val drawable = bind.connectionStatusIndicator.background as? GradientDrawable
        if (drawable != null) {
            drawable.setColor(indicatorColor)
        } else {
            // GradientDrawable이 아닌 경우 새로 생성
            val circle = GradientDrawable()
            circle.shape = GradientDrawable.OVAL
            circle.setColor(indicatorColor)
            bind.connectionStatusIndicator.background = circle
        }
    }

    /**
     * BLE Characteristic 작업 결과 관찰 설정
     */
    private fun setupBLEObservers() {
        // Characteristic Read 결과
        BLEController.characteristicRead.observe(viewLifecycleOwner) { (uuid, value) ->
            Log.d(TAG, "Characteristic read: $uuid, value: ${value.joinToString(" ") { "%02X".format(it) }}")
            serviceAdapter.updateCharacteristicValue(uuid, value)
        }

        // Characteristic Write 결과
        BLEController.characteristicWritten.observe(viewLifecycleOwner) { uuid ->
            Log.d(TAG, "Characteristic written: $uuid")
        }

        // Characteristic Changed (Notify)
        BLEController.characteristicChanged.observe(viewLifecycleOwner) { (uuid, value) ->
            Log.d(TAG, "Characteristic changed: $uuid, value: ${value.joinToString(" ") { "%02X".format(it) }}")
            serviceAdapter.updateCharacteristicValue(uuid, value)
        }
    }

    fun removeFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .remove(this)
            .commit()
        requireActivity().supportFragmentManager.popBackStack()
    }
}