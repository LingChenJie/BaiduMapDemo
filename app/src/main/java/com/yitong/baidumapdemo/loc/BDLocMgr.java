package com.yitong.baidumapdemo.loc;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yitong.baidumapdemo.application.LocApplication;

/**
 * date：2017/1/11
 * des：百度地图定位管理
 * Create by suqi
 * Copyright (c) 2016 Shanghai P&C Information Technology Co., Ltd.
 */

public class BDLocMgr implements BDLocationListener {

    private static BDLocMgr instance;
    private LocationClient mLocClient;

    private LocationInfo mLocationInfo;

    public static BDLocMgr getInstance() {
        if(instance == null) {
            synchronized (BDLocMgr.class) {
                if(instance == null) {
                    instance = new BDLocMgr(LocApplication.getContext());
                }
            }
        }
        return instance;
    }

    private BDLocMgr(Context context) {
        mLocClient = new LocationClient(context);
        initLocOption();

        mLocClient.registerLocationListener(this);
    }

    /**
     * 初始化一些定位信息的配置
     */
    private void initLocOption() {
        LocationClientOption locClientOption = new LocationClientOption();

        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locClientOption.setCoorType("bd09ll");

        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        locClientOption.setScanSpan(1000 * 30);

        //可选，设置是否需要地址信息，默认不需要
        locClientOption.setIsNeedAddress(true);

        //可选，设置是否需要地址描述
        locClientOption.setIsNeedLocationDescribe(true);

        //可选，设置是否需要设备方向结果
        locClientOption.setNeedDeviceDirect(false);

        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locClientOption.setLocationNotify(false);

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locClientOption.setIgnoreKillProcess(true);

        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locClientOption.setIsNeedLocationDescribe(true);

        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locClientOption.setIsNeedLocationPoiList(true);

        //可选，默认false，设置是否收集CRASH信息，默认收集
        locClientOption.SetIgnoreCacheException(false);


        mLocClient.setLocOption(locClientOption);
    }

    /**
     * 开启定位
     */
    public void start() {
        synchronized (BDLocMgr.class) {
            if(mLocClient != null && !mLocClient.isStarted()) {
                mLocClient.start();
            }
        }
    }

    /**
     * 关闭定位
     */
    public void stop() {
        synchronized (BDLocMgr.class) {
            if(mLocClient != null && mLocClient.isStarted()) {
                mLocClient.stop();
            }
        }
    }

    /**
     * 清空当前位置信息
     */
    public void clear() {
        mLocationInfo = null;
    }

    // 接收定位信息
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        if (bdLocation == null) {
            return;
        }

        // 错误代码，如果定位失败会返回这样的经纬度[4.9E-324, 4.9E-324]
        if(62 == bdLocation.getLocType() || 63 == bdLocation.getLocType() || 67 == bdLocation.getLocType()
                || (161 < bdLocation.getLocType() && bdLocation.getLocType() < 168)) {
            return;
        }

        mLocationInfo = new LocationInfo();
        mLocationInfo.latitude = bdLocation.getLatitude();
        mLocationInfo.longitude = bdLocation.getLongitude();
        mLocationInfo.address = bdLocation.getAddrStr();

        Log.i("BDLocMgr", "获取地理位置成功");
        Log.i("BDLocMgr", "经度 ===> " + mLocationInfo.latitude + "; 纬度 ===> " + mLocationInfo.longitude + "; 位置 ===> " + mLocationInfo.address);
    }

    /**
     * 获取当前的位置信息，如果获取为null，则说明未开启定位或获取定位失败
     * @return
     */
    public LocationInfo getCurLocationInfo() {
        return mLocationInfo;
    }

    // 定位实体类
    private class LocationInfo {
        public double latitude;// 纬度
        public double longitude;// 经度
        public String address;// 地址
    }
}
