package com.okunev.waifusim;

/**
 * Created by gwa on 6/13/16.
 */

public class MessageItem {
    String msg, time;

    public MessageItem(String msg, String time) {
        this.msg = msg;
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
