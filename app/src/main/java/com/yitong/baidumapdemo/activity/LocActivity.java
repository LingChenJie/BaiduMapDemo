package com.yitong.baidumapdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.yitong.baidumapdemo.R;
import com.yitong.baidumapdemo.bean.LocInfo;
import com.yitong.baidumapdemo.listener.MySensorEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 主要演示了百度地图基础的相关操作
 */
public class LocActivity extends AppCompatActivity {

    private Context mContext;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private LocationClient mLocClient;// 开启定位的核心类
    private MyLocListener mMyLocListener;// 当前位置的监听

    private boolean isFirstIn = true;// 是否是第一次进入地图

    private double mLatitude;// 经度
    private double mLongitude;// 纬度

    private BitmapDescriptor mIconLoc;// 自定义定位图标
    private BitmapDescriptor mIconOverlay;// 自定义覆盖物图标

    private MySensorEventListener mMySensorEventListener;// 传感器
    private float mCurX;// 当前x方向的位置

    // 当前的方向模式
    private MyLocationConfiguration.LocationMode mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_loc);
        mContext = this;
        initView();
        initAction();
        initData();
    }

    private void initView() {
        mMapView = (MapView) findViewById(R.id.bd_map_view);
        mBaiduMap = mMapView.getMap();

        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }

    private void initAction() {
        // 传感器
        mMySensorEventListener = new MySensorEventListener(mContext);
        mMySensorEventListener.setOrientationListener(new MySensorEventListener.OrientationListener() {
            @Override
            public void onOrientationChange(float x) {
                mCurX = x;
            }
        });

        // 覆盖物点击
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 获取保存的信息
                LocInfo locInfo = (LocInfo) marker.getExtraInfo().getSerializable("locInfo");

                // 点击时显示popup
                InfoWindow infoWindow;
                TextView textView = new TextView(mContext);
                textView.setBackgroundResource(R.drawable.icon_popupmap);
                textView.setPadding(30, 20, 30, 50);
                textView.setText(locInfo.getAddress());
                textView.setTextColor(Color.GRAY);
                textView.setGravity(Gravity.CENTER);

                final LatLng latLng = marker.getPosition();
                Point point = mBaiduMap.getProjection().toScreenLocation(latLng);
                point.y -= 50;// 改变popup的位置，防止被popup盖住
                LatLng ll = mBaiduMap.getProjection().fromScreenLocation(point);

                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(textView);
                infoWindow = new InfoWindow(bitmapDescriptor, ll, 0, new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {
                        mBaiduMap.hideInfoWindow();
                    }
                });
                mBaiduMap.showInfoWindow(infoWindow);

                return true;
            }
        });

        // 地图的点击事件
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();// 隐藏popup
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    private void initData() {
        initLocation();
    }

    // 给覆盖物之间添加连线,并显示1-2-3顺序
    public void addOverlaysLine(List<LocInfo> locInfos) {
        mIconOverlay = BitmapDescriptorFactory.fromResource(R.mipmap.icon_loc);
        mBaiduMap.clear();

        LatLng latLng = null;
        Marker marker = null;
        OverlayOptions overlayOptions = null;
        List<LatLng> points = new ArrayList<LatLng>();

        int curId = 1;

        for(LocInfo locInfo : locInfos) {
            latLng = new LatLng(locInfo.getLatitude(), locInfo.getLongitude());
            overlayOptions = new MarkerOptions().position(latLng).icon(mIconOverlay).zIndex(5);
            marker = (Marker) mBaiduMap.addOverlay(overlayOptions);

            Bundle bundle = new Bundle();
            bundle.putSerializable("locInfo", locInfo);
            marker.setExtraInfo(bundle);

            // 添加文字
            LatLng llText = new LatLng(39.86923, 116.397428);
            OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
                    .fontSize(48).fontColor(0xFFFF00FF).text("No." + curId++).rotate(0)
                    .position(latLng);
            mBaiduMap.addOverlay(ooText);

            points.add(new LatLng(locInfo.getLatitude(), locInfo.getLongitude()));
        }


        // 添加之间的连线
        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.BLUE).points(points);
        mBaiduMap.addOverlay(ooPolyline);

        // 更新位置到添加覆盖物那里
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }

    // 添加覆盖物
    private void addOverlays(List<LocInfo> locInfos) {
        mIconOverlay = BitmapDescriptorFactory.fromResource(R.mipmap.icon_loc);
        mBaiduMap.clear();

        LatLng latLng = null;
        Marker marker = null;
        OverlayOptions overlayOptions = null;

        for(LocInfo locInfo : locInfos) {
            latLng = new LatLng(locInfo.getLatitude(), locInfo.getLongitude());
            overlayOptions = new MarkerOptions().position(latLng).icon(mIconOverlay).zIndex(5);
            marker = (Marker) mBaiduMap.addOverlay(overlayOptions);

            Bundle bundle = new Bundle();
            bundle.putSerializable("locInfo", locInfo);
            marker.setExtraInfo(bundle);
        }

        // 更新位置到添加覆盖物那里
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }

    // 初始化定位
    private void initLocation() {
        mLocClient = new LocationClient(this);
        mMyLocListener = new MyLocListener();
        mLocClient.registerLocationListener(mMyLocListener);

        LocationClientOption locClientOption = new LocationClientOption();
        locClientOption.setCoorType("bd09ll");
        locClientOption.setIsNeedAddress(true);
        locClientOption.setOpenGps(true);
        locClientOption.setScanSpan(1000);
        mLocClient.setLocOption(locClientOption);

        mIconLoc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_arrow);// 初始化图标
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_map_common:// 普通情况
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;

            case R.id.menu_map_site:// 卫星定位
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.menu_map_traffic:// 热力图
                if (mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通(off)");
                } else {
                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle("实时交通(on)");
                }
                break;

            case R.id.menu_map_my_address:// 定位到自己当前位置
                goMyLoc(mLatitude, mLongitude);
                break;

            case R.id.menu_location_mode_normal:// 正常模式
                mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
                break;

            case R.id.menu_location_mode_following:// 俯视模式
                mLocationMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                break;

            case R.id.menu_location_mode_compass:// 罗盘模式
                mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
                break;

            case R.id.menu_add_overlays:// 添加覆盖物
                addOverlays(LocInfo.locInfos);
                break;

            case R.id.menu_add_over_line:// 给覆盖物之间添加连线
                addOverlaysLine(LocInfo.locInfos);
                break;

            case R.id.menu_line_act:// 跳转到绘制line的Activity
                mContext.startActivity(new Intent(mContext, DrawLineActivity.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    // 定位到自己当前的位置
    private void goMyLoc(double mLatitude, double mLongitude) {
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(mapStatusUpdate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView.onResume()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView.onPause()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocClient.isStarted()) {// 开启定位
            mLocClient.start();
        }

        if (mMySensorEventListener != null) {// 开启传感器
            mMySensorEventListener.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);
        if (mLocClient.isStarted()) {// 关闭定位
            mLocClient.stop();
        }

        if (mMySensorEventListener != null) {// 关闭传感器
            mMySensorEventListener.stop();
        }
    }

    public class MyLocListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation loc) {

            MyLocationData data = new MyLocationData.Builder()//
                    .direction(mCurX)// 设置方向
                    .accuracy(loc.getRadius())//
                    .latitude(loc.getLatitude())//
                    .longitude(loc.getLongitude())//
                    .build();
            mBaiduMap.setMyLocationData(data);

            // 配置自定义定位图标和地图模式
            MyLocationConfiguration config = new MyLocationConfiguration(mLocationMode, true, mIconLoc);
            mBaiduMap.setMyLocationConfigeration(config);

            mLatitude = loc.getLatitude();
            mLongitude = loc.getLongitude();

            if (isFirstIn) {// 第一次进入地图，设置经度和纬度
                goMyLoc(loc.getLatitude(), loc.getLongitude());

                isFirstIn = false;
                Toast.makeText(mContext, "当前位置：" + loc.getAddrStr(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
