package com.lenovo.vro.pricing.service.costtype.impl;

import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.mapper.ext.WarrantyMapperExt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class CostTapeBaseService {

    protected List<Warranty> getWarrantyDataList(String partNumber, String country, RedisTemplate<String,
            Object> redisTemplate, WarrantyMapperExt warrantyMapperExt) {
        Warranty form = new Warranty();
        form.setPartNumber(partNumber);
        form.setCountry(country);
        String key = partNumber + "-" + country;

        List<Warranty> resultList;
        if(redisTemplate.opsForHash().hasKey("warranty", key)) {
            resultList = (List<Warranty>) redisTemplate.opsForHash().get("warranty", key);
        } else {
            resultList = warrantyMapperExt.selectMtmWarranty(form);

            if(CollectionUtils.isEmpty(resultList)) {
                form.setPartNumber(partNumber.substring(0, 4));
                resultList = warrantyMapperExt.selectMtmWarrantyByPh(form);
            }

            if(!CollectionUtils.isEmpty(resultList)) {
                redisTemplate.opsForHash().put("warranty", key, resultList);
            }
        }

        return resultList;
    }
}
