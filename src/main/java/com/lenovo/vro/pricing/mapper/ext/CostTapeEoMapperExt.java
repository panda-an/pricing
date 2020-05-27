package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTapeEo;
import com.lenovo.vro.pricing.mapper.CostTapeEoMapper;

import java.util.List;

public interface CostTapeEoMapperExt extends CostTapeEoMapper {

    void insertBatch(List<CostTapeEo> list);

    void deleteAll();
}