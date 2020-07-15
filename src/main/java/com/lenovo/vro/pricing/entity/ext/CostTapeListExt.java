package com.lenovo.vro.pricing.entity.ext;

import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.entity.Warranty;

import java.util.HashMap;
import java.util.List;

public class CostTapeListExt extends CostTapeList {

    private HashMap<String, List<Warranty>> warrantyMap;


    public HashMap<String, List<Warranty>> getWarrantyMap() {
        return warrantyMap;
    }

    public void setWarrantyMap(HashMap<String, List<Warranty>> warrantyMap) {
        this.warrantyMap = warrantyMap;
    }
}
