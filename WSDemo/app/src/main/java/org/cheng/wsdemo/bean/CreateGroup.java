package org.cheng.wsdemo.bean;

import org.cheng.wsdemo.enums.MESSAGETYPE;
import org.cheng.wsdemo.util.FakeDataUtil;

public class CreateGroup {
    private MESSAGETYPE msgType;

    private String  msgFrom;

    private String [] person=new String[FakeDataUtil.MaxGroupNum];

    private String gname;

    public MESSAGETYPE getMsgType() {
        return msgType;
    }

    public void setMsgType(MESSAGETYPE msgType) {
        this.msgType = msgType;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public String[] getPerson() {
        return person;
    }

    public void setPerson(String[] person) {
        this.person = person;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }
}
