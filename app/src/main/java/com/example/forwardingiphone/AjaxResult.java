package com.example.forwardingiphone;

import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private int code;
    private String msg;
    private Object data;

    public AjaxResult() {
    }

    public AjaxResult(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public AjaxResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public AjaxResult(String msg, Object data) {
        this.msg = msg;
        this.data = data;
    }

    public AjaxResult(String msg) {
        this.msg = msg;
    }

    public AjaxResult(Object data) {
        this.data = data;
    }

    public AjaxResult(Integer code) {
        this.code = code;
    }

    public AjaxResult(Integer code, Object data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
