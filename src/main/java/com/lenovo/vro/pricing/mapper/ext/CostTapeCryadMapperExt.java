package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTape;
import com.lenovo.vro.pricing.entity.CostTapeCryad;
import com.lenovo.vro.pricing.mapper.CostTapeCryadMapper;

public interface CostTapeCryadMapperExt extends CostTapeCryadMapper {
    CostTapeCryad getCryad(CostTape costTape);
}