package com.lenovo.vro.pricing.entity;

import java.util.Date;

public class CostTapeBuMapping {
    private Integer id;

    private String bu;

    private String family;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu == null ? null : bu.trim();
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family == null ? null : family.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}