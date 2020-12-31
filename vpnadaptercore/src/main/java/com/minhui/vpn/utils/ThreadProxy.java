package com.minhui.vpn.utils;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.minhui.vpn.log.VPNLog;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/4/30.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */
@Keep
public class ThreadProxy {

    private static final String TAG = "ThreadProxy";
    private final Executor executor;
    private final ExecutorService singleThreadExecutor;

    static class InnerClass {
        static ThreadProxy instance = new ThreadProxy();
    }

    private ThreadProxy() {

        executor = new ThreadPoolExecutor(2, 8,
                15, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE), new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("ThreadProxy multi");
                return thread;
            }

        });
        singleThreadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("ThreadProxy single");
                return thread;
            }

        });
    }

    public void execute(final Runnable run) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                   run.run();
                }catch (Exception e){
                    if(VPNLog.debug){
                        e.printStackTrace();
                    }
                    VPNLog.e(TAG,"failed to run task "+e.getMessage());
                }
            }
        });

    }

    public void executeInSingle(final Runnable run) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    run.run();
                }catch (Exception e){
                    VPNLog.d(TAG,"failed to run task "+e.getMessage());
                }
            }
        });
    }

    public static ThreadProxy getInstance() {
        return InnerClass.instance;
    }
}
