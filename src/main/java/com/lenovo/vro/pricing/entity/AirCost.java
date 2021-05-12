package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class AirCost {
    private Integer id;

    private String country;

    private String type;

    private String mot;

    private BigDecimal cost;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getMot() {
        return mot;
    }

    public void setMot(String mot) {
        this.mot = mot == null ? null : mot.trim();
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}