package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CostTapeCryad {
    private Integer id;

    private String country;

    private String key;

    private BigDecimal cryad;

    private BigDecimal cryadPercent;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key == null ? null : key.trim();
    }

    public BigDecimal getCryad() {
        return cryad;
    }

    public void setCryad(BigDecimal cryad) {
        this.cryad = cryad;
    }

    public BigDecimal getCryadPercent() {
        return cryadPercent;
    }

    public void setCryadPercent(BigDecimal cryadPercent) {
        this.cryadPercent = cryadPercent;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}