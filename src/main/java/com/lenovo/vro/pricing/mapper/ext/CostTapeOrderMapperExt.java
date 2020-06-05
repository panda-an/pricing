package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTapeOrder;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;
import com.lenovo.vro.pricing.mapper.CostTapeOrderMapper;

import java.util.List;

public interface CostTapeOrderMapperExt extends CostTapeOrderMapper {

    List<CostTapeOrder> selectCostTapeList(CostTapeOrder form);

    void deleteCostTapeOrder(Integer id);

    CostTapeOrderExt selectById(Integer id);
}