package org.cheng.wsdemo.ui;

import android.content.Intent;
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
import com.bumptech.glide.Glide;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.bean.AddFriendsBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.w3c.dom.Text;

public class UserInfoActivity extends AppCompatActivity {

    private String name;

    private String Id;

    private String ImageUrl;

    public static final String NAME = "name";

    public static final String ID = "1000002";

    public static final String IMAGE_ID = "image_id";

    private FloatingActionButton AddFriends;

    private FloatingActionButton SendMsg;

    private TextView content_text;

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
        content_text=(TextView)findViewById(R.id.content_text);
        AddFriends=(FloatingActionButton)findViewById(R.id.AddFrinds);
        SendMsg=(FloatingActionButton)findViewById(R.id.SendMsg);

        AddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        String fruitContent = generateFruitContent(name);
        content_text.setText(fruitContent);
    }


    private String generateFruitContent(String fruitName) {
        StringBuilder fruitContent = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            fruitContent.append(fruitName);
        }
        return fruitContent.toString();
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
