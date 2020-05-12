package com.lenovo.vro.pricing.entity.ext;

import com.lenovo.vro.pricing.entity.CostTapeDetail;
import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.entity.CostTapeOrder;

import java.util.List;

public class CostTapeOrderExt extends CostTapeOrder {

    private List<CostTapeList> costTapeListList;

    private List<CostTapeDetail> costTapeDetailList;

    public List<CostTapeList> getCostTapeListList() {
        return costTapeListList;
    }

    public void setCostTapeListList(List<CostTapeList> costTapeListList) {
        this.costTapeListList = costTapeListList;
    }

    public List<CostTapeDetail> getCostTapeDetailList() {
        return costTapeDetailList;
    }

    public void setCostTapeDetailList(List<CostTapeDetail> costTapeDetailList) {
        this.costTapeDetailList = costTapeDetailList;
    }


}
