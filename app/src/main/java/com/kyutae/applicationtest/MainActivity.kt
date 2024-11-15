package com.kyutae.applicationtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    private val REQUEST_ALL_PERMISSION = 2
    var bluetoothManager = Application.ApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    companion object{
         const val TAG_SETTING = "setting_fragment"
         const val TAG_MAIN = "main_fragment"
         const val TAG_MY_PAGE = "my_page_fragment"
         const val TAG_BABY = "baby_fragment"
    }

    private val TAG = this.javaClass.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        permissionCheck()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameLayout, MainFragment())
                .commit()
        }
//        setFragment(TAG_MAIN, MainFragment())

        //블루투스 이용 가능한지 체크하고 불가능 하면 끝낸다.
        packageManager.takeIf {
            it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        }?.also {
            return@onCreate finishForNoBluetooth()
        }

        bluetoothAdapter ?: return finishForNoBluetooth()
        if (bluetoothAdapter!!.isEnabled) {
            return
        }

        //블루투스 꺼있음 -> 킨다 -> 실패하면 앱종료
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        try {
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != RESULT_OK)
                    finishForNoBluetooth()
                else {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    startActivity(intent)
//                    scanDevices2()
                }
            }.launch(enableBtIntent)
        } catch (e: Exception) {
            finishForNoBluetooth()
            Log.e(TAG, "Exception : $e")
            e.printStackTrace()
        }
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                Log.i("APP SINGING :", "APP SINGING CHECK START!")
                Log.i("APP SINGING :", "CERTIFIED APP")
                Log.i("APP SINGING :", "APP VERSION CHECK START!")
                Log.i("APP VERSION : ", "1.0.0  >>  The Latest Version")
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    startActivity(intent)
                } else {
                    finishForNoBluetooth()
                }
            }
        }
    }
    private fun finishForNoBluetooth() {
        val myLayout = layoutInflater.inflate(R.layout.dialog_bluetooth_access, null)

        val builder = AlertDialog.Builder(this@MainActivity, R.style.MyDialogTheme).apply {
            setView(myLayout)
        }

        val accessBtn = myLayout.findViewById<Button>(R.id.access_btn)
        accessBtn.setOnClickListener {

            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"));
                startActivity(intent);
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace();
            }
            finish()

        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
}


