package com.kyutae.applicationtest.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * 자동 재연결 관리 클래스
 */
object AutoReconnectManager {

    private const val PREFS_NAME = "auto_reconnect_prefs"
    private const val KEY_AUTO_RECONNECT_ENABLED = "auto_reconnect_enabled"
    private const val KEY_TARGET_DEVICE_ADDRESS = "target_device_address"
    private const val RECONNECT_WORK_NAME = "ble_reconnect_work"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 자동 재연결 활성화
     */
    fun enableAutoReconnect(context: Context, deviceAddress: String) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_AUTO_RECONNECT_ENABLED, true)
            putString(KEY_TARGET_DEVICE_ADDRESS, deviceAddress)
            apply()
        }

        // WorkManager로 주기적인 재연결 시도 스케줄
        val reconnectRequest = PeriodicWorkRequestBuilder<ReconnectWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setInputData(
                workDataOf(KEY_TARGET_DEVICE_ADDRESS to deviceAddress)
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RECONNECT_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reconnectRequest
        )
    }

    /**
     * 자동 재연결 비활성화
     */
    fun disableAutoReconnect(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_AUTO_RECONNECT_ENABLED, false)
            remove(KEY_TARGET_DEVICE_ADDRESS)
            apply()
        }

        WorkManager.getInstance(context).cancelUniqueWork(RECONNECT_WORK_NAME)
    }

    /**
     * 자동 재연결 활성화 상태 확인
     */
    fun isAutoReconnectEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_RECONNECT_ENABLED, false)
    }

    /**
     * 타겟 장치 주소 가져오기
     */
    fun getTargetDeviceAddress(context: Context): String? {
        return getPrefs(context).getString(KEY_TARGET_DEVICE_ADDRESS, null)
    }

    /**
     * 재연결 Worker
     */
    class ReconnectWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {

        override fun doWork(): Result {
            val deviceAddress = inputData.getString(KEY_TARGET_DEVICE_ADDRESS)
                ?: return Result.failure()

            if (!isAutoReconnectEnabled(applicationContext)) {
                return Result.success()
            }

            // 재연결 알림 표시
            // 사용자가 알림을 클릭하면 MainActivity가 열리고 재연결 시도
            BleNotificationManager.showReconnectNotification(applicationContext, deviceAddress)

            return Result.success()
        }
    }
}
