package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Warranty extends WarrantyKey {
    private BigDecimal nbmc;

    private Date insertTime;

    private String tbaType;

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

    public String getTbaType() {
        return tbaType;
    }

    public void setTbaType(String tbaType) {
        this.tbaType = tbaType;
    }
}