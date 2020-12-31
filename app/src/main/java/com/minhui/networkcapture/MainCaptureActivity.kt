package com.minhui.networkcapture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.security.KeyChain
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.util.IOUtils
import com.google.android.material.navigation.NavigationView
import com.minhui.networkcapture.ads.AdsConstant
import com.minhui.networkcapture.ads.banner.MyAdsView
import com.minhui.networkcapture.ads.interstitial.InterstitialManager
import com.minhui.networkcapture.base.BaseActivity
import com.minhui.networkcapture.ui.CaptureFilterActivity
import com.minhui.networkcapture.utils.AppConstants
import com.minhui.networkcapture.utils.ContextUtil
import com.minhui.vpn.ProxyConfig
import com.minhui.vpn.ProxyConfig.VpnStatusListener
import com.minhui.vpn.VpnServiceHelper
import com.minhui.vpn.log.VPNLog
import java.io.FileInputStream

class MainCaptureActivity : BaseActivity() {
    companion object {
        private const val LOG_TAG: String = "MainCaptureActivity"
        const val OVERLAY_PERMISSION_REQ_CODE = 106;
    }

    private var hasAskOpenFloat: Boolean = false
    private var hasAskInstallCert: Boolean = false
    private val ONE_HOUR = 24 * 3600 * 1000L
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var vpnButton: TextView
    private lateinit var appBarConfiguration: AppBarConfiguration
    var vpnStatusListener: VpnStatusListener = object : VpnStatusListener {
        override fun onVpnStart(context: Context) {
            Log.d(LOG_TAG, "onVpnStart")
            handler.post(Runnable { vpnButton.setText(R.string.stop_capture) })
        }

        override fun onVpnEnd(context: Context) {
            Log.d(LOG_TAG, "onVpnEnd")
            handler.post(Runnable { vpnButton.setText(R.string.start_capture) })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // setContentView(R.layout.activity_vpn_capture);
        vpnButton = findViewById<View>(R.id.run_state) as TextView
        vpnButton.setOnClickListener(View.OnClickListener {
            if (VpnServiceHelper.vpnRunningStatus()) {
                closeVpn()
            } else{
                startVPN()
            }
        })
        sharedPreferences = getSharedPreferences(AppConstants.DATA_SAVE, Context.MODE_PRIVATE)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_favorite,R.id.nav_history, R.id.nav_setting), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        handler = Handler()
        if (ContextUtil.isProVersion(applicationContext)) {
            checkReview()
        } /*else {
            recommendPro(getString(R.string.pro_recommend))
        }*/

        ProxyConfig.Instance.registerVpnStatusListener(vpnStatusListener)
        initAdas()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VpnServiceHelper.START_VPN_SERVICE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            VpnServiceHelper.startVpnService(applicationContext)
        } /*else if (requestCode == AppConstants.REQUEST_CERT && resultCode == Activity.RESULT_OK) {
            sharedPreferences.edit().putBoolean(AppConstants.HAS_INSTALL_ROOT_CERTIFICATE, true).apply()
            performOpenVpn()
        }*/
    }

    private fun initAdas() {
        handler.postDelayed(Runnable {
            VPNLog.d(LOG_TAG, "start add ads")
            InterstitialManager.getInstance().initContext(applicationContext)
            (findViewById<View>(R.id.ads_container) as MyAdsView).initAds(AdsConstant.MAIN_BANNER_ID)
        }, 2000)
    }

    private fun checkReview() { //推荐用户进行留评
        if (!ContextUtil.isGooglePlayChannel(applicationContext)) {
            return
        }
        val hasFullUseApp = sharedPreferences.getBoolean(AppConstants.HAS_FULL_USE_APP, false)
        if (!hasFullUseApp) {
            return
        }
        val hasShowRecommand = sharedPreferences.getBoolean(AppConstants.HAS_SHOW_RECOMMEND, false)
        if (hasShowRecommand) {
            return
        }
        val lastRecommand = sharedPreferences.getLong(AppConstants.LAST_RECOMMAND_TIME, 0)
        //1个小时内不再提醒
        if (System.currentTimeMillis() - lastRecommand < ONE_HOUR) {
            return
        }
        sharedPreferences.edit().putLong(AppConstants.LAST_RECOMMAND_TIME, System.currentTimeMillis()).apply()
        showRecommend()
    }

    private fun showRecommend() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.do_you_like_the_app))
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    showGotoStarDialog()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, which ->
                    showSolveProblemDialog()
                    dialog.dismiss()
                    sharedPreferences.edit().putBoolean(AppConstants.HAS_SHOW_RECOMMEND, true).apply()
                }
                .show()
    }

    private fun showGotoStarDialog() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.do_you_want_star))
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    val appPackageName = packageName
                    try {
                        val appStoreIntent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$appPackageName"))
                        appStoreIntent.setPackage("com.android.vending")
                        startActivity(appStoreIntent)
                    } catch (e: java.lang.Exception) {
                        try {
                            val url = "https://play.google.com/store/apps/details?id=$appPackageName"
                            launchBrowser(url)
                        } catch (se: java.lang.Exception) {
                        }
                    }
                    sharedPreferences.edit().putBoolean(AppConstants.HAS_SHOW_RECOMMEND, true).apply()
                }
                .setNegativeButton(getString(R.string.not_now)) { dialog, which -> dialog.dismiss() }
                .show()
    }


    private fun showSolveProblemDialog() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.any_device_send_email))
                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun closeVpn() {
        VpnServiceHelper.changeVpnRunningStatus(this, false, resources.getString(R.string.app_name))
        VpnServiceHelper.needCapture = false
    }

    private fun startVPN() {
        SSLVPNUtils.startVPN(this)
        VpnServiceHelper.needCapture = true
    }

    /*private fun performOpenVpn() {
        val hasInstallCertificate: Boolean = sharedPreferences.getBoolean(AppConstants.HAS_INSTALL_ROOT_CERTIFICATE, false)
        if (!hasInstallCertificate && !hasAskInstallCert) {
            hasAskInstallCert = true
            showInstallCertDialog()
        } else {
            startVPN()
        }
    }*/

   /* private fun showInstallCertDialog() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.install_root_to_http))
                .setPositiveButton(getString(R.string.install)) { dialog, which ->
                    dialog.dismiss()
                    installCert()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                    performOpenVpn()
                }
                .show()
    }*/


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_capture, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_filter) {
            startActivity(Intent(this, CaptureFilterActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun isMainActivity(): Boolean {
        return true
    }

    override fun getLayout(): Int {
        return R.layout.activity_main_capture
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
