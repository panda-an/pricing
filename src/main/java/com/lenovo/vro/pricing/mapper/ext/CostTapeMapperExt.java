package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTape;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;

import java.util.List;

public interface CostTapeMapperExt {
    List<CostTapeExt> getCostTapeData(CostTape costTape);
}