package com.kyutae.applicationtest.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kyutae.applicationtest.MainActivity
import com.kyutae.applicationtest.R

/**
 * 알림 관리 클래스
 */
object BleNotificationManager {

    private const val CHANNEL_ID = "ble_scanner_channel"
    private const val CHANNEL_NAME = "BLE Scanner"
    private const val CHANNEL_DESCRIPTION = "BLE 장치 연결 및 스캔 알림"

    /**
     * 알림 채널 생성 (Android O 이상)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 장치 발견 알림
     */
    fun showDeviceFoundNotification(context: Context, deviceName: String, deviceAddress: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("새로운 BLE 장치 발견")
            .setContentText("$deviceName ($deviceAddress)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(deviceAddress.hashCode(), notification)
    }

    /**
     * 연결 상태 알림
     */
    fun showConnectionNotification(context: Context, deviceName: String, isConnected: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isConnected) "장치 연결됨" else "장치 연결 해제됨"
        val icon = if (isConnected)
            android.R.drawable.stat_sys_data_bluetooth
        else
            android.R.drawable.stat_sys_warning

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(deviceName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }

    /**
     * 배터리 부족 알림
     */
    fun showLowBatteryNotification(context: Context, deviceName: String, batteryLevel: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("장치 배터리 부족")
            .setContentText("$deviceName 배터리: $batteryLevel%")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1002, notification)
    }

    /**
     * 재연결 시도 알림
     */
    fun showReconnectNotification(context: Context, deviceAddress: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_AUTO_RECONNECT"
            putExtra("EXTRA_DEVICE_ADDRESS", deviceAddress)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("BLE 장치 재연결")
            .setContentText("$deviceAddress 장치에 재연결을 시도합니다")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_revert,
                "재연결",
                pendingIntent
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1003, notification)
    }
}
