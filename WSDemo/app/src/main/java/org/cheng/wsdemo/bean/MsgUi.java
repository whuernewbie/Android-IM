package org.cheng.wsdemo.bean;

import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.litepal.crud.DataSupport;

public class MsgUi extends DataSupport {
    private MESSAGETYPE msgType;

    private String msgname;

    private String msgId;

    private String msgImageUrl;

    public String getMsgImageUrl() {
        return msgImageUrl;
    }

    public void setMsgImageUrl(String msgImageUrl) {
        this.msgImageUrl = msgImageUrl;
    }

    private String lastMsg;

    public void MsgUi(){

    }

    public MESSAGETYPE getMsgType() {
        return msgType;
    }

    public void setMsgType(MESSAGETYPE msgType) {
        this.msgType = msgType;
    }

    public String getMsgname() {
        return msgname;
    }

    public void setMsgname(String msgname) {
        this.msgname = msgname;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setLastMsg(String lastMsg)
    {
        this.lastMsg=lastMsg;
    }

    public String getLastMsg()
    {
        return lastMsg;
    }
}
