package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.PhWarrantyMapping;

public interface PhWarrantyMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PhWarrantyMapping record);

    int insertSelective(PhWarrantyMapping record);

    PhWarrantyMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PhWarrantyMapping record);

    int updateByPrimaryKey(PhWarrantyMapping record);
}