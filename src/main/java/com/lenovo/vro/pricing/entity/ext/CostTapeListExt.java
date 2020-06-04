package com.lenovo.vro.pricing.entity.ext;

import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.entity.Warranty;

import java.util.List;

public class CostTapeListExt extends CostTapeList {

    private List<Warranty> warrantyList;


    public List<Warranty> getWarrantyList() {
        return warrantyList;
    }

    public void setWarrantyList(List<Warranty> warrantyList) {
        this.warrantyList = warrantyList;
    }
}
