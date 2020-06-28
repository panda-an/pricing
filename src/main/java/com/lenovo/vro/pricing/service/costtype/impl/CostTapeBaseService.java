package com.lenovo.vro.pricing.service.costtype.impl;

import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.mapper.ext.WarrantyMapperExt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CostTapeBaseService {

    protected HashMap<String, List<Warranty>> getWarrantyDataList(String partNumber, String country, RedisTemplate<String,
            Object> redisTemplate, WarrantyMapperExt warrantyMapperExt) {
        HashMap<String, List<Warranty>> warrantyMap = new HashMap<>();

        Warranty form = new Warranty();
        form.setPartNumber(partNumber);
        form.setCountry(country);
        String key = partNumber + "-" + country;
        String warrantyKey = "warranty";
        String phWarrantyKey = "warranty-ph";

        List<Warranty> resultList;
        List<Warranty> phResultList = new ArrayList<>();

        if(redisTemplate.opsForHash().hasKey(warrantyKey, key)) {
            resultList = (List<Warranty>) redisTemplate.opsForHash().get(warrantyKey, key);
        } else {
            resultList = warrantyMapperExt.selectMtmWarranty(form);

            if(!CollectionUtils.isEmpty(resultList)) {
                redisTemplate.opsForHash().put(warrantyKey, key, resultList);
            }
        }

        if(CollectionUtils.isEmpty(resultList) || resultList.size() == 1) {
            if(redisTemplate.opsForHash().hasKey(phWarrantyKey, key)) {
                phResultList = (List<Warranty>) redisTemplate.opsForHash().get(phWarrantyKey, key);
            } else {
                form.setPartNumber(partNumber.substring(0, 4));
                phResultList = warrantyMapperExt.selectMtmWarrantyByPh(form);

                if(!CollectionUtils.isEmpty(phResultList)) {
                    if(!CollectionUtils.isEmpty(resultList)) {
                        phResultList = phResultList.stream().
                                filter(e -> !resultList.stream().map(Warranty::getWarrantyCode).collect(Collectors.toList()).contains(e.getWarrantyCode())).
                                collect(Collectors.toList());
                    }
                    redisTemplate.opsForHash().put(phWarrantyKey, key, phResultList);
                }
            }
        }

        warrantyMap.put("warrantyPh", phResultList);
        warrantyMap.put("warranty", resultList);


        return warrantyMap;
    }
}
