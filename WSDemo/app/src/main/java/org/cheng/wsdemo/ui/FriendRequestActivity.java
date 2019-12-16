package org.cheng.wsdemo.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.AddFriendsAdapter;
import org.cheng.wsdemo.bean.AddFriendsBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity {
    private List<AddFriendsBean> addFriendsBeanList=new ArrayList<>();

    private RecyclerView rvRecyclerView;

    private AddFriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        initView();
        initData();

        rvRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));//控制布局为LinearLayout或者是GridView或者是瀑布流布局
        //rv.setLayoutManager(new GridLayoutManager(this,2));//类似桌面拖拽排序的效果
        //rv.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        // list.clear();//清空list
        adapter = new AddFriendsAdapter(addFriendsBeanList,this);
        rvRecyclerView.setAdapter(adapter);
        // 设置item及item中控件的点击事件
        adapter.setOnItemClickListener(MyItemClickListener);

        //为RecycleView绑定触摸事件(作用：滑动删除)
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //首先回调的方法 返回int表示是否监听该方向

                //第一种：
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//拖拽
                int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;//侧滑删除
                //第二种：
                //int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;//拖拽
                //int swipeFlags = 0;//侧滑删除
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //滑动事件
                Collections.swap(addFriendsBeanList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //侧滑事件
                addFriendsBeanList.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //是否可拖拽
                return true;
            }
        });
        helper.attachToRecyclerView(rvRecyclerView);

    }

    private void initView() {
        rvRecyclerView = (RecyclerView) findViewById(R.id.rv_recyclerView);
    }
    private void initData() {
        addFriendsBeanList = new ArrayList<>();

        AddFriendsBean addFriendsBean=new AddFriendsBean();
        addFriendsBean.setFrom("1000001");
        addFriendsBeanList.add(addFriendsBean);

    }

    /**
     * item＋item里的控件点击监听事件
     */
    private AddFriendsAdapter.OnItemClickListener MyItemClickListener = new AddFriendsAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View v, AddFriendsAdapter.ViewName viewName, int position) {
            //viewName可以区分是item还是item内部控件
            switch (v.getId()){
                case R.id.btn_agree:
                    //TODO 处理好友请求
                    Toast.makeText(FriendRequestActivity.this,"你点击了同意按钮"+(position+1),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_refuse:
                    Toast.makeText(FriendRequestActivity.this,"你点击了拒绝按钮"+(position+1),Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(FriendRequestActivity.this,"你点击了item按钮"+(position+1),Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onItemLongClick(View v) {

        }
    };
}
