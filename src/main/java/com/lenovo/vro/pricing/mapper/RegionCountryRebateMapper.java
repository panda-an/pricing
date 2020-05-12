package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.RegionCountryRebate;

public interface RegionCountryRebateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RegionCountryRebate record);

    int insertSelective(RegionCountryRebate record);

    RegionCountryRebate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RegionCountryRebate record);

    int updateByPrimaryKey(RegionCountryRebate record);
}