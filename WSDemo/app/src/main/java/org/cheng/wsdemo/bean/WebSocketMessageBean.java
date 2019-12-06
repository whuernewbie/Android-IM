package org.cheng.wsdemo.bean;

import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.litepal.crud.DataSupport;

import java.util.Date;


public class WebSocketMessageBean extends DataSupport {
    private MESSAGETYPE messageType;
    private String message;
    private String sendUserId;
    private String receiverId;
    private Date date;
    private String groupId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

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
                ",groupId='"+groupId+'\''+
                ",date='"+date+'\''+
                '}';
    }
}