package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CostTapeOrder {
    private Integer id;

    private String orderName;

    private String region;

    private String country;

    private String localCurrency;

    private BigDecimal exchangeRates;

    private String transportType;

    private String userId;

    private Date insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName == null ? null : orderName.trim();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region == null ? null : region.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public String getLocalCurrency() {
        return localCurrency;
    }

    public void setLocalCurrency(String localCurrency) {
        this.localCurrency = localCurrency == null ? null : localCurrency.trim();
    }

    public BigDecimal getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(BigDecimal exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType == null ? null : transportType.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}