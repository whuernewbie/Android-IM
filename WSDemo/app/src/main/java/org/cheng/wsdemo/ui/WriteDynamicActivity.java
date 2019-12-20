package org.cheng.wsdemo.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.DynamicBean;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class WriteDynamicActivity extends AppCompatActivity {

    private ImageView back;

    private ImageView image;

    private Button send;

    private EditText context;

    private TextView location;

    public LocationClient mLocationClient;

    private DynamicBean dynamicBean=new DynamicBean();

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
        mLocationClient.stop();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_dynamic);

        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(WriteDynamicActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(WriteDynamicActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(WriteDynamicActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WriteDynamicActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        location=(TextView)findViewById(R.id.location);

        back=(ImageView) findViewById(R.id.iv_back);

        send=(Button)findViewById(R.id.btn_save_dynamic);

        context=(EditText)findViewById(R.id.write_context);

        image=(ImageView)findViewById(R.id.image);



        Glide.with(WriteDynamicActivity.this).load(FakeDataUtil.SenderImageUrl).into(image);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dynamicBean.setContent(context.getText().toString());
                dynamicBean.setUid(FakeDataUtil.SenderUid);
                dynamicBean.setHeadImageUrl(FakeDataUtil.SenderImageUrl);
                dynamicBean.setUname(FakeDataUtil.SenderUid+FakeDataUtil.uname);

                HttpUtil.postUploadDynamic(FakeDataUtil.UploadDynamic,dynamicBean,new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        try{
                            JSONObject jsonObject =new JSONObject(responseData);
                            System.out.println(jsonObject.toString());
                            if(jsonObject.get("status").toString().equals("ok"))
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NoticeUtil.ShowImportMsg("成功",WriteDynamicActivity.this);
                                    }
                                });

                            }
                        }catch (JSONException e)
                        {
                            //TODO 子线程JSON转换错误
                        }
                    }
                    @Override
                    public void onFailure(Call call,IOException e){
                        //TODO 子线程http错误
                    }
                });
                //TODO 发送动态
                dynamicBean.save();
                context.setText("");

            }
        });

    }

}
