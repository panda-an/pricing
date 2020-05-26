package com.lenovo.vro.pricing.service.costtype;

import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;

import java.util.List;
import java.util.Map;

public interface CostTypeService {
    Map<String, Object> getCostType(CostTape costTape, String type) throws Exception;

    List<String> getCountryList(String region) throws Exception;

    Warranty getWarranty(String country, String warrantyCode) throws Exception;

    List<RegionCountryRebate> getRebateByCountry(String country);

    List<TransportCost> changeTransportType(List<AirCostForm> list);
}
