package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTapeDetail;
import com.lenovo.vro.pricing.mapper.CostTapeDetailMapper;

import java.util.List;

public interface CostTapeDetailMapperExt extends CostTapeDetailMapper {

    void insertBatch(List<CostTapeDetail> listData);

    void deleteCostTapeDetail(int id);

    List<CostTapeDetail> selectCostTapeOrderDetail(int id);
}
