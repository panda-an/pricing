package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.WarrantyMtmMapping;

public interface WarrantyMtmMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(WarrantyMtmMapping record);

    int insertSelective(WarrantyMtmMapping record);

    WarrantyMtmMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WarrantyMtmMapping record);

    int updateByPrimaryKey(WarrantyMtmMapping record);
}