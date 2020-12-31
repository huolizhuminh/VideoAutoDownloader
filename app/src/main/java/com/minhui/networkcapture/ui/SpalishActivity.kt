package com.minhui.networkcapture.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import com.minhui.networkcapture.MainCaptureActivity
import com.minhui.networkcapture.R
import com.minhui.networkcapture.utils.AppConstants


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SpalishActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = getWindow()
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.GRAY
        }
        setContentView(R.layout.activity_spalish)
        Handler().postDelayed(Runnable {
            var intent = Intent(this, MainCaptureActivity::class.java)
            startActivity(intent)
            finish()
        }, 600)
    }
}
