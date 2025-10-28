package com.kyutae.applicationtest

import android.app.Application
import android.content.Context
import com.kyutae.applicationtest.dataclass.DataCenter
import com.kyutae.applicationtest.utils.BleNotificationManager
import com.kyutae.applicationtest.utils.ThemeManager


class BluesCanApplication : Application() {

    companion object {

        lateinit var instance: BluesCanApplication

        fun ApplicationContext(): Context {
            return instance.applicationContext       // application context 관리
        }

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        init()
    }

    private fun init() {
        DataCenter.load()

        // 테마 적용
        ThemeManager.applyTheme(this)

        // 알림 채널 생성
        BleNotificationManager.createNotificationChannel(this)
    }

}
