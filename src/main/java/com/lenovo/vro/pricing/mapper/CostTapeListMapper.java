package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.entity.CostTapeListKey;

public interface CostTapeListMapper {
    int deleteByPrimaryKey(CostTapeListKey key);

    int insert(CostTapeList record);

    int insertSelective(CostTapeList record);

    CostTapeList selectByPrimaryKey(CostTapeListKey key);

    int updateByPrimaryKeySelective(CostTapeList record);

    int updateByPrimaryKey(CostTapeList record);
}