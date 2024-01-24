package com.kyutae.applicationtest

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyutae.applicationtest.MainFragment.Companion.bleGatt
import com.kyutae.applicationtest.MainFragment.Companion.bluetoothDataClass
import com.kyutae.applicationtest.adapters.CharacteristicsAdapter
import com.kyutae.applicationtest.adapters.ServiceAdapter
import com.kyutae.applicationtest.bluetooth.BLEController
import com.kyutae.applicationtest.databinding.FragmentSettingBinding
import com.kyutae.applicationtest.dataclass.DataCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SettingFragment : Fragment() {
    lateinit var bind: FragmentSettingBinding
    private val TAG = "SettingFragment"
    lateinit var serviceAdapter: ServiceAdapter
    lateinit var characteristicsAdapter: CharacteristicsAdapter
    lateinit var mContext: Context
    var gattServices: BluetoothGattService? = null
    var gattBatteryServices: BluetoothGattService? = null
    var gattDeviceInfoServices: BluetoothGattService? = null

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

        println("bluetoothDataClass : $bluetoothDataClass")

        bind.nameTxt.text = bluetoothDataClass?.deviceName ?: "N/A"
        bind.addressTxt.text = bluetoothDataClass?.address ?: "N/A"
        bind.rssiTxt.text = bluetoothDataClass?.rssi ?: "N/A"
        bind.typeTxt.text = bluetoothDataClass?.type ?: "N/A"
        bind.companyKeyTxt.text = bluetoothDataClass?.company ?: "N/A"
        bind.companyValueTxt.text = bluetoothDataClass?.companyValue ?: "N/A"
        bind.serviceUuidTxt.text = bluetoothDataClass?.uuid ?: "N/A"
        bind.serviceDataTxt.text = bluetoothDataClass?.uuidValue ?: "N/A"


        BLEController.gattConnect.observe(requireActivity()) {
            if (it) {
                Log.e(TAG, "LIVEDATA TRUE")
                Log.e(TAG, "$bleGatt")
                if (bleGatt != null) {
                Log.e(TAG, "${DataCenter.serviceGet()}")
                    CoroutineScope(Dispatchers.Main).launch {
                        bind.progressBar.visibility = View.VISIBLE
                        delay(5000L)
                        bind.progressBar.visibility = View.GONE
                        if (DataCenter.serviceGet() != null) {
                            val listservice = DataCenter.serviceGet() as List<*>
                            Log.e(TAG, "listservice : ${listservice}")
                            for (i in listservice.indices) {
                                Log.e(TAG, "listservice$i : ${listservice[i]}")
                            }
                            serviceAdapter = ServiceAdapter(mContext, listservice)
                            serviceAdapter.notifyDataSetChanged()

                            serviceAdapter.mListener = object : ServiceAdapter.OnItemClickListener {
                                override fun onClick(view: View, position: Int) {
                                    if (bind.characteristicLinear.visibility == View.VISIBLE) {
                                        bind.characteristicLinear.visibility = View.GONE
                                        gattServices = null
                                    } else {
                                        onClickItem(listservice[position].toString(), position)
                                    }
                                }
                            }

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
            }
        }

    }

    private fun onClickItem(item: String, index: Int) {
        val serviceUUID = UUID.fromString(item)
        gattServices = BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val mapCharc = DataCenter.charcGet() as Map<*, *>


        Log.i(TAG, "mapCharc : ${mapCharc}")
        if (gattServices!!.uuid.toString().lowercase().startsWith("0000180f")) {
            gattBatteryServices = gattServices


            Log.i(TAG, "gattServices : ${gattServices!!.uuid}")
//            Log.i(TAG, "gattServices charc  : ${gattBatteryCharacteristic}")
            Log.i(TAG, "gattbattery : ${gattBatteryServices!!.uuid}")
        } else if (gattServices!!.uuid.toString().lowercase().startsWith("0000180a")) {
            gattDeviceInfoServices = gattServices
            Log.i(TAG, "gattServices : ${gattServices!!.uuid}")
            Log.i(TAG, "gattDevice : ${gattDeviceInfoServices!!.uuid}")
        } else {
            Log.i(TAG, "gattServices : ${gattServices!!.uuid}")
        }
        val characteristic = mapCharc[gattServices!!.uuid] as List<*>

        if (characteristic.isEmpty()) {
            bind.characteristicLinear.visibility = View.GONE
        } else {
            bind.characteristicLinear.visibility = View.VISIBLE
        }

        characteristicsAdapter = CharacteristicsAdapter(mContext, characteristic)
        characteristicsAdapter.notifyDataSetChanged()

        bind.characteristicRecyclerview.apply {
            adapter = characteristicsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "ONDETACH!!!!!!!!!!!!!!!!!!!")
        DataCenter.serviceDel()
        DataCenter.charcDel()
    }


}