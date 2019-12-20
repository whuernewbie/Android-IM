package org.cheng.wsdemo.bean;

import org.litepal.crud.DataSupport;

public class DynamicBean extends DataSupport implements Comparable<DynamicBean>{
    private String uid;

    private String uname;

    private String headImageUrl;

    private String content;

    private String address;

    private double locationx;

    private double locationy;

    private String time;


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


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLocationx() {
        return locationx;
    }

    public void setLocationx(double locationx) {
        this.locationx = locationx;
    }

    public double getLocationy() {
        return locationy;
    }

    public void setLocationy(double locationy) {
        this.locationy = locationy;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int compareTo(DynamicBean dynamicBean)
    {
        if(dynamicBean.getTime()==null||this.time==null)
        {
            return 0;
        }
        int a=Integer.valueOf(this.time).intValue();
        int b=Integer.valueOf(dynamicBean.time).intValue();
        return b-a;
    }

}
