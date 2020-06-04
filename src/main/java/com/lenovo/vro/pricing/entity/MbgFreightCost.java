package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class MbgFreightCost {
    private Integer id;

    private String productFamily;

    private String country;

    private String mot;

    private String productNumber;

    private BigDecimal fee;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily == null ? null : productFamily.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getMot() {
        return mot;
    }

    public void setMot(String mot) {
        this.mot = mot == null ? null : mot.trim();
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber == null ? null : productNumber.trim();
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}