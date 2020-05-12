package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeDetail;

public interface CostTapeDetailMapper {
    int insert(CostTapeDetail record);

    int insertSelective(CostTapeDetail record);
}