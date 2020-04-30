package com.lenovo.vro.pricing.entity.ext;

import java.io.Serializable;
import java.math.BigDecimal;

public class CostTapeExt implements Serializable {

    private String partNumber;

    private String plant;

    private String country;

    private String productFamily;

    private String priority;

    private String subGeo;

    private String costDescription;

    private String warrantyDescription;

    private BigDecimal bmc;

    private BigDecimal nbmc;

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSubGeo() {
        return subGeo;
    }

    public void setSubGeo(String subGeo) {
        this.subGeo = subGeo;
    }

    public BigDecimal getBmc() {
        return bmc;
    }

    public void setBmc(BigDecimal bmc) {
        this.bmc = bmc;
    }

    public String getCostDescription() {
        return costDescription;
    }

    public void setCostDescription(String costDescription) {
        this.costDescription = costDescription;
    }

    public String getWarrantyDescription() {
        return warrantyDescription;
    }

    public void setWarrantyDescription(String warrantyDescription) {
        this.warrantyDescription = warrantyDescription;
    }

    public BigDecimal getNbmc() {
        return nbmc;
    }

    public void setNbmc(BigDecimal nbmc) {
        this.nbmc = nbmc;
    }
}
