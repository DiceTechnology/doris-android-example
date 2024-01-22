package com.dice.doris.csai.util

import com.dice.doris.csai.App
import com.dice.doris.csai.entity.Playback
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object OkHttpUtils {

    interface Callback<T> {
        fun onSuccess(result: T) {}
        fun onFailed(e: Exception) {}
    }

    inline fun <reified T> getFeed(url: String, callback: Callback<T>) {
        getFeed(Request.Builder().url(url).build(), callback)
    }

    inline fun <reified T> getFeed(request: Request, callback: Callback<T>) {
        App.instance().httpClient
            .newCall(request)
            .enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailed(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val result = response.body?.string()?.let { GsonUtils.fromJson<T>(it) }
                        if (result == null) {
                            callback.onFailed(java.lang.Exception("Parse Failed."))
                        } else {
                            callback.onSuccess(result)
                        }
                    } else {
                        callback.onFailed(java.lang.Exception("Http Failed, please check"))
                    }
                }

            })
    }
}