package org.cheng.wsdemo.bean;

import org.litepal.crud.DataSupport;

public class UserInfo extends DataSupport {
    //昵称
    private String name;
    //id
    private String uid;
    //头像
    private int imageId;
    //邮箱
    private String email;


    public void UserInfo()
    {

    }

    public void setName(String name){
        this.name=name;
    }

    public void setImageId(int imageId)
    {
        this.imageId=imageId;
    }

    public void setEmail(String email)
    {
        this.email=email;
    }

    public String getName()
    {
        return name;
    }


    public int getImageId()
    {
        return imageId;
    }

    public String getEmail()
    {
        return email;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
