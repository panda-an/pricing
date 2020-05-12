package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.entity.ext.WarrantyExt;
import com.lenovo.vro.pricing.mapper.WarrantyMapper;

import java.util.List;

public interface WarrantyMapperExt extends WarrantyMapper {
    void deleteAll();

    void insertBatch(List<Warranty> list);

    Warranty selectMtmWarranty(WarrantyExt warrantyExt);

}