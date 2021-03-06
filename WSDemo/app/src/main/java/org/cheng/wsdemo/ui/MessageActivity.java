package org.cheng.wsdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.cheng.wsdemo.R;
import org.cheng.wsdemo.adapter.MsgAdapter;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.Msgbean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.bean.WebSocketMessageBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.NoticeUtil;
import org.cheng.wsdemo.websocket.MyWebSocket;
import org.cheng.wsdemo.websocket.MyWebSocketHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private List<Msgbean> msgList = new ArrayList<Msgbean>();

    private EditText inputText;

    private Button send;

    private Button back;

    private Button setInfo;

    private ImageView leftImage;

    private CircleImageView rightImage;

    private RecyclerView msgRecyclerView;

    private MsgAdapter adapter;

    private Context mContext;

    private String rid;

    private String rname;

    private String rImage;

    private String chatType;

    public static final String name = "Name";

    public static final String msgTo = "Id";

    public static final String receiverImage = "Image";

    public static final String msgType="msgType";

    //消息接受
    @Override
    protected void onResume() {
        super.onResume();
        MyWebSocket.myWebSocketHandler = new LoginHandler();
    }

    class LoginHandler implements MyWebSocketHandler {
        @Override
        public void mySystemMethod(JSONObject jsonObject) {
            System.out.println(jsonObject.toString());
            try {
                if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.GROUPCHAT.toString()))
                {
                    if (jsonObject.get("msgTo").toString().equals(rid)&&!jsonObject.get("msgFrom").toString().equals(FakeDataUtil.SenderUid)) {
                        Msgbean msg = new Msgbean(jsonObject.get("message").toString(), Msgbean.TYPE_RECEIVED);

                        msg.setId(jsonObject.get("msgFrom").toString());
                        msg.setName(jsonObject.get("msgFrom").toString());

                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新ListView中的显示
                        msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将ListView定位到最后一行
                    }
                }
                else if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.USERCHAT.toString()))
                {
                    if (jsonObject.get("msgFrom").toString().equals(rid)&&jsonObject.get("msgTo").toString().equals(FakeDataUtil.SenderUid)) {
                        Msgbean msg = new Msgbean(jsonObject.get("message").toString(), Msgbean.TYPE_RECEIVED);

                        msg.setId(jsonObject.get("msgFrom").toString());
                        msg.setName(jsonObject.get("msgFrom").toString());

                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size() - 1); // 当有新消息时，刷新ListView中的显示
                        msgRecyclerView.scrollToPosition(msgList.size() - 1); // 将ListView定位到最后一行
                    }
                }


            } catch (JSONException e) {
                //TODO JSON格式转换错误
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputmsg);
        mContext = MessageActivity.this;

        final Intent intent = getIntent();
        rid = intent.getStringExtra(msgTo);
        rname = intent.getStringExtra(name);
        chatType=intent.getStringExtra(msgType);
        //rImage = intent.getStringExtra(receiverImage);


        initMsgs(); // 初始化消息数据

        back = (Button) findViewById(R.id.back);
        setInfo = (Button) findViewById(R.id.InfoSet);
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);


        setInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatType.equals(MESSAGETYPE.GROUPCHAT.toString()))
                {
                    Intent intent1=new Intent(mContext, GroupInfoActivity.class);
                    intent1.putExtra(GroupInfoActivity.groupId,rid);
                    startActivity(intent1);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                            webSocketMessageBean.setMsgType(chatType);
                            //用户ID
                            webSocketMessageBean.setMsgFrom(FakeDataUtil.SenderUid);
                            //接收方Id
                            webSocketMessageBean.setMsgTo(rid);
                            //发送文字
                            webSocketMessageBean.setMessage(sendText);
                            //转换成JSON并发送
                            webSocketMessageBean.save();

                            Toast.makeText(mContext, JSON.toJSONString(webSocketMessageBean), Toast.LENGTH_LONG).show();

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

    private void initMsgs() {
        if(chatType.equals(MESSAGETYPE.USERCHAT.toString()))
        {
            List<WebSocketMessageBean> webSocketMessageBeans = DataSupport.where("(msgFrom=? AND msgTo=?)or(msgFrom=? AND msgTo=?)", FakeDataUtil.SenderUid, rid, rid, FakeDataUtil.SenderUid).order("id asc").find(WebSocketMessageBean.class);
            for (WebSocketMessageBean wb : webSocketMessageBeans
            ) {
                if (wb.getMsgTo().toString().equals(FakeDataUtil.SenderUid)) {
                    Msgbean msg1 = new Msgbean(wb.getMessage().toString(), Msgbean.TYPE_RECEIVED);
                    msg1.setId(wb.getMsgFrom());
                    msg1.setName("");
                    msgList.add(msg1);
                } else if (wb.getMsgTo().toString().equals(rid)) {
                    Msgbean msg1 = new Msgbean(wb.getMessage().toString(), Msgbean.TYPE_SENT);
                    msg1.setId(wb.getMsgFrom());
                    msg1.setName("");
                    msgList.add(msg1);
                }
            }
        }
        else if(chatType.equals(MESSAGETYPE.GROUPCHAT.toString()))
        {
            List<WebSocketMessageBean> webSocketMessageBeans=DataSupport.where("msgTo = ?",rid).order("id asc").find(WebSocketMessageBean.class);
            for (WebSocketMessageBean wb : webSocketMessageBeans
            ) {
                if (wb.getMsgFrom().equals(FakeDataUtil.SenderUid)) {
                    Msgbean msg1 = new Msgbean(wb.getMessage().toString(), Msgbean.TYPE_SENT);
                    msg1.setId(wb.getMsgFrom());
                    msg1.setName("");
                    msgList.add(msg1);
                } else
                    {
                    Msgbean msg1 = new Msgbean(wb.getMessage(), Msgbean.TYPE_RECEIVED);
                    msg1.setId(wb.getMsgFrom());
                    msg1.setName("");
                    msgList.add(msg1);
                }
            }
        }

    }
}
