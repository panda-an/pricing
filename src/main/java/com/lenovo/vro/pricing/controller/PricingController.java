package com.lenovo.vro.pricing.controller;

import com.github.pagehelper.PageInfo;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderForm;
import com.lenovo.vro.pricing.service.costtype.CostOrderListService;
import com.lenovo.vro.pricing.service.costtype.CostTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CostTypeService costTypeService;

    @Autowired
    private CostOrderListService costOrderListService;

    /**
     * 获取第一层cost tape data
     * @param mtm part number
     * @param country country
     * @param region 登录用户region
     * @return ResponseBean
     */
    @GetMapping(value={"/getCostType/{mtm}/{country}/{region}/{fulfilment}"})
    public ResponseBean getCostType(@PathVariable String mtm, @PathVariable String country,
                                        @PathVariable String region, @PathVariable String fulfilment) {
        ResponseBean bean = new ResponseBean();

        try {
            CostTape costTape = new CostTape();
            if(StringUtils.isEmpty(mtm) || StringUtils.isEmpty(country) ||StringUtils.isEmpty(region)) {
                bean.setCode(CodeConfig.OPERATION_FAILED);
                bean.setMsg("Parameter is empty");
                return bean;
            }

            costTape.setPartNumber(mtm);
            costTape.setCountry(country);
            costTape.setRegion(region);
            costTape.setFulfilment(fulfilment);
            Map<String, Object> map = costTypeService.getCostType(costTape, CodeConfig.COST_TAPE);
            bean.setObj(map);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for mtm has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 获取sbb数据，cost tape第二层
     * @param mtm part number
     * @param plant 第一层的plant
     * @param productFamily productFamily
     * @param subGeo subGeo
     * @return ResponseBean
     */
    @GetMapping(value={"/getSbb/{mtm}/{plant}/{productFamily}/{subGeo}"})
    public ResponseBean getSbb(@PathVariable String mtm, @PathVariable String plant, @PathVariable String productFamily,
                               @PathVariable String subGeo) {
        ResponseBean bean = new ResponseBean();

        try {
            CostTape costTape = new CostTape();
            if(StringUtils.isEmpty(mtm) || StringUtils.isEmpty(plant) ||
                    StringUtils.isEmpty(productFamily) || StringUtils.isEmpty(subGeo)) {
                bean.setCode(CodeConfig.OPERATION_FAILED);
                bean.setMsg("Parameter is empty");
                return bean;
            }

            costTape.setPartNumber(mtm);
            costTape.setPlant(plant);
            costTape.setProductFamily(productFamily);
            costTape.setSubGeo(subGeo);
            Map<String, Object> map = costTypeService.getCostType(costTape, CodeConfig.SBB_TAPE);
            bean.setObj(map);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for mtm has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 国家country下拉框值
     * @param region 用户所属region
     * @return ResponseBean
     */
    @GetMapping("/getCountryByRegion/{region}")
    public ResponseBean getCountryByRegion(@PathVariable String region) {
        ResponseBean bean = new ResponseBean();

        try{
            List<String> listCountry = costTypeService.getCountryList(region);
            bean.setObj(listCountry);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for country has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 根据country获取rebate和air cost基础数据
     * @param country 国家
     * @return ResponseBean bean
     */
    @GetMapping("/getRebateByCountry/{country}")
    public ResponseBean getRebateByCountry(@PathVariable String country) {
        ResponseBean bean = new ResponseBean();

        try {
            List<RegionCountryRebate> list = costTypeService.getRebateByCountry(country);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
            bean.setObj(list);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for rebate has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 获取warranty data
     * @param country country
     * @param warrantyCode 获取warranty code
     * @return ResponseBean
     */
    @GetMapping("/getWarrantyCodeByCondition/{country}/{warrantyCode}")
    public ResponseBean getWarrantyCodeByCondition(@PathVariable String country, @PathVariable String warrantyCode) {
        ResponseBean bean = new ResponseBean();

        try{
            Warranty listWarranty = costTypeService.getWarranty(country, warrantyCode);
            bean.setObj(listWarranty);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for warranty has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 保存创建的order data
     * @param form 数据
     * @return ResponseBean
     */
    @PostMapping("/saveOrder")
    public ResponseBean saveOrder(@RequestBody CostTapeOrderExt form) {
        ResponseBean bean = new ResponseBean();

        if(form == null) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("The data is empty!");
            return bean;
        }

        try {
            String resultCode = costOrderListService.saveOrder(form);
            bean.setCode(resultCode);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Save order has error");
            e.printStackTrace();
        }
        return bean;
    }

    /**
     * 修改的order data
     * @param form 数据
     * @return ResponseBean
     */
    @PostMapping("/updateOrder")
    public ResponseBean updateOrder(@RequestBody CostTapeOrderExt form) {
        ResponseBean bean = new ResponseBean();

        if(form == null) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("The data is empty!");
            return bean;
        }

        try {
            String resultCode = costOrderListService.updateOrder(form);
            bean.setCode(resultCode);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Save order has error");
            e.printStackTrace();
        }
        return bean;
    }

    /**
     * 查询创建的信息列表
     * @param form 筛选条件
     * @return ResponseBean
     */
    @PostMapping("/selectCostTapeOrderList")
    public ResponseBean selectCostTapeOrderList(@RequestBody CostTapeOrderForm form) {
        ResponseBean bean = new ResponseBean();

        if(StringUtils.isEmpty(form.getRegion()) || StringUtils.isEmpty(form.getUserId())) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("The parameter userId or region is empty!");
            return bean;
        }

        try {
            PageInfo<CostTapeOrder> result = costOrderListService.selectCostTapeOrderList(form);
            bean.setObj(result);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query data has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 查看一条新建的列表信息
     * @param id 该条id
     * @return ResponseBean
     */
    @GetMapping("/selectCostTapeOrderDetail/{id}")
    public ResponseBean selectCostTapeOrderDetail(@PathVariable Integer id) {
        ResponseBean bean = new ResponseBean();

        if(id == null || id == 0) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("The parameter id is empty!");
            return bean;
        }

        try {
            CostTapeOrderExt result = costOrderListService.selectCostTapeOrderDetail(id);
            bean.setObj(result);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query data has error");
            e.printStackTrace();
        }

        return bean;
    }

    /**
     * 删除该条信息
     * @param id 该条id
     * @return ResponseBean
     */
    @DeleteMapping("/deleteCostTapeOrder/{id}")
    public ResponseBean deleteCostTapeOrder(@PathVariable Integer id) {
        ResponseBean bean = new ResponseBean();

        if(id == null || id == 0) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("The parameter id is empty!");
            return bean;
        }

        try {
            String result = costOrderListService.deleteCostTapeOrder(id);

            bean.setCode(result);
        } catch (Exception e) {
            logger.error(e.getMessage());
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("delete data has error");
            e.printStackTrace();
        }

        return bean;
    }

    @PostMapping("exportData")
    public void exportData(@RequestBody CostTapeOrderExt form, HttpServletResponse response) {
        try {
            costOrderListService.exportData(form, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/exportData/{id}")
    public void exportData(@PathVariable int id, HttpServletResponse response) {
        try{
            costOrderListService.exportData(id, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
