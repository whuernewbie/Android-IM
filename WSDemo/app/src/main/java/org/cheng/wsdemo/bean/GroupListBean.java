package org.cheng.wsdemo.bean;

import org.litepal.crud.DataSupport;

public class GroupListBean extends DataSupport {
    private String gid;

    private String uid;

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
