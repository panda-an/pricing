package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.AirCost;
import com.lenovo.vro.pricing.entity.AirCostForm;
import com.lenovo.vro.pricing.mapper.AirCostMapper;

public interface AirCostMapperExt extends AirCostMapper {

    AirCost getCostTapeAirCost1(AirCostForm form);

    AirCost getCostTapeAirCost2(AirCost form);
}