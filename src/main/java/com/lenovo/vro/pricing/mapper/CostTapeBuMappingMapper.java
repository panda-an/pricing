package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeBuMapping;

public interface CostTapeBuMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CostTapeBuMapping record);

    int insertSelective(CostTapeBuMapping record);

    CostTapeBuMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CostTapeBuMapping record);

    int updateByPrimaryKey(CostTapeBuMapping record);
}