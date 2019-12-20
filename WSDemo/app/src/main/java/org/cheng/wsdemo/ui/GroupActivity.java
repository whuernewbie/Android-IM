package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.GroupsAdapter;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.GroupListBean;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;

    private List<GroupInfo> GroupList = new ArrayList<>();

    private GroupsAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

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
        navView.setCheckedItem(R.id.nav_group);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_msg:
                        mDrawerLayout.closeDrawers();
                        Intent intent=new Intent(GroupActivity.this,MessagesActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_friends:
                        mDrawerLayout.closeDrawers();
                        Intent intent1=new Intent(GroupActivity.this,FriendsActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_group:
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_dynamic:
                        mDrawerLayout.closeDrawers();
                        Intent intent2=new Intent(GroupActivity.this,DynamicShowActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_Info:
                        mDrawerLayout.closeDrawers();
                        Intent intent3=new Intent(GroupActivity.this,InfoChangeActivity.class);
                        startActivity(intent3);
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
        initGroups();
        RecyclerView      recyclerView  = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new GroupsAdapter(GroupList);
        recyclerView.setAdapter(adapter);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshGroups();
            }
        });
    }

    private void refreshGroups() {
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
                        initGroups();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //初始化群组列表
    private void initGroups() {
        GroupList.clear();
        List<GroupListBean> groupListBeans= DataSupport.where("uid = ?", FakeDataUtil.SenderUid).find(GroupListBean.class);
        List<GroupInfo> groupInfos=DataSupport.findAll(GroupInfo.class);
        for (GroupListBean group:groupListBeans
             ) {

            for (GroupInfo groupInfo:groupInfos
                 ) {
                if(group.getGid().equals(groupInfo.getGid()))
                {
                    GroupList.add(groupInfo);
                }
            }
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

}