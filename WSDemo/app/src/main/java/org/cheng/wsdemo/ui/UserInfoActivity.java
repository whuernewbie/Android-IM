package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.Glide;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.AddFriendsBean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {

    private String name;

    private String Id;

    private String ImageUrl;

    public static final String NAME = "name";

    public static final String ID = "1000002";

    public static final String IMAGE_ID = "image_id";

    private FloatingActionButton AddFriends;

    private FloatingActionButton SendMsg;

    private TextView tvUid;

    private TextView tvUname;

    private TextView tvEmail;

    private TextView tvSex;

    private TextView tvAge;

    private TextView tvMoreInfo;

    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        Intent intent = getIntent();
        name = intent.getStringExtra(NAME);
        ImageUrl = intent.getStringExtra(IMAGE_ID);
        Id = intent.getStringExtra(ID);


        imageView=(ImageView)findViewById(R.id.image_view);
        tvAge=(TextView)findViewById(R.id.age);
        tvEmail=(TextView)findViewById(R.id.email);
        tvMoreInfo=(TextView)findViewById(R.id.moreInfo);
        tvSex=(TextView)findViewById(R.id.sex);
        tvUid=(TextView)findViewById(R.id.uid);
        tvUname=(TextView)findViewById(R.id.uname);

        AddFriends=(FloatingActionButton)findViewById(R.id.AddFrinds);
        SendMsg=(FloatingActionButton)findViewById(R.id.SendMsg);

        AddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 限制加好友

                if(WebSocketService.webSocketConnection.isConnected())
                {
                    AddFriendsBean addFriendsBean = new AddFriendsBean();
                    addFriendsBean.setMsgTo(Id);
                    addFriendsBean.setMsgFrom(FakeDataUtil.SenderUid);
                    addFriendsBean.setMsgType(MESSAGETYPE.FriendReq.toString());
                    addFriendsBean.setActionType("request");
                    WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(addFriendsBean));

                    NoticeUtil.ShowImportMsg(JSON.toJSONString(addFriendsBean),UserInfoActivity.this);
                }else
                {
                    NoticeUtil.ShowImportMsg(NoticeUtil.NO_CONNECT,UserInfoActivity.this);
                }


            }
        });

        SendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 限制是否可以聊天
                Intent intent = new Intent(UserInfoActivity.this, MessageActivity.class);
                intent.putExtra(MessageActivity.receiverImage, ImageUrl);
                intent.putExtra(MessageActivity.name, name);
                intent.putExtra(MessageActivity.msgTo, ID);
                intent.putExtra(MessageActivity.msgType,MESSAGETYPE.USERCHAT);
                startActivity(intent);
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        collapsingToolbar.setTitle(name);
        Glide.with(this).load(ImageUrl).into(imageView);

        tvUname.setText(name);
        tvUid.setText(Id);

        HttpUtil.postFindUserInfo(FakeDataUtil.FindUserInfo,Id,new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData=response.body().string();
                System.out.println(responseData);
                try
                {
                    JSONObject jsonObject =new JSONObject(responseData);
                    if(jsonObject.get("status").toString().equals("ok"))
                    {
                        final UserInfo userInfo =JSON.parseObject(jsonObject.get("userInfo").toString(),new TypeReference<UserInfo>(){});
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvAge.setText(" "+userInfo.getAge());
                                tvEmail.setText(" "+userInfo.getEmail());
                                tvMoreInfo.setText(" "+userInfo.getMoreInfo());
                                tvSex.setText(" "+userInfo.getSex());
                                tvUid.setText(" "+userInfo.getUid());
                                tvUname.setText(" "+userInfo.getUname());
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

            }

        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
