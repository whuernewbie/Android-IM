package org.cheng.wsdemo.websocket;



import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.cheng.wsdemo.bean.FriendListBean;
import org.cheng.wsdemo.bean.AddFriendsBean;
import org.cheng.wsdemo.bean.GroupInfo;
import org.cheng.wsdemo.bean.GroupListBean;
import org.cheng.wsdemo.bean.UserInfo;
import org.cheng.wsdemo.bean.WebSocketMessageBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.WebSocketUtil;
import org.cheng.wsdemo.ui.MessageActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class MyWebSocket implements Runnable{
    private static final String TAG = "MyWebSocket";
    /** 心跳包间隔 **/
    private static final int BEATSPACE = 2000;
    /** 重连间隔 **/
    private static final int RECONNECTSPACE = 5000;
    /** 最长的无连接时间 **/
    private static final int BEAT_MAX_UNCON_SPACE = 10000;
    /** 上一次接受到心跳包反馈信息的时间 **/
    private static long lastBeatSuccessTime;
    /** 心跳包定时 **/
    private static Timer beatTimer = null;
    /** 心跳包定时任务 **/
    private static TimerTask beatTimerTask = null;

    /** WebSocket重连 **/
    private static Timer wsReconnectTimer = null;
    private static TimerTask wsReconnectTimerTask = null;

    private static final int DELAY = 5000;


    @Override
    public void run() {
        WebSocketService.webSocketConnection = new WebSocketConnection(){
            @Override
            public void sendTextMessage(String payload) {
                super.sendTextMessage(payload);
            }
        };
        startReconnect();//定时重连
    }
    private void connect(){
        try{
            WebSocketService.webSocketConnection.connect(WebSocketUtil.ROOT_URL , getHandler());
        }catch (WebSocketException e){
            e.printStackTrace();
        }
    }
    private WebSocketHandler getHandler(){
        return new WebSocketHandler(){
            @Override
            public void onOpen(){
                super.onOpen();
                /** 关闭重连 **/
                stopReconnect();
                /** 建立连接时开启心跳包 **/
                startBeat();
            }
            @Override
            public void onClose(int code , String reason){
                super.onClose(code , reason);
                Log.i(TAG , code + " , " + reason);
            }

            public String JSONTokener(String in) {
                // consume an optional byte order mark (BOM) if it exists
                if (in != null && in.startsWith("\ufeff")) {
                    in = in.substring(1);
                }
                return in;
            }

            public void handMessage(String payLoad){
                try
                {
                    JSONObject jsonObject=new JSONObject(payLoad);
                    //好友请求接受处理
                    if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.FriendReq.toString()))
                    {
                        AddFriendsBean addFriendsBean=JSON.parseObject(payLoad , new TypeReference<AddFriendsBean>(){});
                        //获取数据库中当前登陆用户收到的好友请求，有好友请求和好友同意两种
                        //List<AddFriendsBean> addFriendsBeans= DataSupport.where("sgTo =?",FakeDataUtil.SenderUid).find(AddFriendsBean.class);
                        //处理好友同意的情况，(应当从数据库中删除该条)，之后添加到好友列表,如果不显示发送的请求，则直接添加好友
                        if(addFriendsBean.getActionType().equals("agree"))
                        {
                            //先存到用户信息表
                            UserInfo userInfo=new UserInfo();
                            userInfo.setUname(addFriendsBean.getMsgFrom());
                            userInfo.setUid(addFriendsBean.getMsgFrom());
                            userInfo.save();

                            //再在用户关系表中绑定用户关系
                            FriendListBean friendListBean =new FriendListBean();
                            friendListBean.setUid1(addFriendsBean.getMsgFrom());
                            friendListBean.setUid2(addFriendsBean.getMsgTo());
                            friendListBean.save();

                        }
                        //处理请求的情况，如果是请求，则放入好友请求表中
                        else if(addFriendsBean.getActionType().equals("request"))
                        {
                            addFriendsBean.save();
                        }
                    }
                    if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.USERCHAT.toString())||jsonObject.get("msgType").toString().equals(MESSAGETYPE.GROUPCHAT.toString()))
                    {

                        WebSocketMessageBean webSocketMessageBean=JSON.parseObject(payLoad , new TypeReference<WebSocketMessageBean>(){});
                        //webSocketMessageBean.getMsgTo()
                        if(!jsonObject.get("msgFrom").toString().equals(FakeDataUtil.SenderUid))
                        {
                            webSocketMessageBean.save();
                        }
                        if(webSocketMessageBean.getMsgType().toString().equals("USERCHAT")||jsonObject.get("msgType").toString().equals(MESSAGETYPE.GROUPCHAT.toString()))
                        {
                            myWebSocketHandler.mySystemMethod(jsonObject);
                        }
                    }
                    //群邀请信息
                    if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.GroupInvite.toString()))
                    {
                        AddFriendsBean addFriendsBean=JSON.parseObject(payLoad , new TypeReference<AddFriendsBean>(){});
                        //如果  收到  某个用户  同意  加群的消息，先添加用户信息到数据库，再添加用户信息到群聊信息
                        if(addFriendsBean.getActionType().equals("agree"))
                        {
                            //添加到所在群聊
                            List<GroupInfo> groupInfos=DataSupport.where("gid= ?",addFriendsBean.getMsgTo()).find(GroupInfo.class);
                            for (GroupInfo groupInfo: groupInfos
                                 ) {
                                if(groupInfo.getGid().equals(addFriendsBean.getMsgTo()))
                                {
                                    String [] person=groupInfo.getPerson();
                                    person[person.length]=addFriendsBean.getMsgFrom();
                                    groupInfo.setPerson(person);
                                    groupInfo.save();
                                }
                            }
                        }
                        //如果收到邀请本用户加群的信息
                        else if(addFriendsBean.getActionType().equals("request"))
                        {
                            addFriendsBean.save();
                        }

                    }
                    //群创建信息
                    if(jsonObject.get("msgType").toString().equals(MESSAGETYPE.GroupCreate.toString()))
                    {
                        if(jsonObject.get("status").toString().equals("ok"))
                        {
                            GroupInfo groupInfo=new GroupInfo();
                            groupInfo.setGid(jsonObject.get("gid").toString());
                            groupInfo.setGname(FakeDataUtil.GroupName);
                            groupInfo.setOwner(FakeDataUtil.SenderUid);
                            groupInfo.setNumber(1);
                            String [] person=new String[]{FakeDataUtil.SenderUid};
                            groupInfo.setPerson(person);

                            groupInfo.save();

                            GroupListBean groupListBean=new GroupListBean();
                            groupListBean.setUid(FakeDataUtil.SenderUid);
                            groupListBean.setGid(jsonObject.get("gid").toString());
                            groupListBean.save();
                        }
                        else
                        {
                            //TODO 群创建失败
                        }
                    }
                    Log.i(TAG  , "接收到返回数据 : " + payLoad);
                }catch (JSONException e)
                {
                    System.out.println(e);
                    //TODO 子线程JSON格式转换错误
                }
            }


            @Override
            public void onTextMessage(String payLoad){
                /** TODO payLoad - 接收到的信息 , 将其转成实体类 ， 方便业务处理**/
                try
                {
                    JSONObject jsonObject=new org.json.JSONObject(JSONTokener(payLoad));

                    //如果收到的是离线消息
                    if(jsonObject.has("offline_message"))
                    {
                        JSONArray jsonArray=new JSONArray(jsonObject.get("offline_message").toString());
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject jsonObject1=new org.json.JSONObject(JSONTokener(jsonArray.get(i).toString()));
                            if(jsonObject1.has("msg"))
                            {
                                handMessage(jsonObject1.get("msg").toString());
                            }
                        }
                    }
                    //如果收到的是一般消息
                    else
                    {
                        handMessage(payLoad);
                    }
                }catch (JSONException e)
                {
                    System.out.println(e);
                    //TODO 子线程JSON格式转换错误
                }
            }
        };
    }

    public static MyWebSocketHandler myWebSocketHandler=new MyWebSocketHandler() {
        @Override
        public void mySystemMethod(JSONObject jsonObject) {
            //TODO 消息转出方法
            }
    };


    private void startBeat(){
        /** 定时器 **/
        beatTimer = new Timer();
        beatTimerTask = new TimerTask() {
            @Override
            public void run() {
                long thisBeatTime = new Date().getTime();
                /** 消息实体 **/
                WebSocketMessageBean webSocketMessageBean = new WebSocketMessageBean();
                webSocketMessageBean.setMsgType(MESSAGETYPE.BEAT);
                /** 只能发送String **/
                if(WebSocketService.webSocketConnection != null && WebSocketService.webSocketConnection.isConnected()){
                    WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(webSocketMessageBean));
                    lastBeatSuccessTime = thisBeatTime;
                }else{
                    if(lastBeatSuccessTime != 0 && (thisBeatTime - lastBeatSuccessTime) > BEAT_MAX_UNCON_SPACE){
                        /** 判断 当前时间 距离 上次连接时间 已经超过 设定的 最长未连接时间 ,开启重连**/
                        stopSendBeat();
                        startReconnect();
                    }
                }
            }
        };
        beatTimer.schedule(beatTimerTask , DELAY , BEATSPACE);
    }
    private void stopSendBeat(){
        try{
            if(beatTimer != null){
                beatTimer.cancel();
                beatTimer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void startReconnect(){
        wsReconnectTimer = new Timer();
        wsReconnectTimerTask = new TimerTask() {
            @Override
            public void run() {
                connect();
            }
        };
        wsReconnectTimer.schedule(wsReconnectTimerTask , DELAY  , RECONNECTSPACE);
    }
    private void stopReconnect(){
        lastBeatSuccessTime = new Date().getTime();
        try{
            if(wsReconnectTimer != null){
                wsReconnectTimer.cancel();
                wsReconnectTimer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
