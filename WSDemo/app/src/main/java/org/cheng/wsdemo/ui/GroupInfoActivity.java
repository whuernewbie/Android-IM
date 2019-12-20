package org.cheng.wsdemo.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.AddFriendsBean;
import org.cheng.wsdemo.bean.CreateGroup;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.litepal.crud.DataSupport;

import java.util.List;

public class GroupInfoActivity extends AppCompatActivity {

    private TextView tvGid;

    private TextView tvGname;

    private TextView tvCreateTime;

    private TextView tvSize;

    private TextView tvOwner;

    private TextView tvMember;

    private EditText inviteId;

    private Button invite;

    private Button delete;

    private Button chat;

    public static  final String groupId="groupId";

    private String gId;

    private ImageView back;

    private GroupInfo groupInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        final Intent intent =getIntent();
        gId=intent.getStringExtra(groupId);

        inviteId=(EditText)findViewById(R.id.inviteId);
        tvOwner=(TextView)findViewById(R.id.owner);
        tvCreateTime=(TextView)findViewById(R.id.CreateTime);
        tvMember=(TextView)findViewById(R.id.member);
        tvSize=(TextView)findViewById(R.id.size);
        tvGid=(TextView)findViewById(R.id.uid);
        tvGname=(TextView)findViewById(R.id.uname);



        back=(ImageView)findViewById(R.id.iv_back);
        chat=(Button)findViewById(R.id.Enter);
        invite=(Button)findViewById(R.id.invite);
        delete=(Button)findViewById(R.id.delete);

        //TODO 数据库操作应该另开线程
        List<GroupInfo> groupInfoList= DataSupport.where("gid = ?",gId).find(GroupInfo.class);
        for (GroupInfo ginfo: groupInfoList
             ) {
            groupInfo=ginfo;
        }
        tvOwner.setText(groupInfo.getOwner());
        tvCreateTime.setText(groupInfo.getCreateTime());
        tvSize.setText(groupInfo.getNumber());
        tvGid.setText(gId);
        tvGname.setText(groupInfo.getGname());
        tvMember.setText(groupInfo.getPerson());

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //邀请好友
                if(WebSocketService.webSocketConnection.isConnected())
                {
                    AddFriendsBean addFriendsBean=new AddFriendsBean();
                    addFriendsBean.setMsgFrom(gId);
                    addFriendsBean.setMsgTo(inviteId.getText().toString());
                    addFriendsBean.setActionType("request");
                    addFriendsBean.setMsgType(MESSAGETYPE.GroupCreate.toString());

                    WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(addFriendsBean));
                    NoticeUtil.ShowImportMsg("成功",GroupInfoActivity.this);
                }
                else
                {
                    NoticeUtil.ShowImportMsg(NoticeUtil.NO_CONNECT,GroupInfoActivity.this);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(GroupInfoActivity.this,MessageActivity.class);
                intent.putExtra(MessageActivity.msgType,MESSAGETYPE.GROUPCHAT.toString());
                intent.putExtra(MessageActivity.msgTo,tvGid.getText());
                intent.putExtra(MessageActivity.receiverImage,groupInfo.getHeadImageUrl());
                intent.putExtra(MessageActivity.name,groupInfo.getGname());
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 删除操作
            }
        });












    }


}
