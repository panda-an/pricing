package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CostTapeList extends CostTapeListKey {
    private Integer pid;

    private String partNumber;

    private String warrantyCode;

    private String plant;

    private String country;

    private String productFamily;

    private String subGeo;

    private String description;

    private BigDecimal bmc;

    private BigDecimal nbmc;

    private BigDecimal total;

    private String type;

    private Date insertTime;

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber == null ? null : partNumber.trim();
    }

    public String getWarrantyCode() {
        return warrantyCode;
    }

    public void setWarrantyCode(String warrantyCode) {
        this.warrantyCode = warrantyCode == null ? null : warrantyCode.trim();
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant == null ? null : plant.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily == null ? null : productFamily.trim();
    }

    public String getSubGeo() {
        return subGeo;
    }

    public void setSubGeo(String subGeo) {
        this.subGeo = subGeo == null ? null : subGeo.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public BigDecimal getBmc() {
        return bmc;
    }

    public void setBmc(BigDecimal bmc) {
        this.bmc = bmc;
    }

    public BigDecimal getNbmc() {
        return nbmc;
    }

    public void setNbmc(BigDecimal nbmc) {
        this.nbmc = nbmc;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}