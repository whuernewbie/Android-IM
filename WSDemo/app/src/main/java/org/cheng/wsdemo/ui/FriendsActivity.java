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
import android.widget.Button;
import android.widget.Toast;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.FriendListBean;
import org.cheng.wsdemo.adapter.FriendsAdapter;
import org.cheng.wsdemo.adapter.MsgsAdapter;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.websocket.MyWebSocket;
import org.cheng.wsdemo.websocket.MyWebSocketHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class FriendsActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;

    private List<UserInfo> FriendList = new ArrayList<>();

    private FriendsAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

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
        navView.setCheckedItem(R.id.nav_friends);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_msg:
                        mDrawerLayout.closeDrawers();
                        Intent intent=new Intent(FriendsActivity.this,MessagesActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_friends:
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_group:
                        mDrawerLayout.closeDrawers();
                        Intent intent1=new Intent(FriendsActivity.this,GroupActivity.class);
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.nav_dynamic:
                        //TODO 转动态
                        break;
                    case R.id.nav_Info:
                        //TODO 转个人信息
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
               //TODO 圆形控件点击事件处理
            }
        });


        //初始化消息列表界面，下拉刷新
        initFriends();
        RecyclerView      recyclerView  = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FriendsAdapter(FriendList);
        recyclerView.setAdapter(adapter);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFriends();
            }
        });
    }

    private void refreshFriends() {
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
                        FriendList.clear();
                        initFriends();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //初始化好友列表，先从好友关系表中找出所有的好友，再根据好友id去用户信息表中查找用户信息，输出到friendList中
    private void initFriends() {
        List<FriendListBean> friendListBeanList = DataSupport.where("uid1=? or uid2= ?",FakeDataUtil.SenderUid,FakeDataUtil.SenderUid).find(FriendListBean.class);
        for (FriendListBean friendListBean: friendListBeanList
             ) {
            if(friendListBean.getUid1().equals(FakeDataUtil.SenderUid))
            {
                List<UserInfo> userInfo=DataSupport.where("uid = ?",friendListBean.getUid2()).find(UserInfo.class);
                FriendList.addAll(userInfo);
            }else{
                List<UserInfo> userInfo=DataSupport.where("uid = ?",friendListBean.getUid1()).find(UserInfo.class);
                FriendList.addAll(userInfo);
            }

        //TODO 去除重复元素补丁（待修改）
            LinkedHashSet<UserInfo> hashSet=new LinkedHashSet<>((FriendList));
            ArrayList<UserInfo> userInfos=new ArrayList<>(hashSet);
            FriendList.clear();
            FriendList.addAll(userInfos);

        }

        //TODO 初始化好友列表

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
