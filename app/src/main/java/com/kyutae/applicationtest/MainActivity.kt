package com.kyutae.applicationtest

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kyutae.applicationtest.databinding.ActivityMainBinding
import com.kyutae.applicationtest.dataclass.DataCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding
//    private lateinit var userAdapter: UserAdapter
    private var bleGatt: BluetoothGatt? = null
    private val REQUEST_ENABLE_BT=1
    lateinit var PERMISSIONS: Array<String>

    companion object{
         const val TAG_SETTING = "setting_fragment"
         const val TAG_MAIN = "main_fragment"
         const val TAG_MY_PAGE = "my_page_fragment"
         const val TAG_BABY = "baby_fragment"
    }

//    companion object{
//        var devicesArr = mutableListOf<ScanResult>()
//        lateinit var userAdapter: UserAdapter
//    }
    private val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        permissionCheck()


        setFragment(TAG_MAIN, MainFragment())


//        navigationView.setOnItemSelectedListener { item ->
//            when(item.itemId) {
//                R.id.mainFragment -> setFragment(TAG_MAIN, MainFragment())
//                R.id.logFragment -> setFragment(TAG_MY_PAGE, LogFragment())
//                R.id.settingFragment -> setFragment(TAG_SETTING, SettingFragment())
//            }
//            true
//        }

    }



    fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null){
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
            fragTransaction.addToBackStack(null)
        }

        val setting = manager.findFragmentByTag(TAG_SETTING)
        val main = manager.findFragmentByTag(TAG_MAIN)
        val myPage = manager.findFragmentByTag(TAG_MY_PAGE)
        val baby = manager.findFragmentByTag(TAG_BABY)

        if (setting != null){
            fragTransaction.hide(setting)
//            fragTransaction.detach(setting)
        }
        if (baby != null){
            fragTransaction.hide(baby)
//            fragTransaction.detach(setting)
        }

        if (main != null){
            fragTransaction.hide(main)
//            fragTransaction.detach(main)
        }

        if (myPage != null) {
//            fragTransaction.hide(myPage)
            fragTransaction.detach(myPage)
        }

        if (tag == TAG_SETTING) {
            if (setting!=null){
//                fragTransaction.show(setting)
                fragTransaction.attach(setting)
            }
        } else if (tag == TAG_MAIN) {
            if (main != null) {
                fragTransaction.show(main)
//                fragTransaction.attach(main)
            }
        } else if (tag == TAG_MY_PAGE){
            if (myPage != null){
//                fragTransaction.show(myPage)
                fragTransaction.attach(myPage)
            }
        } else if (tag == TAG_BABY){
            if (baby != null){
//                fragTransaction.show(myPage)
                fragTransaction.attach(baby)
            }
        }

        fragTransaction.commitAllowingStateLoss()
    }

    private fun permissionCheck(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PERMISSIONS = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        val permissionsToRequest = ArrayList<String>()
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_ENABLE_BT
            )
        } else {
            // 이미 모든 권한이 있는 경우 처리할 작업을 여기에 추가
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        DataCenter.serviceDel()
        Log.e(TAG, "$TAG  : onDestroy")
    }
}














