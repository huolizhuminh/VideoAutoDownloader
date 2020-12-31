package com.minhui.networkcapture.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.minhui.networkcapture.R
import com.minhui.networkcapture.utils.ContextUtil


class WebShowActivity : AppCompatActivity() {
    companion object{
        const val  SHOW_URL ="show_web_url"
        const val  TITLE = "title"
        fun statWebActivity(context: Context, title:String, url:String){
            val intent = Intent(context,WebShowActivity::class.java)
            intent.putExtra(SHOW_URL,url)
            intent.putExtra(TITLE,title)
            context.startActivity(intent)
        }
        fun startUsePolicy(context: Context){
            val local = ContextUtil.getLocal(context)
            if (local.endsWith("zh")){
                statWebActivity(context,context.getString(R.string.use_license),"file:///android_asset/use_license_zh.html")
            }else{
                statWebActivity(context,context.getString(R.string.use_license),"file:///android_asset/use_license_zh.html")
            }
        }
        fun startPrivacyPolicy(context: Context){
            val local = ContextUtil.getLocal(context)
            if (local.endsWith("zh")){
                statWebActivity(context,context.getString(R.string.privacy_policy),"file:///android_asset/privacy_zh.html")
            }else{
                statWebActivity(context,context.getString(R.string.privacy_policy),"file:///android_asset/privacy.html")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_show)
        val titleStr = intent.getStringExtra(TITLE)
        title =titleStr;
        val webStr = intent.getStringExtra(SHOW_URL);
        val webView = findViewById<WebView>(R.id.web_view)
        webView.loadUrl(webStr)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish() // back button
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
