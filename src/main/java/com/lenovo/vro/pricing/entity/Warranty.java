package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Warranty extends WarrantyKey {
    private String type;

    private String warrantyCode;

    private String subGeo;

    private String brand;

    private BigDecimal nbmc;

    private Date insertTime;

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

    public String getSubGeo() {
        return subGeo;
    }

    public void setSubGeo(String subGeo) {
        this.subGeo = subGeo == null ? null : subGeo.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand == null ? null : brand.trim();
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