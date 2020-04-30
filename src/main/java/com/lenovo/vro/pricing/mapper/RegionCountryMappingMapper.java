package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.RegionCountryMapping;

public interface RegionCountryMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RegionCountryMapping record);

    int insertSelective(RegionCountryMapping record);

    RegionCountryMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RegionCountryMapping record);

    int updateByPrimaryKey(RegionCountryMapping record);
}