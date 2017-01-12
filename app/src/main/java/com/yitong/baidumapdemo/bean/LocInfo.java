package com.yitong.baidumapdemo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * date：2017/1/12
 * des：
 * Create by suqi
 * Copyright (c) 2016 Shanghai P&C Information Technology Co., Ltd.
 */

public class LocInfo implements Serializable {

    private double latitude;// 纬度
    private double longitude;// 纬度
    private String address;// 位置

    public static List<LocInfo> locInfos;

    static {
        locInfos = new ArrayList<>();
        locInfos.add(new LocInfo(34.242652, 108.971171, "哎呦，不错哦"));
        locInfos.add(new LocInfo(34.242852, 108.973171, "你是谁？？？"));
        locInfos.add(new LocInfo(34.242152, 108.971971, "Are you really!"));
    }

    public LocInfo(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
