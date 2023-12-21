package com.kyutae.applicationtest

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyutae.applicationtest.MainActivity.Companion.TAG_SETTING
import com.kyutae.applicationtest.MainFragment.Companion.bluetoothDataClass
import com.kyutae.applicationtest.adapters.UserAdapter
import com.kyutae.applicationtest.bluetooth.BLEController
import com.kyutae.applicationtest.bluetooth.Utils
import com.kyutae.applicationtest.databinding.FragmentBabyBinding
import com.kyutae.applicationtest.databinding.FragmentMainBinding
import com.kyutae.applicationtest.databinding.FragmentSettingBinding
import com.kyutae.applicationtest.dataclass.BluetoothDataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.random.Random

class BabyFragment : Fragment() {
    lateinit var bind: FragmentBabyBinding
    private var bleGatt: BluetoothGatt? = null
    private val TAG = "BabyFragment"
    var timeDuration : Timer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentBabyBinding.inflate(inflater, container, false)


        return bind.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        bind.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startAction(true)
            } else {
                startAction(false)
            }
        }


    }

    fun startAction(boolean: Boolean){
        when(boolean){
            true -> {
                timeDuration = timer(period = 1000, initialDelay = 0) {
                    // 10 이상 100 이하의 랜덤한 정수 생성
                    val randomHeartValue = generateHeartRandomValue()
                    val randomO2Value = generateO2RandomValue()
                    bind.heartRate.text = "$randomHeartValue bpm"
                    bind.O2Rate.text = "$randomO2Value %"

                    heartStateView(randomHeartValue)
                    o2StateView(randomO2Value)
                }
            }
            false -> {
                timeDuration?.cancel()
            }
        }
    }

    fun generateHeartRandomValue(): Int {
        val randomNumber = Random.nextDouble(0.0, 1.0)
        return when {
            // 90% 확률로 90 이상의 값
            randomNumber > 0.1 -> Random.nextInt(100, 181)
            // 10% 확률로 90 미만의 값
            else -> {
                Random.nextInt(0, 201)
//                Random.nextInt(180, 201)
            }
        }
    }

    fun generateO2RandomValue(): Int {
        val randomNumber = Random.nextDouble(0.0, 1.0)
        return when {
            // 90% 확률로 90 이상의 값
            randomNumber > 0.1 -> Random.nextInt(90, 101)
            // 10% 확률로 90 미만의 값
            else -> Random.nextInt(0, 90)
        }
    }

    fun heartStateView(value : Int){
        when (value) {
            in 101 until 180 -> {
                bind.heartStateTxt.apply {
                    text = "Normal"
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }
            else -> {
                bind.heartStateTxt.apply {
                    text = "ALERT"
                    setTextColor(ContextCompat.getColor(context, R.color.red))
                }
            }
        }
    }
    fun o2StateView(value : Int){
        when (value) {
            in 90 until 101 -> {
                bind.O2StateTxt.apply {
                    text = "Normal"
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }
            else -> {
                bind.O2StateTxt.apply {
                    text = "ALERT"
                    setTextColor(ContextCompat.getColor(context, R.color.red))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeDuration?.cancel()
    }



}