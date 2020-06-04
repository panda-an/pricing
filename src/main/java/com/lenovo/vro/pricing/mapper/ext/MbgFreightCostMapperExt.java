package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.MbgFreightCost;
import com.lenovo.vro.pricing.entity.MbgFreightCostKey;
import com.lenovo.vro.pricing.mapper.MbgFreightCostMapper;

import java.util.List;

public interface MbgFreightCostMapperExt extends MbgFreightCostMapper {

    void insertBatch(List<MbgFreightCost> list);

    void deleteAll();

    List<MbgFreightCost> getMbgFreightCost(MbgFreightCost mbgFreightCost);
}