package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeCryad;

public interface CostTapeCryadMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CostTapeCryad record);

    int insertSelective(CostTapeCryad record);

    CostTapeCryad selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CostTapeCryad record);

    int updateByPrimaryKey(CostTapeCryad record);
}