package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTapeBuMapping;
import com.lenovo.vro.pricing.mapper.CostTapeBuMappingMapper;

import java.util.List;

public interface CostTapeBuMappingMapperExt extends CostTapeBuMappingMapper {
   List<CostTapeBuMapping> getListCostTapeBuMapping();

   void deleteAll();

   void insertBatch(List<CostTapeBuMapping> list);
}