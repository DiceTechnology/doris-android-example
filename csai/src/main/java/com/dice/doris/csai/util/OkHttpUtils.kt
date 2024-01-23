package com.dice.doris.csai.util

import android.os.Handler
import android.os.Looper
import com.dice.doris.csai.App
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object OkHttpUtils {

    interface Callback<T> {
        fun onSuccess(result: T) {}
        fun onFailed(e: Exception) {}
    }

    val mainHandler = Handler(Looper.getMainLooper())

    inline fun <reified T> getFeed(url: String, callback: Callback<T>) {
        getFeed(Request.Builder().url(url).build(), callback)
    }

    inline fun <reified T> getFeed(request: Request, callback: Callback<T>) {
        App.instance().httpClient
            .newCall(request)
            .enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    mainHandler.post { callback.onFailed(e) }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val result = response.body?.string()?.let { GsonUtils.fromJson<T>(it) }
                        if (result == null) {
                            mainHandler.post { callback.onFailed(java.lang.Exception("Parse Failed.")) }
                        } else {
                            mainHandler.post { callback.onSuccess(result) }
                        }
                    } else {
                        mainHandler.post { callback.onFailed(java.lang.Exception("Http Failed, please check")) }
                    }
                }

            })
    }
}