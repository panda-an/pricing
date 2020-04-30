package com.lenovo.vro.pricing.service.costtype;

import com.lenovo.vro.pricing.entity.CostTape;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;

import java.util.List;

public interface CostTypeService {
    CostTapeExt getCostType(CostTape costTapeExt) throws Exception;

    List<String> getCountryList(String region) throws Exception;

    List<String> getWarrantyCodeList(String country) throws Exception;
}
