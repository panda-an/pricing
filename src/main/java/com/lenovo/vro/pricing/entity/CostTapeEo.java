package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CostTapeEo {
    private Integer id;

    private String brand;

    private String subgeo;

    private BigDecimal nbmc;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand == null ? null : brand.trim();
    }

    public String getSubgeo() {
        return subgeo;
    }

    public void setSubgeo(String subgeo) {
        this.subgeo = subgeo == null ? null : subgeo.trim();
    }

    public BigDecimal getNbmc() {
        return nbmc;
    }

    public void setNbmc(BigDecimal nbmc) {
        this.nbmc = nbmc;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}