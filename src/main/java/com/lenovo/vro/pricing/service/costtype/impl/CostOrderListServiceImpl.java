package com.lenovo.vro.pricing.service.costtype.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.CostTapeDetail;
import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.entity.CostTapeOrder;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderForm;
import com.lenovo.vro.pricing.mapper.ext.CostTapeDetailMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeListMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeOrderMapperExt;
import com.lenovo.vro.pricing.service.costtype.CostOrderListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CostOrderListServiceImpl implements CostOrderListService {

    @Autowired
    private CostTapeOrderMapperExt costTapeOrderMapperExt;

    @Autowired
    private CostTapeDetailMapperExt costTapeDetailMapperExt;

    @Autowired
    private CostTapeListMapperExt costTapeListMapperExt;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public PageInfo<CostTapeOrder> selectCostTapeOrderList(CostTapeOrderForm form) {
        PageHelper.offsetPage(form.getPageNum(), form.getPageSize());
        List<CostTapeOrder> result = costTapeOrderMapperExt.selectCostTapeList(form);
        return new PageInfo<>(result);
    }

    @Override
    public CostTapeOrderExt selectCostTapeOrderDetail(Integer id) {
        CostTapeOrderExt order = costTapeOrderMapperExt.selectByPrimaryKey(id);
        List<CostTapeDetail> detailList = costTapeDetailMapperExt.selectCostTapeOrderDetail(id);
        List<CostTapeList> costTapeListList = costTapeListMapperExt.selectCostTapeList(id);

        if(order != null) {
            order.setCostTapeDetailList(detailList);
            order.setCostTapeListList(costTapeListList);
        }

        return order;
    }

    @Override
    public String deleteCostTapeOrder(Integer id) {
        costTapeOrderMapperExt.deleteCostTapeOrder(id);
        costTapeDetailMapperExt.deleteCostTapeDetail(id);
        costTapeListMapperExt.deleteCostTapeList(id);

        return CodeConfig.OPERATION_SUCCESS;
    }

    @Override
    public String saveOrder(CostTapeOrderExt form) {

        CostTapeOrder insertDto = new CostTapeOrder();
        BeanUtils.copyProperties(form, insertDto);

        int id = costTapeOrderMapperExt.insertSelective(insertDto);

        if(id != 0) {
            List<CostTapeList> costTapeListList = form.getCostTapeListList();
            List<CostTapeDetail> costTapeDetailList = form.getCostTapeDetailList();

            try{
                if(!CollectionUtils.isEmpty(costTapeDetailList)) {
                    costTapeDetailList.forEach(n -> n.setCostId(id));
                    costTapeDetailMapperExt.insertBatch(costTapeDetailList);
                }

                if(!CollectionUtils.isEmpty(costTapeListList)) {
                    costTapeListList.forEach(n -> n.setCostId(id));
                    costTapeListMapperExt.insertBatch(costTapeListList);
                }

                return CodeConfig.OPERATION_SUCCESS;
            } catch (Exception e) {
                rollBackCostTapeSave(id);
                throw e;
            }

        }

        logger.error("Insert id is 0");
        return CodeConfig.OPERATION_FAILED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOrder(CostTapeOrderExt form) {
        CostTapeOrder updateDto = new CostTapeOrder();
        BeanUtils.copyProperties(form, updateDto);
        int id = form.getId();

        try {
            costTapeOrderMapperExt.updateByPrimaryKeySelective(updateDto);


            List<CostTapeList> costTapeListList = form.getCostTapeListList();
            List<CostTapeDetail> costTapeDetailList = form.getCostTapeDetailList();

            if(!CollectionUtils.isEmpty(costTapeListList)) {
                costTapeListMapperExt.deleteCostTapeList(id);
                costTapeListMapperExt.insertBatch(costTapeListList);
            }

            if(!CollectionUtils.isEmpty(costTapeDetailList)) {
                costTapeDetailMapperExt.deleteCostTapeDetail(id);
                costTapeDetailMapperExt.insertBatch(costTapeDetailList);
            }

            return CodeConfig.OPERATION_SUCCESS;
        } catch (Exception e) {

            throw e;
        }

    }


    private void rollBackCostTapeSave(int id) {
        costTapeOrderMapperExt.deleteByPrimaryKey(id);
        costTapeDetailMapperExt.deleteCostTapeDetail(id);
        costTapeListMapperExt.deleteCostTapeList(id);
    }
}
