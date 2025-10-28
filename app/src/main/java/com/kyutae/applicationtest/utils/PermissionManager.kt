package com.kyutae.applicationtest.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * 중앙화된 권한 관리 클래스
 * BLE 스캔에 필요한 권한을 관리합니다
 */
class PermissionManager(
    private val context: Context,
    private val onPermissionsGranted: () -> Unit,
    private val onPermissionsDenied: (List<String>) -> Unit
) {

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    companion object {
        /**
         * Android 버전에 따라 필요한 BLE 권한 목록을 반환합니다
         */
        fun getRequiredPermissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 (API 31) 이상
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                // Android 11 (API 30) 이하
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        }

        /**
         * 모든 필수 권한이 부여되었는지 확인합니다
         */
        fun hasAllPermissions(context: Context): Boolean {
            return getRequiredPermissions().all { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * 부여되지 않은 권한 목록을 반환합니다
         */
        fun getMissingPermissions(context: Context): List<String> {
            return getRequiredPermissions().filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Activity에서 권한 요청 Launcher를 등록합니다
     */
    fun registerPermissionLauncher(activity: AppCompatActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handlePermissionResult(permissions)
        }
    }

    /**
     * Fragment에서 권한 요청 Launcher를 등록합니다
     */
    fun registerPermissionLauncher(fragment: Fragment) {
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handlePermissionResult(permissions)
        }
    }

    /**
     * 권한 요청 결과를 처리합니다
     */
    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value }.keys.toList()

        if (deniedPermissions.isEmpty()) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied(deniedPermissions)
        }
    }

    /**
     * 권한 요청을 시작합니다
     */
    fun requestPermissions() {
        val missingPermissions = getMissingPermissions(context)

        if (missingPermissions.isEmpty()) {
            onPermissionsGranted()
        } else {
            permissionLauncher?.launch(missingPermissions.toTypedArray())
                ?: throw IllegalStateException("Permission launcher not registered. Call registerPermissionLauncher() first.")
        }
    }

    /**
     * 권한 확인 및 요청 (필요한 경우)
     */
    fun checkAndRequestPermissions() {
        if (hasAllPermissions(context)) {
            onPermissionsGranted()
        } else {
            requestPermissions()
        }
    }

    /**
     * 특정 권한이 부여되었는지 확인합니다
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    /**
     * BLE 스캔 권한이 있는지 확인합니다
     */
    fun hasBleScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * BLE 연결 권한이 있는지 확인합니다
     */
    fun hasBleConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            true // Android 11 이하에서는 별도 권한 불필요
        }
    }
}
