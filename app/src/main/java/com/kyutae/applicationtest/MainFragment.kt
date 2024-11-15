package com.kyutae.applicationtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kyutae.applicationtest.MainActivity.Companion.TAG_SETTING
import com.kyutae.applicationtest.adapters.UserAdapter
import com.kyutae.applicationtest.bluetooth.BLEController
import com.kyutae.applicationtest.bluetooth.Utils
import com.kyutae.applicationtest.databinding.FragmentMainBinding
import com.kyutae.applicationtest.dataclass.BluetoothDataClass

class MainFragment : Fragment() {
    lateinit var bind: FragmentMainBinding
//    private lateinit var userAdapter: UserAdapter
    private val REQUEST_ENABLE_BT=1
    lateinit var PERMISSIONS: Array<String>
    private var mInterstitialAd: InterstitialAd? = null
    companion object{
        var bleGatt: BluetoothGatt? = null
        var devicesArr = mutableListOf<ScanResult>()
        lateinit var userAdapter: UserAdapter
        var bluetoothDataClass : BluetoothDataClass? = null
        var isScanning = MutableLiveData<Boolean>()


    }
    private val TAG = "MainFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentMainBinding.inflate(inflater, container, false)

        return bind.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAds()
        isScanning.value = false

        isScanning.observe(requireActivity()){
            if (it){
                bind.scanButton.text = "Scan Stop"
                bind.scanButton.setOnClickListener {
                    Utils.scanDevice(false)
                }
                bind.progressCircular.visibility = View.VISIBLE
            }else{
                bind.scanButton.text = "Scan Start"
                bind.scanButton.setOnClickListener {
                    Utils.scanDevice(true)
                }
                bind.progressCircular.visibility = View.GONE
            }
        }

        userAdapter = UserAdapter(requireContext(), devicesArr)
        userAdapter.notifyDataSetChanged()
        userAdapter.mListener = object : UserAdapter.OnItemClickListener {
            @SuppressLint("MissingPermission")
            override fun onClick(view: View, position: Int) {
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(requireActivity())
                    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Utils.scanDevice(false) // scan 중지
                            val device = devicesArr[position].device
                            Log.d(TAG, "ITEM ONCLICK")
                            bleGatt =  BLEController(context, bleGatt).connectGatt(device)


                            bluetoothDataClass = BluetoothDataClass(
                                view.findViewById<TextView>(R.id.name_txt).text.toString(),
                                view.findViewById<TextView>(R.id.address_txt).text.toString(),
                                view.findViewById<TextView>(R.id.rssi_txt).text.toString(),
                                view.findViewById<TextView>(R.id.type_txt).text.toString(),
                                view.findViewById<TextView>(R.id.company_key_txt).text.toString(),
                                view.findViewById<TextView>(R.id.company_value_txt).text.toString(),
                                view.findViewById<TextView>(R.id.service_uuid_txt).text.toString(),
                                view.findViewById<TextView>(R.id.service_data_txt).text.toString(),
                            )

                            println("bluetoothDataClass MAIN : $bluetoothDataClass")



                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.mainFrameLayout, SettingFragment())
                                .commit()
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            mInterstitialAd = null
                            setUpAds()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when ad fails to show.
                            Log.e(TAG, "Ad failed to show fullscreen content.")
                            mInterstitialAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.")
                        }
                    }
                } else {
                    Utils.scanDevice(false) // scan 중지
                    val device = devicesArr[position].device
                    Log.d(TAG, "ITEM ONCLICK")
                    bleGatt =  BLEController(context, bleGatt).connectGatt(device)


                    bluetoothDataClass = BluetoothDataClass(
                        view.findViewById<TextView>(R.id.name_txt).text.toString(),
                        view.findViewById<TextView>(R.id.address_txt).text.toString(),
                        view.findViewById<TextView>(R.id.rssi_txt).text.toString(),
                        view.findViewById<TextView>(R.id.type_txt).text.toString(),
                        view.findViewById<TextView>(R.id.company_key_txt).text.toString(),
                        view.findViewById<TextView>(R.id.company_value_txt).text.toString(),
                        view.findViewById<TextView>(R.id.service_uuid_txt).text.toString(),
                        view.findViewById<TextView>(R.id.service_data_txt).text.toString(),
                    )

                    println("bluetoothDataClass MAIN : $bluetoothDataClass")



                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFrameLayout, SettingFragment())
                        .commit()
                }



            }

        }
        bind.recyclerview.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun setUpAds(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(requireContext(),"ca-app-pub-6001930991464725/5149017413", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG," adError?.toString()")
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }
}