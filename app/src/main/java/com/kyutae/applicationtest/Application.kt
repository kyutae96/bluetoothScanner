package com.kyutae.applicationtest

import android.app.Application
import android.content.Context
import com.kyutae.applicationtest.dataclass.DataCenter


class Application : Application() {

    companion object {

        lateinit var application: Application

        fun ApplicationContext(): Context {
            return application.applicationContext       // application context 관리
        }

    }

    override fun onCreate() {
        super.onCreate()
        application = this


        init()

    }


    private fun init() {
        DataCenter.load()

    }

}
