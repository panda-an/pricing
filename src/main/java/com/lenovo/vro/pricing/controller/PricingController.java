package com.lenovo.vro.pricing.controller;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.ResponseBean;
import com.lenovo.vro.pricing.entity.CostTape;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;
import com.lenovo.vro.pricing.service.costtype.CostTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private CostTypeService costTypeService;

    @GetMapping(value={"/getCostType/{mtm}/{country}/{region}/{warrantyCode}", "/getCostType/{mtm}/{country}/{region}"})
    public ResponseBean getCostTypeList(@PathVariable String mtm, @PathVariable String country,
                                        @PathVariable String region, @PathVariable(required = false) String warrantyCode) {
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
            costTape.setWarrantyCode(warrantyCode);
            CostTapeExt resultList = costTypeService.getCostType(costTape);
            bean.setObj(resultList);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for mtm has error");
            e.printStackTrace();
        }

        return bean;
    }

    @GetMapping("/getCountryByRegion/{region}")
    public ResponseBean getCountryByRegion(@PathVariable String region) {
        ResponseBean bean = new ResponseBean();

        try{
            List<String> listCountry = costTypeService.getCountryList(region);
            bean.setObj(listCountry);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for country has error");
            e.printStackTrace();
        }

        return bean;
    }

    @GetMapping("/getWarrantyCodeByCountry/{country}")
    public ResponseBean getWarrantyCodeByCountry(@PathVariable String country) {
        ResponseBean bean = new ResponseBean();

        try{
            List<String> listWarranty = costTypeService.getWarrantyCodeList(country);
            bean.setObj(listWarranty);
            bean.setCode(CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            bean.setCode(CodeConfig.OPERATION_FAILED);
            bean.setMsg("Query for warranty has error");
            e.printStackTrace();
        }

        return bean;
    }
}
