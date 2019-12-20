package org.cheng.wsdemo.bean;

import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.litepal.crud.DataSupport;

import java.util.Date;


public class WebSocketMessageBean extends DataSupport {
    private String msgType;
    private String message;
    private String msgFrom;
    private String msgTo;
    private Date   date;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public WebSocketMessageBean(){
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

}
