package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;

public class TransportCost {

    private String partNumber;

    private BigDecimal cost;

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
