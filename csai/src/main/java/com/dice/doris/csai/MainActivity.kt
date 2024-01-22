package com.dice.doris.csai

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dice.doris.csai.R.id
import com.dice.doris.csai.R.layout
import com.dice.doris.csai.entity.Detail
import com.dice.doris.csai.entity.Playback
import com.dice.doris.csai.player.PlayerActivity
import com.dice.doris.csai.player.SourceHolder
import com.dice.doris.csai.util.DeviceUtils
import com.dice.doris.csai.util.OkHttpUtils
import com.dice.doris.csai.util.ParserUtils
import com.diceplatform.doris.entity.SourceBuilder
import okhttp3.Request
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var detail: Detail? = null
    private var playback: Playback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        initComponent()
    }

    @SuppressLint("CommitPrefEdits")
    private fun initComponent() {
        findViewById<View>(id.get_auth_token).setOnClickListener {
            //AuthUtil.getToken(this)
        }
        findViewById<View>(id.get_video_btn).setOnClickListener {
            getVideoDetail()
        }
        findViewById<View>(id.open_video_btn).setOnClickListener {
            if (detail == null) {
                Toast.makeText(this, "can not get detail feed.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (playback == null) {
                Toast.makeText(this, "can not get playback feed.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // this is demo, you can post all params to bundle or others.
            SourceHolder.source = SourceBuilder()
                .setId(detail!!.id.toString())
                .setUrl(playback!!.hls.first().url)
                .setImaCsaiProperties(ParserUtils.parseCsaiProperties(detail!!))
                .setTextTracks(ParserUtils.parseTextTrack(playback!!))
                .build()
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

        OkHttpUtils.getFeed(request, object : OkHttpUtils.Callback<Detail> {
            override fun onSuccess(result: Detail) {
                this@MainActivity.detail = result
                getPlaybackUrl(result.playerUrlCallback)
            }
        })
    }

    private fun getPlaybackUrl(url: String) {
        OkHttpUtils.getFeed(url, object : OkHttpUtils.Callback<Playback> {
            override fun onSuccess(result: Playback) {
                this@MainActivity.playback = result
            }
        })
    }
}
