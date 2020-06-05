package com.lenovo.vro.pricing.service.costtype;

import com.github.pagehelper.PageInfo;
import com.lenovo.vro.pricing.entity.CostTapeOrder;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderForm;

import javax.servlet.http.HttpServletResponse;

public interface CostOrderListService {

    PageInfo<CostTapeOrder> selectCostTapeOrderList(CostTapeOrderForm form);

    CostTapeOrderExt selectCostTapeOrderDetail(Integer id, String type);

    String deleteCostTapeOrder(Integer id);

    String saveOrder(CostTapeOrderExt form);

    String updateOrder(CostTapeOrderExt form);

    void exportData(CostTapeOrderExt form, HttpServletResponse response);

    void exportData(int id, HttpServletResponse response);
}
