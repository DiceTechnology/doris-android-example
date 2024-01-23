package com.dice.doris.csai

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dice.doris.csai.R.id
import com.dice.doris.csai.R.layout
import com.dice.doris.csai.entity.LiveDetail
import com.dice.doris.csai.entity.LivePlayback
import com.dice.doris.csai.entity.VodDetail
import com.dice.doris.csai.entity.VodPlayback
import com.dice.doris.csai.player.PlayerActivity
import com.dice.doris.csai.player.PlayerActivity.Companion.source
import com.dice.doris.csai.util.DeviceUtils
import com.dice.doris.csai.util.OkHttpUtils
import com.dice.doris.csai.util.OkHttpUtils.Callback
import com.dice.doris.csai.util.ParserUtils
import com.diceplatform.doris.entity.SourceBuilder
import okhttp3.Request
import java.util.Locale

@SuppressLint("CommitPrefEdits", "SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val handler = AutoHandler(Looper.getMainLooper())
    private val progress: ProgressBar by lazy { findViewById(R.id.progress) }
    private var vodDetail: VodDetail? = null
    private var vodPlayback: VodPlayback? = null
    private var liveDetail: LiveDetail? = null
    private var livePlayback: LivePlayback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        initComponent()
        handler.start()
    }

    private fun initComponent() {
        CsaiConfig.isLive = getAppPreferences().getBoolean("isLive", CsaiConfig.isLive)
        findViewById<CheckBox>(id.toggle_is_live).apply {
            isChecked = CsaiConfig.isLive
            setOnCheckedChangeListener { _, checked ->
                CsaiConfig.isLive = checked
                getAppPreferences().edit().putBoolean("isLive", checked).apply()
            }
        }

        findViewById<View>(id.start_video).setOnClickListener {
            handler.start(delayMillis = 0L)
        }
    }

    private fun getVideo() {
        progress.visibility = View.VISIBLE
        vodDetail = null
        vodDetail = null
        liveDetail = null
        livePlayback = null
        OkHttpUtils.getToken(object : Callback<String> {
            override fun onSuccess(result: String) {
                getVideoDetail(result)
            }

            override fun onFailed(e: Exception) {
                Toast.makeText(this@MainActivity, "get token failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getVideoDetail(authToken: String) {
        val request = Request.Builder()
            .url(CsaiConfig.videoUrl)
            .header(CsaiHeader.AUTH, "Bearer $authToken") //your token
            .header(CsaiHeader.API_KEY, CsaiConfig.API_KEY)
            .header(CsaiHeader.REALM, CsaiConfig.REALM)
            .header(CsaiHeader.CONTENT_TYPE, "application/json")
            .header(CsaiHeader.APP_NAME, getString(packageManager.getPackageInfo(packageName, 0).applicationInfo.labelRes))
            .header(CsaiHeader.APP_VERSION, packageManager.getPackageInfo(packageName, 0).versionName)
            .header(CsaiHeader.APP_BUNDLE, packageName)
            .header(CsaiHeader.APP_STOREID, packageName)
            .header(CsaiHeader.DVC_DNT, if (CsaiConfig.SHOULD_TRACK_USER) "0" else "1")
            .header(CsaiHeader.DVC_H, resources.displayMetrics.heightPixels.toString())
            .header(CsaiHeader.DVC_W, resources.displayMetrics.widthPixels.toString())
            .header(CsaiHeader.DVC_LANG, Locale.getDefault().language)
            .header(CsaiHeader.DVC_OS, "2") // 2:android
            .header(CsaiHeader.DVC_OSV, Build.VERSION.SDK_INT.toString())
            .header(CsaiHeader.DVC_TYPE, if (DeviceUtils.isPad(this)) "5" else "4") // 5:tablet, 4:phone
            .header(CsaiHeader.DVC_MAKE, Build.MANUFACTURER)
            .header(CsaiHeader.DVC_MODEL, Build.MODEL)
            .header(CsaiHeader.CST_TCF, CsaiConfig.CMP_TCF)
            .header(CsaiHeader.CST_USP, CsaiConfig.CMP_USP)
            .build()

        if (CsaiConfig.isLive) {
            OkHttpUtils.getFeed(request, object : OkHttpUtils.Callback<LiveDetail> {
                override fun onSuccess(result: LiveDetail) {
                    liveDetail = result
                    getLivePlaybackUrl(result.playerUrlCallback)
                }

                override fun onFailed(e: Exception) {
                    Toast.makeText(this@MainActivity, "get video detail failed.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            OkHttpUtils.getFeed(request, object : OkHttpUtils.Callback<VodDetail> {
                override fun onSuccess(result: VodDetail) {
                    vodDetail = result
                    getVodPlaybackUrl(result.playerUrlCallback)
                }

                override fun onFailed(e: Exception) {
                    Toast.makeText(this@MainActivity, "get video detail failed.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getVodPlaybackUrl(url: String) {
        OkHttpUtils.getFeed(url, object : OkHttpUtils.Callback<VodPlayback> {
            override fun onSuccess(result: VodPlayback) {
                vodPlayback = result
                openVideo()
            }

            override fun onFailed(e: Exception) {
                Toast.makeText(this@MainActivity, "get playback failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getLivePlaybackUrl(url: String) {
        OkHttpUtils.getFeed(url, object : OkHttpUtils.Callback<LivePlayback> {
            override fun onSuccess(result: LivePlayback) {
                livePlayback = result
                openVideo()
            }

            override fun onFailed(e: Exception) {
                Toast.makeText(this@MainActivity, "get playback failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openVideo() {
        progress.visibility = View.INVISIBLE
        if (vodDetail == null && liveDetail == null) {
            Toast.makeText(this, "can not get detail feed, please check!", Toast.LENGTH_SHORT).show()
            return
        }
        if (vodPlayback == null && livePlayback == null) {
            Toast.makeText(this, "can not get playback feed, please check!", Toast.LENGTH_SHORT).show()
            return
        }
        // this is demo, you can post all params to bundle or others.
        // this is demo, you can post all params to bundle or others.
        // this is demo, you can post all params to bundle or others.
        if (vodDetail != null && vodPlayback != null) {
            PlayerActivity.startActivity(
                this, SourceBuilder()
                    .setId(vodDetail!!.id.toString())
                    .setUrl(vodPlayback!!.hls.firstOrNull()?.url ?: vodPlayback!!.dash.firstOrNull()?.url)
                    .setImaCsaiProperties(ParserUtils.parseCsaiProperties(vodDetail!!.adsConfiguration))
                    .setTextTracks(ParserUtils.parseTextTrack(vodPlayback!!))
                    .build()
            )
        } else if (liveDetail != null && livePlayback != null) {
            PlayerActivity.startActivity(
                this, SourceBuilder()
                    .setId(liveDetail!!.id.toString())
                    .setUrl(livePlayback!!.hls.url) //TODO
                    .setImaCsaiProperties(ParserUtils.parseCsaiProperties(liveDetail!!.adsConfiguration))
                    .build()
            )
        }
    }

    private fun getAppPreferences(): SharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE)

    private inner class AutoHandler(looper: Looper) : Handler(looper) {
        private val msgGetVideo: Int = 1

        override fun handleMessage(msg: Message) {
            if (msg.what == msgGetVideo) {
                getVideo()
            }
        }

        fun start(delayMillis: Long = 3 * 1000) {
            removeMessages(msgGetVideo)
            sendEmptyMessageDelayed(msgGetVideo, delayMillis)
        }
    }
}


