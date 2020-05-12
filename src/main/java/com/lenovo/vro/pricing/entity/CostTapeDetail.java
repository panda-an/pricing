package com.lenovo.vro.pricing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CostTapeDetail {
    private Integer costId;

    private String category;

    private Integer totalQuantity;

    private BigDecimal totalGrossRev;

    private BigDecimal totalNetRev;

    private BigDecimal totalBmcGpUsdWocc;

    private BigDecimal totalBmcGpPercentWocc;

    private BigDecimal totalTmcGpUsdWocc;

    private BigDecimal totalTmcGpPercentWocc;

    private BigDecimal totalTmcGpUsdCc;

    private BigDecimal totalTmcGpPercentCc;

    private Date insertTime;

    public Integer getCostId() {
        return costId;
    }

    public void setCostId(Integer costId) {
        this.costId = costId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null ? null : category.trim();
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalGrossRev() {
        return totalGrossRev;
    }

    public void setTotalGrossRev(BigDecimal totalGrossRev) {
        this.totalGrossRev = totalGrossRev;
    }

    public BigDecimal getTotalNetRev() {
        return totalNetRev;
    }

    public void setTotalNetRev(BigDecimal totalNetRev) {
        this.totalNetRev = totalNetRev;
    }

    public BigDecimal getTotalBmcGpUsdWocc() {
        return totalBmcGpUsdWocc;
    }

    public void setTotalBmcGpUsdWocc(BigDecimal totalBmcGpUsdWocc) {
        this.totalBmcGpUsdWocc = totalBmcGpUsdWocc;
    }

    public BigDecimal getTotalBmcGpPercentWocc() {
        return totalBmcGpPercentWocc;
    }

    public void setTotalBmcGpPercentWocc(BigDecimal totalBmcGpPercentWocc) {
        this.totalBmcGpPercentWocc = totalBmcGpPercentWocc;
    }

    public BigDecimal getTotalTmcGpUsdWocc() {
        return totalTmcGpUsdWocc;
    }

    public void setTotalTmcGpUsdWocc(BigDecimal totalTmcGpUsdWocc) {
        this.totalTmcGpUsdWocc = totalTmcGpUsdWocc;
    }

    public BigDecimal getTotalTmcGpPercentWocc() {
        return totalTmcGpPercentWocc;
    }

    public void setTotalTmcGpPercentWocc(BigDecimal totalTmcGpPercentWocc) {
        this.totalTmcGpPercentWocc = totalTmcGpPercentWocc;
    }

    public BigDecimal getTotalTmcGpUsdCc() {
        return totalTmcGpUsdCc;
    }

    public void setTotalTmcGpUsdCc(BigDecimal totalTmcGpUsdCc) {
        this.totalTmcGpUsdCc = totalTmcGpUsdCc;
    }

    public BigDecimal getTotalTmcGpPercentCc() {
        return totalTmcGpPercentCc;
    }

    public void setTotalTmcGpPercentCc(BigDecimal totalTmcGpPercentCc) {
        this.totalTmcGpPercentCc = totalTmcGpPercentCc;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}