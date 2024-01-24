package com.dice.doris.csai.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import com.dice.doris.csai.CsaiConfig
import com.dice.doris.csai.CsaiHeader
import com.dice.doris.csai.entity.AdsConfiguration
import com.dice.doris.csai.entity.LivePlayback
import com.dice.doris.csai.entity.VideoInfo
import com.dice.doris.csai.entity.VodDetail
import com.dice.doris.csai.entity.VodPlayback
import com.dice.doris.csai.util.OkHttpUtils.Callback
import okhttp3.Request
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

object SourceUtils {
    interface SourceCallback {
        fun onSourceCallback(videoInfo: VideoInfo, adsConfiguration: AdsConfiguration?)
    }

    fun getSource(context: Context, callback: SourceCallback) {
        // #1. get your auth token first.
        OkHttpUtils.getToken(object : Callback<String> {
            override fun onSuccess(result: String) {
                getVideoDetail(context, result, callback)
            }

            override fun onFailed(e: Exception) {
                context.showToast("get token failed.")
            }
        })
    }

    private fun getVideoDetail(context: Context, authToken: String, callback: SourceCallback) {
        val request = Request.Builder()
            .url(CsaiConfig.videoUrl)
            .header(CsaiHeader.AUTH, "Bearer $authToken") //your auth token, you should set value in CSAIConfig.AUTH_NAME
            .header(CsaiHeader.API_KEY, CsaiConfig.API_KEY)
            .header(CsaiHeader.REALM, CsaiConfig.REALM)
            .header(CsaiHeader.CONTENT_TYPE, "application/json")
            .header(CsaiHeader.APP_NAME, context.getString(context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.labelRes))
            .header(CsaiHeader.APP_VERSION, context.packageManager.getPackageInfo(context.packageName, 0).versionName)
            .header(CsaiHeader.APP_BUNDLE, context.packageName)
            .header(CsaiHeader.APP_STOREID, context.packageName)
            .header(CsaiHeader.DVC_DNT, if (CsaiConfig.SHOULD_TRACK_USER) "0" else "1")
            .header(CsaiHeader.DVC_H, context.resources.displayMetrics.heightPixels.toString())
            .header(CsaiHeader.DVC_W, context.resources.displayMetrics.widthPixels.toString())
            .header(CsaiHeader.DVC_LANG, Locale.getDefault().language) // language
            .header(CsaiHeader.DVC_OS, "2") // 2:android
            .header(CsaiHeader.DVC_OSV, Build.VERSION.SDK_INT.toString()) // OS version
            .header(CsaiHeader.DVC_TYPE, if (isPad(context)) "5" else "4") // 5:tablet, 4:phone
            .header(CsaiHeader.DVC_MAKE, Build.MANUFACTURER)
            .header(CsaiHeader.DVC_MODEL, Build.MODEL)
            .header(CsaiHeader.CST_TCF, CsaiConfig.CMP_TCF) // GDPR TCF string
            .header(CsaiHeader.CST_USP, CsaiConfig.CMP_USP) // CCPA us privacy string
            .build()

        OkHttpUtils.getFeed(request, object : Callback<VodDetail> {
            override fun onSuccess(result: VodDetail) {
                getPlaybackUrl(context, result, callback)
            }

            override fun onFailed(e: Exception) {
                context.showToast("get video detail failed.")
            }
        })
    }

    private fun getPlaybackUrl(context: Context, videoDetail: VodDetail, callback: SourceCallback) {
        if (videoDetail.playerUrlCallback != null) {
            if (CsaiConfig.isLive) {
                OkHttpUtils.getFeed(videoDetail.playerUrlCallback, object : Callback<LivePlayback> {
                    override fun onSuccess(result: LivePlayback) {
                        val videoInfo = parseSteam(result.hls?.let { listOf(it) }, result.dash?.let { listOf(it) })
                        videoInfo?.let {
                            callback.onSourceCallback(it, videoDetail.adsConfiguration)
                        }
                    }

                    override fun onFailed(e: Exception) {
                        context.showToast("get playback failed.")
                    }
                })
            } else {
                OkHttpUtils.getFeed(videoDetail.playerUrlCallback, object : Callback<VodPlayback> {
                    override fun onSuccess(result: VodPlayback) {
                        val videoInfo = parseSteam(result.hls, result.dash)
                        videoInfo?.let {
                            callback.onSourceCallback(it, videoDetail.adsConfiguration)
                        }
                    }

                    override fun onFailed(e: Exception) {
                        context.showToast("get playback failed.")
                    }
                })
            }
        } else {
            context.showToast("get video detail playerUrlCallback is null.")
        }
    }

    private fun parseSteam(hls: List<VideoInfo>?, dash: List<VideoInfo>?): VideoInfo? {
        val isDrm = hls?.find { it.drm != null } != null || dash?.find { it.drm != null } != null
        if (isDrm) {
            return hls?.find { it.drm?.keySystems?.contains("WIDEVINE") == true } ?: dash?.first()
        }
        return hls?.first()
    }

    private fun isPad(context: Context): Boolean {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val dm = DisplayMetrics()
        display.getMetrics(dm)
        val x = (dm.widthPixels / dm.xdpi).toDouble().pow(2.0)
        val y = (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)
        val screenInches = sqrt(x + y) // screen size
        return screenInches >= 7.0
    }
}

private fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}