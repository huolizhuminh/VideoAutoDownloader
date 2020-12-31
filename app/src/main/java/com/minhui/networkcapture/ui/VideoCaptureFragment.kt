package com.minhui.networkcapture.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import com.minhui.networkcapture.R
import com.minhui.networkcapture.base.BaseFragment
import com.minhui.vpn.ProxyConfig
import com.minhui.vpn.parser.VideoCapturedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VideoCaptureFragment : BaseFragment() {
    private lateinit var mVideoCaptureView: VideoCaptureView
    private val TAG = "VideoCaptureFragment"


    var listener: ProxyConfig.VpnStatusListener = object : ProxyConfig.VpnStatusListener {
        override fun onVpnStart(context: Context) {
            refreshView()
        }

        override fun onVpnEnd(context: Context) {

        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_videocapture
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mVideoCaptureView = view.findViewById(R.id.videoCaptureView)
        ProxyConfig.Instance.registerVpnStatusListener(listener)
        EventBus.getDefault().register(this)
        refreshView()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ProxyConfig.Instance.unregisterVpnStatusListener(listener)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoCaptured(event: VideoCapturedEvent?) {
        mVideoCaptureView.post {
            refreshView()
        }
    }

    private fun refreshView() {
        mVideoCaptureView.refreshDataAndRefreshView(ProxyConfig.Instance.currentCaptureVideo)
    }
}


