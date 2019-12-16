package org.cheng.wsdemo.websocket;



import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.cheng.wsdemo.bean.WebSocketMessageBean;
import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.service.WebSocketService;
import org.cheng.wsdemo.util.FakeDataUtil;
import org.cheng.wsdemo.util.WebSocketUtil;
import org.cheng.wsdemo.ui.MessageActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * create by cheng
 * 2019.06.04
 *
 * **/
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

    private static final int DELAY = 1000;


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
                WebSocketMessageBean webSocketMessageBean = new WebSocketMessageBean();
                //消息类型
                webSocketMessageBean.setMessageType(MESSAGETYPE.USERCHAT);
                //用户ID
                webSocketMessageBean.setSendUserId(FakeDataUtil.SenderUid);
                //接收方Id
                webSocketMessageBean.setReceiverId("100000");
                //发送文字
                webSocketMessageBean.setMessage("测试");
                //转换成JSON并发送
                WebSocketService.webSocketConnection.sendTextMessage(JSON.toJSONString(webSocketMessageBean));
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

            @Override
            public void onTextMessage(String payLoad){
                /** TODO payLoad - 接收到的信息 , 将其转成实体类 ， 方便业务处理**/
                try
                {
                    JSONObject jsonObject=new org.json.JSONObject(JSONTokener(payLoad));
                    System.out.println(jsonObject.has("messageType"));
                    if(jsonObject.has("messageType"))
                    {
                        WebSocketMessageBean webSocketMessageBean = JSON.parseObject(payLoad , new TypeReference<WebSocketMessageBean>(){});
                        webSocketMessageBean.save();
                        if(webSocketMessageBean.getMessageType().toString().equals("USERCHAT"))
                        {
                            myWebSocketHandler.mySystemMethod(jsonObject);
                        }
                    }
                    Log.i(TAG  , "接收到返回数据 : " + payLoad);
                }catch (JSONException e)
                {
                    System.out.println(e);
                    //TODO jse
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
                webSocketMessageBean.setMessageType(MESSAGETYPE.BEAT);
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
