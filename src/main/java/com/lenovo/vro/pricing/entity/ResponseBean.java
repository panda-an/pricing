package com.lenovo.vro.pricing.entity;

public class ResponseBean {

    private String msg;

    private Object obj;

    //"0" false/failed  "1" true/success
    private String code;

    public ResponseBean(){}

    public ResponseBean(String msg, Object obj) {
        this.msg = msg;
        this.obj = obj;
    }

    public ResponseBean(String msg, Object obj, String code) {
        this.msg = msg;
        this.obj = obj;
        this.code = code;
    }

    public ResponseBean(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
