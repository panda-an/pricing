package com.lenovo.vro.pricing.entity;

public class MbgFreightCostKey {
    private String productFamily;

    private String country;

    private String mot;

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
}