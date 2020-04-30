package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.CostTypeFamilyMapping;

public interface CostTypeFamilyMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CostTypeFamilyMapping record);

    int insertSelective(CostTypeFamilyMapping record);

    CostTypeFamilyMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CostTypeFamilyMapping record);

    int updateByPrimaryKey(CostTypeFamilyMapping record);
}