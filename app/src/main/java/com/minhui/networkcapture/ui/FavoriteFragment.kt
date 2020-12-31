package com.minhui.networkcapture.ui

import android.os.Bundle
import android.view.View
import com.minhui.networkcapture.R
import com.minhui.networkcapture.base.BaseFragment
import com.minhui.vpn.greenDao.SessionHelper
import com.minhui.vpn.log.VPNLog
import com.minhui.vpn.utils.ThreadProxy

class FavoriteFragment : BaseFragment()  {
    private lateinit var favoriteCaptureView : VideoCaptureView
    override fun getLayout(): Int {
        return R.layout.fragment_favorite
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoriteCaptureView = view.findViewById(R.id.favoriteCaptureView)
        refreshView();
    }

    private fun refreshView() {
        ThreadProxy.getInstance().execute{
            val daoSession = SessionHelper.getDaoSession(context, SessionHelper.VIDEO_FAVORITE_TABLE)
            val videoList = daoSession.videoItemDao.queryBuilder().list()
            VPNLog.d("refreshView","refreshView size = "+videoList.size)
            val fileList = ArrayList<String>()
            for( item in videoList){
                fileList.add(item.path)
            }
            favoriteCaptureView.refreshDataAndRefreshView(fileList)
        }
    }
}