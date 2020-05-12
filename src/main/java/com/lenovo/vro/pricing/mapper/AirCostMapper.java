package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.AirCost;

public interface AirCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AirCost record);

    int insertSelective(AirCost record);

    AirCost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AirCost record);

    int updateByPrimaryKey(AirCost record);
}