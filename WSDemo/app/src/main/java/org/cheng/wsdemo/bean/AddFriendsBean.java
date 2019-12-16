package org.cheng.wsdemo.bean;

import org.litepal.crud.DataSupport;

public class AddFriendsBean extends DataSupport {
    private String type;

    private String actionType;

    private String from;

    private String to;



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
