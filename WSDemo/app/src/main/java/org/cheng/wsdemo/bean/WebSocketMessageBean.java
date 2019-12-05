package org.cheng.wsdemo.bean;

import org.cheng.wsdemo.enums.MESSAGETYPE;


public class WebSocketMessageBean {
    private MESSAGETYPE messageType;
    private String message;
    private String sendUserId;
    private String receiverId;
    private String email;
    private String password;


    public WebSocketMessageBean(){
    }

    public MESSAGETYPE getMessageType() {
        return messageType;
    }

    public void setMessageType(MESSAGETYPE messageType) {
        this.messageType = messageType;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    @Override
    public String toString() {
        return "WebSocketMessageBean{" +
                "messageType=" + messageType +
                ", message='" + message + '\'' +
                ", sendUserId='" + sendUserId + '\'' +
                ", receiverId='"+receiverId+'\''+
                '}';
    }
}
