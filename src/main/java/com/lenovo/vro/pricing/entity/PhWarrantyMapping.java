package com.lenovo.vro.pricing.entity;

import java.util.Date;

public class PhWarrantyMapping {
    private Integer id;

    private String phCode;

    private String type;

    private String warrantyCode;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhCode() {
        return phCode;
    }

    public void setPhCode(String phCode) {
        this.phCode = phCode == null ? null : phCode.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getWarrantyCode() {
        return warrantyCode;
    }

    public void setWarrantyCode(String warrantyCode) {
        this.warrantyCode = warrantyCode == null ? null : warrantyCode.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}