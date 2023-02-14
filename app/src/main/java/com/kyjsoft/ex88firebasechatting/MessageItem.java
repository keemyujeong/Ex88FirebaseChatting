package com.kyjsoft.ex88firebasechatting;

public class MessageItem {

    public String name;
    public String message;
    public String url;
    public String time;

    public MessageItem(String name, String message, String url, String time) {
        this.name = name;
        this.message = message;
        this.url = url;
        this.time = time;
    }

    public MessageItem() {
    }

}
