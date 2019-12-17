package org.cheng.wsdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.bean.WebSocketMessageBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.ui.BaseActivity;
import org.cheng.wsdemo.ui.CreatGroupActivity;
import org.cheng.wsdemo.ui.FindFriendsActivity;
import org.cheng.wsdemo.ui.FriendRequestActivity;
import org.cheng.wsdemo.ui.LoginActivity;
import org.cheng.wsdemo.ui.MessageActivity;
import org.cheng.wsdemo.ui.MessagesActivity;
import org.cheng.wsdemo.ui.UserInfoActivity;

import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;

public class MainActivity extends BaseActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        //建立Websocket连接
        Intent intentService = new Intent(mContext, WebSocketService.class);
        startService(intentService);

        Intent intent1=new Intent(mContext, CreatGroupActivity.class);
        startActivity(intent1);
        finish();

    }

}
