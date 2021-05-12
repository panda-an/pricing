package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeBuffer;

public interface CostTapeBufferMapper {
    int deleteByPrimaryKey(String bu);

    int insert(CostTapeBuffer record);

    int insertSelective(CostTapeBuffer record);

    CostTapeBuffer selectByPrimaryKey(String bu);

    int updateByPrimaryKeySelective(CostTapeBuffer record);

    int updateByPrimaryKey(CostTapeBuffer record);
}