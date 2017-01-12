package com.yitong.baidumapdemo.application;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;

/**
 * date：2017/1/11
 * des：
 * Create by suqi
 * Copyright (c) 2016 Shanghai P&C Information Technology Co., Ltd.
 */

public class LocApplication extends Application {

    private static LocApplication mApp;

    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

        init();
    }

    private void init() {
        initLoc();
    }

    /**
     * 初始化地图配置
     */
    private void initLoc() {
        // BDLocMgr.getInstance().start();
        mVibrator =(Vibrator)mApp.getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(mApp);
    }

    public static Context getContext() {
        return mApp;
    }
}
