package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTapeOrder;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;

public interface CostTapeOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CostTapeOrder record);

    int insertSelective(CostTapeOrder record);

    CostTapeOrderExt selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CostTapeOrder record);

    int updateByPrimaryKey(CostTapeOrder record);
}