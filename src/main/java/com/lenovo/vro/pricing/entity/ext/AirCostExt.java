package com.lenovo.vro.pricing.entity.ext;

import com.lenovo.vro.pricing.entity.AirCost;

public class AirCostExt extends AirCost {
    private String partNumber;

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
}
