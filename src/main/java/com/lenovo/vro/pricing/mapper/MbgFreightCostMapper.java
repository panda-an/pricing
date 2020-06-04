package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.MbgFreightCost;

public interface MbgFreightCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MbgFreightCost record);

    int insertSelective(MbgFreightCost record);

    MbgFreightCost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MbgFreightCost record);

    int updateByPrimaryKey(MbgFreightCost record);
}