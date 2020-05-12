package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.mapper.CostTapeListMapper;

import java.util.List;

public interface CostTapeListMapperExt extends CostTapeListMapper {

    void insertBatch(List<CostTapeList> listData);

    void deleteItem(CostTapeList costTapeList);

    void deleteCostTapeList(int id);

    List<CostTapeList> selectCostTapeList(int id);
}