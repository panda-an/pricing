package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTapeGsc;
import com.lenovo.vro.pricing.mapper.CostTapeGscMapper;

import java.util.List;

public interface CostTapeGscMapperExt extends CostTapeGscMapper {

    void insertBatch(List<CostTapeGsc> list);

    void deleteAll();

}