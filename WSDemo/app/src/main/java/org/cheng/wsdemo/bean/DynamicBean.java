package org.cheng.wsdemo.bean;

public class DynamicBean {
    private String uid;

    private String uname;

    private String headImageUrl;

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    private String context;

    private String address;

    private double loactionx;

    private double loactiony;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLoactionx() {
        return loactionx;
    }

    public void setLoactionx(double loactionx) {
        this.loactionx = loactionx;
    }

    public double getLoactiony() {
        return loactiony;
    }

    public void setLoactiony(double loactiony) {
        this.loactiony = loactiony;
    }
}
