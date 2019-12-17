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

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.CreateGroupAdapter;
import org.cheng.wsdemo.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class CreatGroupActivity extends AppCompatActivity {

    private List<UserInfo> userInfoList=new ArrayList<>();

    private List<UserInfo> result=new ArrayList<>();

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

        adapter=new CreateGroupAdapter(userInfoList,this);
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
               result= adapter.ChooseSelectData();
               //TODO 处理得到的好友集合
            }
        });



    }

    private void initFriends(){
        //TODO 初始化好友列表
        for(int i=0;i<=20;i++)
        {
            UserInfo userInfo=new UserInfo();
            userInfo.setUid("1000001");
            userInfo.setName("小明");
            userInfoList.add(userInfo);
        }
    }
}
