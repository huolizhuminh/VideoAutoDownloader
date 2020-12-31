package com.minhui.vpn.upload;


import androidx.annotation.Keep;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/8.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
@Keep
public interface UploadListener {
    void onSuccess();

    void onFailed();
}
