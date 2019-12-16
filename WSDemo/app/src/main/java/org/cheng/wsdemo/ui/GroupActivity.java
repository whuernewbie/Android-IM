package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import org.cheng.wsdemo.adapter.GroupsAdapter;
import org.cheng.wsdemo.adapter.MsgsAdapter;
import org.cheng.wsdemo.bean.Friends;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.MsgUi;

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

    //初始化好友列表
    private void initGroups() {
        //TODO 初始化群组

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
                Toast.makeText(this, "You clicked Backup", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(this, "You clicked Delete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "You clicked Settings", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

}