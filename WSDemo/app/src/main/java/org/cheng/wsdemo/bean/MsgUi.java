package org.cheng.wsdemo.bean;

import org.litepal.crud.DataSupport;

public class MsgUi extends DataSupport {
    private UserInfo userInfo;

    private String lastMsg;

    public void MsgUi(){

    }

    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo=userInfo;
    }

    public void setLastMsg(String lastMsg)
    {
        this.lastMsg=lastMsg;
    }

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public String getLastMsg()
    {
        return lastMsg;
    }
}
