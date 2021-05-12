package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.AirCost;
import com.lenovo.vro.pricing.mapper.AirCostMapper;

import java.util.List;

public interface AirCostMapperExt extends AirCostMapper {

    void insertBatch(List<AirCost> list);

    void deleteAll();

    AirCost getCostTapeAirCost1(AirCost form);
}