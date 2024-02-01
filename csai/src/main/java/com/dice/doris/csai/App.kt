package com.dice.doris.csai

import android.app.Application
import okhttp3.OkHttpClient

class App : Application() {
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: App
        fun instance() = instance
    }
}