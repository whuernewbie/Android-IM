package org.cheng.wsdemo.bean;

public class Msgbean {

    public static final int TYPE_RECEIVED = 0;

    public static final int TYPE_SENT = 1;

    private String name;

    private String id;

    private String Image;

    private String content;

    private int type;

    public Msgbean(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
