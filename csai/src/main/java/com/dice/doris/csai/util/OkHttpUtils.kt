package com.dice.doris.csai.util

import android.os.Handler
import android.os.Looper
import com.dice.doris.csai.App
import com.dice.doris.csai.CsaiConfig
import com.dice.doris.csai.CsaiHeader
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
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

    fun getToken(callback: Callback<String>) {
        val request = Request.Builder()
            .url("${CsaiConfig.BASE_URL}/v2/login")
            .header(CsaiHeader.REALM, CsaiConfig.REALM)
            .header(CsaiHeader.API_KEY, CsaiConfig.API_KEY)
            .header(CsaiHeader.CONTENT_TYPE, "application/json")
            .post(
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    JSONObject().apply {
                        put("id", CsaiConfig.AUTH_NAME)
                        put("secret", CsaiConfig.AUTH_PASSWORD)
                    }.toString()
                )
            )
            .build()
        App.instance().httpClient
            .newCall(request)
            .enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    mainHandler.post { callback.onFailed(e) }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code in 200..299 && response.body != null) {
                        val data = JSONObject(response.body!!.string())
                        mainHandler.post { callback.onSuccess(data.getString("authorisationToken")) }
                    } else {
                        mainHandler.post { callback.onFailed(java.lang.Exception("Failed.")) }
                    }
                }
            })
    }
}