package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;

public class CostTapeBuffer {
    private String bu;

    private BigDecimal buffer;

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu == null ? null : bu.trim();
    }

    public BigDecimal getBuffer() {
        return buffer;
    }

    public void setBuffer(BigDecimal buffer) {
        this.buffer = buffer;
    }
}