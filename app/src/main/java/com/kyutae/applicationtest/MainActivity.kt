package com.kyutae.applicationtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kyutae.applicationtest.bluetooth.BLEController
import com.kyutae.applicationtest.databinding.ActivityMainBinding
import com.kyutae.applicationtest.dataclass.DataCenter
import com.kyutae.applicationtest.utils.PermissionManager

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private var bleGatt: BluetoothGatt? = null
    private lateinit var permissionManager: PermissionManager

    var bluetoothManager = BluesCanApplication.ApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    companion object{
         const val TAG_SETTING = "setting_fragment"
         const val TAG_MAIN = "main_fragment"
         const val TAG_MY_PAGE = "my_page_fragment"
         const val TAG_BABY = "baby_fragment"
    }

    private val TAG = this.javaClass.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge 활성화
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        // WindowInsets 적용 (카메라 노치/시스템 바 영역 회피)
        val mainFrameLayout = findViewById<android.widget.FrameLayout>(R.id.mainFrameLayout)
        ViewCompat.setOnApplyWindowInsetsListener(mainFrameLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // 권한 관리자 초기화
        permissionManager = PermissionManager(
            context = this,
            onPermissionsGranted = {
                onPermissionsGranted()
            },
            onPermissionsDenied = { deniedPermissions ->
                onPermissionsDenied(deniedPermissions)
            }
        )
        permissionManager.registerPermissionLauncher(this)

        // BLE 하드웨어 확인
        if (!checkBluetoothSupport()) {
            return
        }

        // 권한 확인 및 요청
        permissionManager.checkAndRequestPermissions()

        // 재연결 인텐트 처리
        handleReconnectIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleReconnectIntent(intent)
    }

    /**
     * 자동 재연결 인텐트 처리
     */
    @SuppressLint("MissingPermission")
    private fun handleReconnectIntent(intent: Intent?) {
        if (intent?.action == "ACTION_AUTO_RECONNECT") {
            val deviceAddress = intent.getStringExtra("EXTRA_DEVICE_ADDRESS")
            if (deviceAddress != null) {
                Log.d(TAG, "Auto-reconnect requested for device: $deviceAddress")

                // 블루투스 어댑터에서 장치 가져오기
                try {
                    val device: BluetoothDevice = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                        ?: run {
                            Log.e(TAG, "Failed to get remote device")
                            Toast.makeText(this, R.string.error_device_not_found, Toast.LENGTH_SHORT).show()
                            return
                        }

                    // BLE 연결 시도
                    bleGatt = BLEController(this, bleGatt).connectGatt(device)
                    Toast.makeText(this, R.string.toast_reconnecting, Toast.LENGTH_SHORT).show()

                    // SettingFragment로 이동
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFrameLayout, SettingFragment())
                        .addToBackStack(null)
                        .commit()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Invalid device address: $deviceAddress", e)
                    Toast.makeText(this, R.string.error_invalid_address, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 권한이 부여되었을 때 호출
     */
    private fun onPermissionsGranted() {
        Log.d(TAG, "All permissions granted")

        if (supportFragmentManager.findFragmentById(R.id.mainFrameLayout) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameLayout, MainFragment())
                .commit()
        }

        // 블루투스 활성화 확인
        if (bluetoothAdapter?.isEnabled == false) {
            requestEnableBluetooth()
        }
    }

    /**
     * 권한이 거부되었을 때 호출
     */
    private fun onPermissionsDenied(deniedPermissions: List<String>) {
        Log.e(TAG, "Permissions denied: $deniedPermissions")
        Toast.makeText(
            this,
            "BLE 스캔을 위해 필요한 권한이 거부되었습니다.\n설정에서 권한을 허용해주세요.",
            Toast.LENGTH_LONG
        ).show()

        // 설정 화면으로 이동하는 다이얼로그 표시
        showPermissionDeniedDialog()
    }

    /**
     * 블루투스 지원 여부 확인
     */
    private fun checkBluetoothSupport(): Boolean {
        if (packageManager.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_LONG).show()
            finishForNoBluetooth()
            return false
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_disabled, Toast.LENGTH_LONG).show()
            finishForNoBluetooth()
            return false
        }

        return true
    }

    /**
     * 블루투스 활성화 요청
     */
    private fun requestEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        try {
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) {
                    Toast.makeText(this, R.string.dialog_bluetooth_message, Toast.LENGTH_LONG).show()
                    finishForNoBluetooth()
                } else {
                    Log.d(TAG, "Bluetooth enabled successfully")
                }
            }.launch(enableBtIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while enabling Bluetooth: $e")
            e.printStackTrace()
            finishForNoBluetooth()
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

    /**
     * 권한 거부 시 설정으로 이동하는 다이얼로그 표시
     */
    private fun showPermissionDeniedDialog() {
        val myLayout = layoutInflater.inflate(R.layout.dialog_bluetooth_access, null)

        val builder = AlertDialog.Builder(this, R.style.MyDialogTheme).apply {
            setView(myLayout)
        }

        val accessBtn = myLayout.findViewById<Button>(R.id.access_btn)
        accessBtn.text = "설정으로 이동"
        accessBtn.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
            finish()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        DataCenter.serviceDel()
        Log.e(TAG, "$TAG  : onDestroy")
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


