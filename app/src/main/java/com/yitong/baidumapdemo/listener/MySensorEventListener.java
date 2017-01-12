package com.yitong.baidumapdemo.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * date：2017/1/11
 * des：方向监听
 * Create by suqi
 * Copyright (c) 2016 Shanghai P&C Information Technology Co., Ltd.
 */

public class MySensorEventListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Context mContext;

    private float lastX;// 只关心x轴
    private OrientationListener mOrientationListener;

    public void start() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);// 获得方向传感器
        }

        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    public MySensorEventListener(Context context) {
        mContext = context;
    }

    // 方向发生变化
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[SensorManager.DATA_X];

            if (Math.abs(x - lastX) > 1.0) {
                if (mOrientationListener != null) {
                    mOrientationListener.onOrientationChange(x);
                }
            }
            lastX = x;
        }
    }

    // 精度发生改变
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // x方向监听接口
    public interface OrientationListener {
        void onOrientationChange(float x);
    }

    public void setOrientationListener(OrientationListener orientationListener) {
        mOrientationListener = orientationListener;
    }
}
