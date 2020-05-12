package com.lenovo.vro.pricing.service.costtype;

import com.lenovo.vro.pricing.entity.CostTape;
import com.lenovo.vro.pricing.entity.RegionCountryRebate;
import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;

import java.util.List;
import java.util.Map;

public interface CostTypeService {
    CostTapeExt getCostType(CostTape costTape, String type) throws Exception;

    List<String> getCountryList(String region) throws Exception;

    Warranty getWarranty(String country, String warrantyCode) throws Exception;

    List<RegionCountryRebate> getRebateByCountry(String country);

}
