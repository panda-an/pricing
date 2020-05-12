package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.RegionCountryRebate;
import com.lenovo.vro.pricing.mapper.RegionCountryRebateMapper;

import java.util.List;

public interface RegionCountryRebateMapperExt extends RegionCountryRebateMapper {

    List<RegionCountryRebate> selectRebateListByCountry(String country);
}