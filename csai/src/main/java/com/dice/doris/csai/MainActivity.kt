package com.dice.doris.csai

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dice.doris.csai.R.id
import com.dice.doris.csai.R.layout
import com.dice.doris.csai.entity.LiveDetail
import com.dice.doris.csai.entity.LivePlayback
import com.dice.doris.csai.entity.VodDetail
import com.dice.doris.csai.entity.VodPlayback
import com.dice.doris.csai.player.PlayerActivity
import com.dice.doris.csai.player.SourceHolder
import com.dice.doris.csai.util.AuthUtil
import com.dice.doris.csai.util.DeviceUtils
import com.dice.doris.csai.util.OkHttpUtils
import com.dice.doris.csai.util.ParserUtils
import com.diceplatform.doris.entity.SourceBuilder
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@SuppressLint("CommitPrefEdits", "SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val stateInfo: TextView by lazy { findViewById(R.id.state_txt) }
    private var vodDetail: VodDetail? = null
    private var vodPlayback: VodPlayback? = null
    private var liveDetail: LiveDetail? = null
    private var livePlayback: LivePlayback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        initComponent()
    }

    private fun initComponent() {
        findViewById<CheckBox>(R.id.toggle_is_live).apply {
            isChecked = CsaiConfig.isLive
            setOnCheckedChangeListener { _, checked ->
                CsaiConfig.isLive = checked
            }
        }

        findViewById<View>(id.get_auth_token).setOnClickListener {
            stateInfo.text = "get token"
            AuthUtil.getToken(this)
        }
        findViewById<View>(id.get_video_btn).setOnClickListener {
            stateInfo.text = "get video detail"
            getVideoDetail()
        }
        findViewById<View>(id.open_video_btn).setOnClickListener {
            if (vodDetail == null && liveDetail == null) {
                Toast.makeText(this, "can not get detail feed.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (vodPlayback == null && livePlayback == null) {
                Toast.makeText(this, "can not get playback feed.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // this is demo, you can post all params to bundle or others.
            if (vodDetail != null && vodPlayback != null) {
                SourceHolder.source = SourceBuilder()
                    .setId(vodDetail!!.id.toString())
                    .setUrl(vodPlayback!!.hls.firstOrNull()?.url ?: vodPlayback!!.dash.firstOrNull()?.url)
                    .setImaCsaiProperties(ParserUtils.parseCsaiProperties(vodDetail!!.adsConfiguration))
                    .setTextTracks(ParserUtils.parseTextTrack(vodPlayback!!))
                    .build()
            } else {
                SourceHolder.source = SourceBuilder()
                    .setId(liveDetail!!.id.toString())
                    .setUrl(livePlayback!!.hls.url ?: livePlayback!!.hlsUrl)
                    .setImaCsaiProperties(ParserUtils.parseCsaiProperties(liveDetail!!.adsConfiguration))
                    .build()
            }
            startActivity(Intent(this, PlayerActivity::class.java))
        }
    }

    private fun getVideoDetail() {
        val authToken = getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "your token") ?: "your token"
        val shouldTrack = false // eg: track user
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
            .header(CsaiHeader.DVC_DNT, if (shouldTrack) "0" else "1")
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
                    stateInfo.text = "LiveDetail\n\n${formatObject(result.toString())}"
                    this@MainActivity.liveDetail = result
                    getLivePlaybackUrl(result.playerUrlCallback)
                }
            })
        } else {
            OkHttpUtils.getFeed(request, object : OkHttpUtils.Callback<VodDetail> {
                override fun onSuccess(result: VodDetail) {
                    stateInfo.text = "VodDetail\n\n${formatObject(result.toString())}"
                    this@MainActivity.vodDetail = result
                    getVodPlaybackUrl(result.playerUrlCallback)
                }
            })
        }
    }

    private fun getVodPlaybackUrl(url: String) {
        OkHttpUtils.getFeed(url, object : OkHttpUtils.Callback<VodPlayback> {
            override fun onSuccess(result: VodPlayback) {
                stateInfo.text = "VodPlayback\n\n${formatObject(result.toString())}"
                this@MainActivity.vodPlayback = result
            }
        })
    }

    private fun getLivePlaybackUrl(url: String) {
        OkHttpUtils.getFeed(url, object : OkHttpUtils.Callback<LivePlayback> {
            override fun onSuccess(result: LivePlayback) {
                stateInfo.text = "LivePlayback\n\n${formatObject(result.toString())}"
                this@MainActivity.livePlayback = result
            }
        })
    }

    private fun formatObject(data: String): String {
        return try {
            JSONObject(data).toString(2)
        } catch (e: Exception) {
            e.printStackTrace();
            data
        }
    }
}
