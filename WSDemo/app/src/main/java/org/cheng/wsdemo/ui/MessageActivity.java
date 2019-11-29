package org.cheng.wsdemo.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.MsgAdapter;
import org.cheng.wsdemo.bean.Msgbean;
import org.cheng.wsdemo.bean.WebSocketMessageBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private List<Msgbean> msgList = new ArrayList<Msgbean>();

    private EditText inputText;

    private Button send;

    private RecyclerView msgRecyclerView;

    private MsgAdapter adapter;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputmsg);
        initMsgs(); // 初始化消息数据
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (R.id.send == v.getId()) {
                    /**
                     * 检查连接是否有效，如果有效，发送信息
                     * **/
                    if (WebSocketService.webSocketConnection.isConnected()) {
                        //输入框中的信息
                        String sendText = content;
                        if (!"".equals(sendText)) {
                            //建立消息体类
                            WebSocketMessageBean webSocketMessageBean = new WebSocketMessageBean();
                            //消息类型
                            webSocketMessageBean.setMessageType(MESSAGETYPE.USERCHAT);
                            //用户ID
                            webSocketMessageBean.setSendUserId(FakeDataUtil.SENDUSERID);
                            //发送文字
                            webSocketMessageBean.setMessage(sendText);
                            //转换成JSON并发送
                            WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(webSocketMessageBean));
                        } else {
                            Toast.makeText(mContext, NoticeUtil.NOT_ALLOWED_EMP, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(mContext, NoticeUtil.NO_CONNECT, Toast.LENGTH_LONG).show();
                    }
                    if (!"".equals(content)) {
                        Msgbean msg = new Msgbean(content, Msgbean.TYPE_SENT);
                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新ListView中的显示
                        msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将ListView定位到最后一行
                        inputText.setText(""); // 清空输入框中的内容
                    }
                }
            }
        });
    }

    /*
        调用本方法控制view发送一个text消息
     */
    public void receiveMsg(String stringMsg){
        Msgbean msg=new Msgbean(stringMsg,Msgbean.TYPE_RECEIVED);
        msgList.add(msg);
    }

    private void initMsgs() {
        Msgbean msg1 = new Msgbean("Hello guy.", Msgbean.TYPE_RECEIVED);
        msgList.add(msg1);
        Msgbean msg2 = new Msgbean("Hello. Who is that?", Msgbean.TYPE_SENT);
        msgList.add(msg2);
        Msgbean msg3 = new Msgbean("This is Tom. Nice talking to you. ", Msgbean.TYPE_RECEIVED);
        msgList.add(msg3);
    }
}
