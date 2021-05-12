package com.lenovo.vro.pricing.service.costtype;

import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.entity.ext.AirCostExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;

import java.util.List;
import java.util.Map;

public interface CostTypeService {
    Map<String, Object> getCostType(CostTape costTape, String type, String operationType) throws Exception;

    List<String> getCountryList(String region) throws Exception;

    List<MbgWarrantyCost> getMbgWarrantyCostList(String region) throws Exception;

    Warranty getWarranty(String country, String warrantyCode) throws Exception;

    List<RegionCountryRebate> getRebateByCountry(String country);

    List<TransportCost> changeTransportType(List<AirCostExt> list);

    List<CostTape> getSbbInfo(CostTape form) throws Exception;
}
