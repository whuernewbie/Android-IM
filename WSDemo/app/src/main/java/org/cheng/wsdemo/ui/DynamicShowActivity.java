package org.cheng.wsdemo.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.DynamicAdapter;
import org.cheng.wsdemo.adapter.FriendsAdapter;
import org.cheng.wsdemo.bean.DynamicBean;
import org.cheng.wsdemo.bean.FriendListBean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.WebSocketUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DynamicShowActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;

    private List<DynamicBean> DynamicList = new ArrayList<>();

    private DynamicAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    public LocationClient mLocationClient;

    private DynamicBean dynamicBean=new DynamicBean();

    private int start=0;

    private int end=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(DynamicShowActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(DynamicShowActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(DynamicShowActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(DynamicShowActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //打开侧边菜单，默认点击friends
        navView.setCheckedItem(R.id.nav_dynamic);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_msg:
                        mDrawerLayout.closeDrawers();
                        Intent intent=new Intent(DynamicShowActivity.this,MessagesActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_friends:
                        mDrawerLayout.closeDrawers();
                        Intent intent2=new Intent(DynamicShowActivity.this,FriendsActivity.class);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.nav_group:
                        mDrawerLayout.closeDrawers();
                        Intent intent1=new Intent(DynamicShowActivity.this,GroupActivity.class);
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.nav_dynamic:
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_Info:
                        Intent intent3=new Intent(DynamicShowActivity.this,InfoChangeActivity.class);
                        startActivity(intent3);
                        finish();
                        break;
                    case R.id.nav_view:
                        break;
                }
                return true;
            }
        });

        //圆形控件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DynamicShowActivity.this,WriteDynamicActivity.class);
                startActivity(intent);
            }
        });


        //初始化消息列表界面，下拉刷新
        initDynamic();
        RecyclerView      recyclerView  = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DynamicAdapter(DynamicList);
        recyclerView.setAdapter(adapter);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDynamic();
            }
        });
    }

    private void refreshDynamic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO 刷新动态页面
                        DynamicList.clear();
                        HttpUtil.DownloadDynamic(FakeDataUtil.DownloadDynamic,FakeDataUtil.SenderUid,dynamicBean.getLocationx(),dynamicBean.getLocationy(),start,end,new Callback(){
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responseData=response.body().string();
                                try
                                {
                                    JSONObject jsonObject =new JSONObject(responseData);
                                    System.out.println("成功成功成功"+jsonObject.get("status").toString());
                                    if(jsonObject.get("status").toString().equals("ok"))
                                    {
                                        boolean findit=false;
                                        List<DynamicBean> dynamicBeans=DataSupport.findAll(DynamicBean.class);
                                        System.out.println(jsonObject);
                                        final JSONArray jsonArray =new JSONArray(jsonObject.get("msg").toString());
                                        for(int i=0;i<jsonArray.length();i++)
                                        {
                                            DynamicBean dynamicBean= JSON.parseObject(jsonArray.get(i).toString(),new TypeReference<DynamicBean>(){});
                                            for (DynamicBean dynamic:dynamicBeans
                                                 ) {
                                                if(dynamic.getUid().equals(dynamicBean.getUid())&&dynamic.getTime()!=null&&dynamic.getTime().equals(dynamicBean.getTime()))
                                                {
                                                    findit=true;
                                                }
                                            }
                                            if(!findit)
                                            {
                                                dynamicBean.save();
                                                findit=false;
                                            }

                                        }
                                        start+=4;
                                        end+=4;
                                    }
                                    else
                                    {
                                        //TODO 已经到底
                                    }
                                }catch (JSONException e)
                                {
                                    //TODO 子线程JSON格式转换错误
                                }
                            }
                            @Override
                            public void onFailure(Call call,IOException e){
                                //TODO 子线程http传输错误
                            }

                        });
                        initDynamic();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //初始化动态列表
    private void initDynamic() {

        List<FriendListBean> friendListBeanList = DataSupport.where("uid1=? or uid2= ?", FakeDataUtil.SenderUid, FakeDataUtil.SenderUid).find(FriendListBean.class);
        for (FriendListBean friendListBean : friendListBeanList
        ) {
            if (friendListBean.getUid1().equals(FakeDataUtil.SenderUid)) {
                List<DynamicBean> dynamicBeans = DataSupport.where("uid = ?", friendListBean.getUid2()).find(DynamicBean.class);
                DynamicList.addAll(dynamicBeans);
            } else {
                List<DynamicBean> dynamicBeans = DataSupport.where("uid = ?", friendListBean.getUid1()).find(DynamicBean.class);
                DynamicList.addAll(dynamicBeans);
            }

            //TODO 去除重复元素补丁（待修改）
            List<DynamicBean> dynamicBeans = DataSupport.where("uid = ?", FakeDataUtil.SenderUid).find(DynamicBean.class);
            DynamicList.addAll(dynamicBeans);

            LinkedHashSet<DynamicBean> hashSet=new LinkedHashSet<>((DynamicList));
            ArrayList<DynamicBean> userInfos=new ArrayList<>(hashSet);
            DynamicList.clear();
            DynamicList.addAll(userInfos);

            Collections.sort(DynamicList);

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.backup:
                Intent intent2=new Intent(this,FriendRequestActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_create_group:
                Intent intent=new Intent(this,CreatGroupActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_find:
                Intent intent1=new Intent(this,FindFriendsActivity.class);
                startActivity(intent1);
                break;
            default:
        }
        return true;
    }
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("国家：").append(location.getCountry());
            currentPosition.append("省：").append(location.getProvince());
            currentPosition.append("市：").append(location.getCity());
            currentPosition.append("区：").append(location.getDistrict());
            currentPosition.append("街道：").append(location.getStreet());

            dynamicBean.setLocationx(location.getLongitude());
            dynamicBean.setLocationy(location.getLatitude());
            dynamicBean.setAddress(currentPosition.toString());
            dynamicBean.setTime(getNowTimeStamp());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mLocationClient.stop();
    }


    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(60000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    public static String getNowTimeStamp() {
        long time = System.currentTimeMillis();
        String nowTimeStamp = String.valueOf(time / 1000);
        return nowTimeStamp;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


}
