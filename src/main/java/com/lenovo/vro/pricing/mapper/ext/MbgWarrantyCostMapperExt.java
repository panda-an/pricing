package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.MbgWarrantyCost;
import com.lenovo.vro.pricing.mapper.MbgWarrantyCostMapper;

import java.util.List;

public interface MbgWarrantyCostMapperExt extends MbgWarrantyCostMapper {
    List<MbgWarrantyCost> getWarrantyByRegion(String geo);
}