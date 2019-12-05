package org.cheng.wsdemo.bean;

public class UserInfo {
    //昵称
    private String name;
    //id
    private String id;
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

    public void setId(String Id)
    {
        this.id=Id;
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

    public String getId()
    {
        return id;
    }

    public int getImageId()
    {
        return imageId;
    }

    public String getEmail()
    {
        return email;
    }


}
