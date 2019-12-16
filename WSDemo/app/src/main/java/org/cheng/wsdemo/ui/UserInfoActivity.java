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
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;

public class UserInfoActivity extends BaseActivity {

    public static final String NAME = "name";

    public static final String ID = "1000002";

    public static final String IMAGE_ID = "image_id";

    private FloatingActionButton AddFriends;

    private FloatingActionButton SendMsg;

    private String name;

    private String Id="1000002";

    private int ImageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        Intent intent = getIntent();
        name = intent.getStringExtra(NAME);
        ImageId = intent.getIntExtra(IMAGE_ID, 0);
        Id = intent.getStringExtra(ID);

        AddFriends=(FloatingActionButton)findViewById(R.id.AddFrinds);
        SendMsg=(FloatingActionButton)findViewById(R.id.SendMsg);

        AddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(WebSocketService.webSocketConnection.isConnected())
                {
                    AddFriendsBean addFriendsBean = new AddFriendsBean();
                    addFriendsBean.setTo(Id);
                    addFriendsBean.setFrom(FakeDataUtil.SenderUid);
                    addFriendsBean.setType("FriendReq");
                    addFriendsBean.setActionType("request");
                    WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(addFriendsBean));

                    NoticeUtil.ShowImportMsg(NoticeUtil.ADD_FRIENDS,UserInfoActivity.this);
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
                intent.putExtra(MessageActivity.receiverImage, ImageId);
                intent.putExtra(MessageActivity.name, name);
                intent.putExtra(MessageActivity.receiverId, ID);
                startActivity(intent);
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        ImageView fruitImageView = (ImageView) findViewById(R.id.fruit_image_view);
        TextView fruitContentText = (TextView) findViewById(R.id.fruit_content_text);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbar.setTitle(name);
        Glide.with(this).load(ImageId).into(fruitImageView);
        String fruitContent = generateFruitContent(name);
        fruitContentText.setText(fruitContent);
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
