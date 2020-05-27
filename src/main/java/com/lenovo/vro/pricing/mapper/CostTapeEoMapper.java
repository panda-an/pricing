package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeEo;

public interface CostTapeEoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CostTapeEo record);

    int insertSelective(CostTapeEo record);

    CostTapeEo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CostTapeEo record);

    int updateByPrimaryKey(CostTapeEo record);
}