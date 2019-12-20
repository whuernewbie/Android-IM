package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.MsgsAdapter;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.MsgUi;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.bean.WebSocketMessageBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.websocket.MyWebSocket;
import org.cheng.wsdemo.websocket.MyWebSocketHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class MessagesActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;

    private List<MsgUi> MsgList = new ArrayList<>();

    private MsgsAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onResume(){
        super.onResume();
        MyWebSocket.myWebSocketHandler=new MessagesActivity.MsgsHandler();
    }

    class MsgsHandler implements MyWebSocketHandler {
        @Override
        public void mySystemMethod(JSONObject jsonObject)
        {
            System.out.println(jsonObject.toString());
            try
            {
                if(jsonObject.has("msgType"))
                {
                    MsgUi msgUi=new MsgUi();
                    if(jsonObject.get("msgType").equals(MESSAGETYPE.USERCHAT))
                    {
                        msgUi.setMsgname(jsonObject.get("msgFrom").toString());
                        msgUi.setMsgId(jsonObject.get("msgFrom").toString());
                        msgUi.setLastMsg(jsonObject.get("message").toString());
                        msgUi.setMsgType(MESSAGETYPE.valueOf(jsonObject.get("msgType").toString()));

                    }else
                    {
                        msgUi.setMsgname(jsonObject.get("msgTo").toString()+jsonObject.get("msgTo").toString());
                        msgUi.setMsgId(jsonObject.get("msgTo").toString());
                        msgUi.setLastMsg(jsonObject.get("message").toString());
                        msgUi.setMsgType(MESSAGETYPE.valueOf(jsonObject.get("msgType").toString()));

                    }


                    //去除重复消息提示
                    boolean findIt=false;
                    for (MsgUi msg:MsgList
                         ) {
                        if(msg.getMsgId().equals(msgUi.getMsgId()))
                        {
                            msg.setLastMsg(jsonObject.get("message").toString());

                            findIt=true;
                            break;
                        }

                    }
                    if(!findIt)
                    {
                        //从数据库中查找用户或者群聊信息，更新用户显示
                        if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.USERCHAT.toString()))
                        {
                            List<UserInfo> userInfoList=DataSupport.where("uid = ?",msgUi.getMsgId()).find(UserInfo.class);
                            for (UserInfo user:userInfoList
                                 ) {
                                if(user.getUid().equals(msgUi.getMsgId()))
                                {
                                    msgUi.setMsgImageUrl(user.getImageUrl());
                                    msgUi.setMsgname(user.getUname());
                                }
                            }
                        }
                        else if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.GROUPCHAT.toString()))
                        {
                            List<GroupInfo> groupInfoList=DataSupport.where("gid = ?",msgUi.getMsgId()).find(GroupInfo.class);
                            for (GroupInfo groupinfo:groupInfoList
                                 ) {
                                msgUi.setMsgImageUrl(groupinfo.getHeadImageUrl());
                                msgUi.setMsgname(groupinfo.getGname());

                            }
                        }
                        MsgList.add(msgUi);
                    }
                    adapter.notifyDataSetChanged();
                }

            }catch (JSONException e)
            {
                //TODO JSON格式转换错误
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        Intent intentService = new Intent(MessagesActivity.this, WebSocketService.class);
        startService(intentService);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //打开侧边菜单，默认点击call
        navView.setCheckedItem(R.id.nav_msg);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_msg:
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_friends:
                        mDrawerLayout.closeDrawers();
                        Intent intent=new Intent(MessagesActivity.this,FriendsActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_group:
                        mDrawerLayout.closeDrawers();
                        Intent intent1=new Intent(MessagesActivity.this,GroupActivity.class);
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.nav_dynamic:
                        mDrawerLayout.closeDrawers();
                        Intent intent2=new Intent(MessagesActivity.this,DynamicShowActivity.class);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.nav_Info:
                        mDrawerLayout.closeDrawers();
                        Intent intent3=new Intent(MessagesActivity.this,InfoChangeActivity.class);
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
                Intent intent=new Intent(MessagesActivity.this,WriteDynamicActivity.class);
                startActivity(intent);

            }
        });


        //初始化消息列表界面，下拉刷新
        initMessages();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MsgsAdapter(MsgList);
        recyclerView.setAdapter(adapter);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFruits();
            }
        });
    }

    private void refreshFruits() {
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
                        initMessages();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //TODO 初始化消息列表,
    private void initMessages() {
    //    List<WebSocketMessageBean> msgList= DataSupport.where("msgTo = ? or msgType= ?", FakeDataUtil.SenderUid,MESSAGETYPE.GROUPCHAT.toString()).find(WebSocketMessageBean.class);
    //    for (WebSocketMessageBean msg:msgList
     //        ) {
      //      if(msg.getMsgType().equals(MESSAGETYPE.GROUPCHAT.toString()))
      //      {

      //      }

     //   }


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

}
