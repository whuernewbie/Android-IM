package org.cheng.wsdemo.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.CreateGroupAdapter;
import org.cheng.wsdemo.bean.CreateGroup;
import org.cheng.wsdemo.bean.FriendListBean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class CreatGroupActivity extends AppCompatActivity {

    private List<UserInfo> FriendList=new ArrayList<>();

    private String [] result= new String[FakeDataUtil.MaxGroupNum] ;

    private CreateGroupAdapter adapter;

    private RecyclerView recyclerView;

    private CheckBox selectAll;

    private Button create;

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initFriends();
        recyclerView=(RecyclerView)findViewById(R.id.cg_recyclerView);
        selectAll=(CheckBox)findViewById(R.id.select_all);
        create=(Button)findViewById(R.id.btn_create);
        back=(ImageView)findViewById(R.id.iv_back);

        GridLayoutManager layoutManager=new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);

        adapter=new CreateGroupAdapter(FriendList,this);
        recyclerView.setAdapter(adapter);

        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    adapter.selectAll();
                    adapter.notifyDataSetChanged();
                }else
                {
                    adapter.unSelectAll();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //得到群成员
                result=FakeDataUtil.removeArrayEmptyTextBackNewArray(adapter.ChooseSelectData());

                CreateGroup createGroup=new CreateGroup();
                createGroup.setGname(FakeDataUtil.GroupName);
                createGroup.setMsgFrom(FakeDataUtil.SenderUid);
                createGroup.setMsgType(MESSAGETYPE.GroupCreate);
                createGroup.setPerson(result);
                if(WebSocketService.webSocketConnection.isConnected())
                {
                    WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(createGroup));
                }
                else
                {
                    NoticeUtil.ShowImportMsg(NoticeUtil.NO_CONNECT,CreatGroupActivity.this);
                }



               //TODO 处理得到的好友集合
            }
        });



    }

    private void initFriends() {
        //TODO 初始化好友列表
        List<FriendListBean> friendListBeanList = DataSupport.where("uid1=? or uid2= ?", FakeDataUtil.SenderUid, FakeDataUtil.SenderUid).find(FriendListBean.class);
        for (FriendListBean friendListBean : friendListBeanList
        ) {
            if (friendListBean.getUid1().equals(FakeDataUtil.SenderUid)) {
                List<UserInfo> userInfo = DataSupport.where("uid = ?", friendListBean.getUid2()).find(UserInfo.class);
                FriendList.addAll(userInfo);
            } else {
                List<UserInfo> userInfo = DataSupport.where("uid = ?", friendListBean.getUid1()).find(UserInfo.class);
                FriendList.addAll(userInfo);
            }
            LinkedHashSet<UserInfo> hashSet   = new LinkedHashSet<>((FriendList));
            ArrayList<UserInfo>     userInfos = new ArrayList<>(hashSet);
            FriendList.clear();
            FriendList.addAll(userInfos);

//TODO 去除重复元素补丁（待修改）
        }

    }
}
