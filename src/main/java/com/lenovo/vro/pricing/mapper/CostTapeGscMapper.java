package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeGsc;

public interface CostTapeGscMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CostTapeGsc record);

    int insertSelective(CostTapeGsc record);

    CostTapeGsc selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CostTapeGsc record);

    int updateByPrimaryKey(CostTapeGsc record);
}