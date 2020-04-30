package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.entity.WarrantyKey;

public interface WarrantyMapper {
    int deleteByPrimaryKey(WarrantyKey key);

    int insert(Warranty record);

    int insertSelective(Warranty record);

    Warranty selectByPrimaryKey(WarrantyKey key);

    int updateByPrimaryKeySelective(Warranty record);

    int updateByPrimaryKey(Warranty record);
}