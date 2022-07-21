package com.example.forwardingiphone.model;

public class SendMsg {
    private Integer code;
    private String title;
    private String message;
    private String userName;
    public SendMsg(){

    }

    public SendMsg(String title, String message, String userName) {
        this.title = title;
        this.message = message;
        this.userName = userName;
    }

    public SendMsg(Integer code, String title, String message, String userName) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.userName = userName;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
