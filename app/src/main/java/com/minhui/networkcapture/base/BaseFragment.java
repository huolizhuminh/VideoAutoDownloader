package com.minhui.networkcapture.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/5.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public abstract class BaseFragment extends Fragment {

    private View contentView;
    private Unbinder bind;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        contentView = getContentView();
        if (contentView == null) {
            contentView = inflater.inflate(getLayout(), container, false);
        }
        bind = ButterKnife.bind(this,contentView);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }

    protected int getLayout() {
        return 0;
    }

    protected View getContentView() {
        return null;
    }

    public void onVisible() {

    }

    public void onInVisible() {

    }
}
