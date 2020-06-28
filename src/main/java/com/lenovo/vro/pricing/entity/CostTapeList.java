package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CostTapeList extends CostTapeListKey {
    private String pid;

    private String partNumber;

    private String warrantyCode;

    private String plant;

    private String country;

    private String productFamily;

    private String geo;

    private String description;

    private String brand;

    private BigDecimal bmc;

    private BigDecimal nbmc;

    private BigDecimal tmc;

    private Integer qty;

    private BigDecimal pricing;

    private BigDecimal airCost;

    private BigDecimal fundings;

    private BigDecimal adjCost;

    private String rebate;

    private Integer mbgFreightId;

    private BigDecimal tmcPercent;

    private BigDecimal bmcPercent;

    private String type;

    private String categoryType;

    private String comment;

    private String warrantyCategoryType;

    private String recoveryType;

    private Date insertTime;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid == null ? null : pid.trim();
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

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo == null ? null : geo.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand == null ? null : brand.trim();
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

    public BigDecimal getTmc() {
        return tmc;
    }

    public void setTmc(BigDecimal tmc) {
        this.tmc = tmc;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getPricing() {
        return pricing;
    }

    public void setPricing(BigDecimal pricing) {
        this.pricing = pricing;
    }

    public BigDecimal getAirCost() {
        return airCost;
    }

    public void setAirCost(BigDecimal airCost) {
        this.airCost = airCost;
    }

    public BigDecimal getFundings() {
        return fundings;
    }

    public void setFundings(BigDecimal fundings) {
        this.fundings = fundings;
    }

    public BigDecimal getAdjCost() {
        return adjCost;
    }

    public void setAdjCost(BigDecimal adjCost) {
        this.adjCost = adjCost;
    }

    public String getRebate() {
        return rebate;
    }

    public void setRebate(String rebate) {
        this.rebate = rebate == null ? null : rebate.trim();
    }

    public Integer getMbgFreightId() {
        return mbgFreightId;
    }

    public void setMbgFreightId(Integer mbgFreightId) {
        this.mbgFreightId = mbgFreightId;
    }

    public BigDecimal getTmcPercent() {
        return tmcPercent;
    }

    public void setTmcPercent(BigDecimal tmcPercent) {
        this.tmcPercent = tmcPercent;
    }

    public BigDecimal getBmcPercent() {
        return bmcPercent;
    }

    public void setBmcPercent(BigDecimal bmcPercent) {
        this.bmcPercent = bmcPercent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType == null ? null : categoryType.trim();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? null : comment.trim();
    }

    public String getWarrantyCategoryType() {
        return warrantyCategoryType;
    }

    public void setWarrantyCategoryType(String warrantyCategoryType) {
        this.warrantyCategoryType = warrantyCategoryType == null ? null : warrantyCategoryType.trim();
    }

    public String getRecoveryType() {
        return recoveryType;
    }

    public void setRecoveryType(String recoveryType) {
        this.recoveryType = recoveryType == null ? null : recoveryType.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}