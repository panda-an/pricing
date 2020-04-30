package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.mapper.RegionCountryMappingMapper;

import java.util.List;

public interface RegionCountryMappingMapperExt extends RegionCountryMappingMapper {

    List<String> getCountryByRegion(String region);
}