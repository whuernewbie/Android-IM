package org.cheng.wsdemo.ui;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.AddFriendsAdapter;
import org.cheng.wsdemo.bean.FriendListBean;
import org.cheng.wsdemo.bean.AddFriendsBean;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.GroupListBean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.http.HttpUtil;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class FriendRequestActivity extends AppCompatActivity {
    private List<AddFriendsBean> addFriendsBeanList=new ArrayList<>();

    private RecyclerView rvRecyclerView;

    private AddFriendsAdapter adapter;

    private ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        initView();
        initData();

        back=(ImageView) findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                if(addFriendsBeanList.size()!=0)
                {
                    addFriendsBeanList.remove(viewHolder.getAdapterPosition());
                }
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
        List<AddFriendsBean> addFriendsBeans= DataSupport.findAll(AddFriendsBean.class);
        addFriendsBeanList.addAll(addFriendsBeans);

    }

    /**
     * item＋item里的控件点击监听事件
     */
    private AddFriendsAdapter.OnItemClickListener MyItemClickListener = new AddFriendsAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View v, AddFriendsAdapter.ViewName viewName, int position) {
            //viewName可以区分是item还是item内部控件
            AddFriendsBean addFriendsBean=addFriendsBeanList.get(position);
            switch (v.getId()){
                case R.id.btn_agree:
                    //TODO 处理好友请求以及加群请求
                    if(WebSocketService.webSocketConnection.isConnected())
                    {
                        addFriendsBeanList.remove(addFriendsBean);
                        addFriendsBean.delete();
                        addFriendsBean.setActionType("agree");
                        WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(addFriendsBean));
                        adapter.notifyDataSetChanged();

                        //如果同意的是好友请求
                        if(addFriendsBean.getMsgType().equals(MESSAGETYPE.FriendReq.toString()))
                        {
                            //先存到用户信息表 TODO 应当get好友信息
                            UserInfo userInfo =new UserInfo();
                            userInfo.setUname(addFriendsBean.getMsgFrom());
                            userInfo.setUid(addFriendsBean.getMsgFrom());
                            userInfo.save();

                            //再在用户关系表中绑定用户关系
                            FriendListBean friendListBean =new FriendListBean();
                            friendListBean.setUid1(addFriendsBean.getMsgFrom());
                            friendListBean.setUid2(addFriendsBean.getMsgTo());
                            friendListBean.save();
                        }
                        //如果同意的是群邀请 则添加群关系列表，获取群信息，添加群信息
                        else if(addFriendsBean.getMsgType().equals(MESSAGETYPE.GroupInvite.toString()))
                        {
                            HttpUtil.postFindGroupInfo(FakeDataUtil.FindGroupInfo,addFriendsBean.getMsgFrom(),new okhttp3.Callback(){
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String responseData=response.body().string();
                                    System.out.println(responseData);
                                    try
                                    {
                                        JSONObject jsonObject =new JSONObject(responseData);
                                        if(jsonObject.get("status").toString().equals("ok"))
                                        {
                                            JSONObject jsonObject1 =new JSONObject(jsonObject.get("groupInfo").toString());
                                            GroupInfo groupInfo=JSON.parseObject(jsonObject1.get("groupInfo").toString(),new TypeReference<GroupInfo>(){});
                                            groupInfo.save();
                                            System.out.println(jsonObject.toString());
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

                            //绑定群关系
                            GroupListBean groupListBean=new GroupListBean();
                            groupListBean.setGid(addFriendsBean.getMsgFrom());
                            groupListBean.setUid(FakeDataUtil.SenderUid);
                            groupListBean.save();
                        }

                        Toast.makeText(FriendRequestActivity.this,"你点击了同意按钮"+(position+1),Toast.LENGTH_SHORT).show();
                    }else
                    {
                        //TODO 网络问题
                    }
                    break;
                case R.id.btn_refuse:
                    addFriendsBeanList.remove(addFriendsBean);
                    addFriendsBean.delete();
                    adapter.notifyDataSetChanged();
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
