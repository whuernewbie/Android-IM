package org.cheng.wsdemo.bean;

import org.cheng.wsdemo.util.FakeDataUtil;
import org.litepal.crud.DataSupport;

public class GroupInfo extends DataSupport {
    private String gid;

    private String gname;

    private int number;

    private String headImageUrl;

    private String createTime;

    private String owner;

    private String [] person=new String[FakeDataUtil.MaxGroupNum];

    public String[] getPerson() {
        return person;
    }

    public void setPerson(String[] person) {
        this.person = person;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
