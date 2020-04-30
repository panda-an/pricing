package com.lenovo.vro.pricing.service.costtype.impl;

import com.lenovo.vro.pricing.entity.CostTape;
import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeMapperExt;
import com.lenovo.vro.pricing.mapper.ext.RegionCountryMappingMapperExt;
import com.lenovo.vro.pricing.mapper.ext.WarrantyMapperExt;
import com.lenovo.vro.pricing.service.costtype.CostTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CostTypeServiceImpl implements CostTypeService {

    @Autowired
    private CostTapeMapperExt costTapeMapperExt;

    @Autowired
    private WarrantyMapperExt warrantyMapperExt;

    @Autowired
    private RegionCountryMappingMapperExt regionCountryMappingMapperExt;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CostTapeExt getCostType(CostTape costTape) throws Exception {

        final String COST_DATA = "cost_hash";

        String region = costTape.getRegion();
        String country = costTape.getCountry();
        String partNumber = costTape.getPartNumber();
        if(StringUtils.isEmpty(region) || StringUtils.isEmpty(country) || StringUtils.isEmpty(partNumber)) {
            logger.error("Parameter is empty");
            throw new Exception("Parameter is empty");
        }

        CostTapeExt result;
        String key = region + "-" + country + "-" + partNumber;
        if(redisTemplate.opsForHash().hasKey(COST_DATA, key)) {
            result = (CostTapeExt) redisTemplate.opsForHash().get(COST_DATA, key);
        } else {
            List<CostTapeExt> costTapeExtList = costTapeMapperExt.getCostTapeData(costTape);
            if(!CollectionUtils.isEmpty(costTapeExtList) && costTapeExtList.size() > 1) {
                // family same
                if(costTapeExtList.stream().map(CostTapeExt::getProductFamily).distinct().count() == 1) {
                    // priority same
                    if(costTapeExtList.stream().map(CostTapeExt::getPriority).distinct().count() == 1) {
                        // same geo
                        if(costTapeExtList.stream().map(CostTapeExt::getSubGeo).distinct().count() == 1) {
                            result = filterSameGeoList(costTapeExtList);
                            if(result == null) {
                                logger.error("Can not filter cost tape under same geo, priority for mtm: {}", partNumber);
                                throw new Exception("Can not filter cost tape");
                            } else {
                                redisTemplate.opsForHash().put(COST_DATA, key, result);
                            }
                        } else {
                            // geo contains all and non-all
                            if(costTapeExtList.stream().anyMatch(n -> n.getSubGeo().equals("ALL"))) {
                                List<CostTapeExt> sameGeoList = costTapeExtList.stream().filter(n -> !n.getSubGeo().equals("ALL")).collect(Collectors.toList());
                                // non-geo list size > 1
                                if(sameGeoList.size() > 1) {
                                    result = filterSameGeoList(sameGeoList);
                                    if(result == null) {
                                        logger.error("Cost tape is null for geo has all");
                                        throw new Exception("Can not filter cost tape");
                                    }
                                } else {
                                    result = sameGeoList.get(0);
                                }
                                redisTemplate.opsForHash().put(COST_DATA, key, result);
                            } else {
                                logger.error("Same mtm found no 'ALL' value and diverse geos");
                                throw new Exception("Same mtm found no 'ALL' value and diverse geos");
                            }
                        }
                    } else {
                        // priority not same
                        // same geo
                        if(costTapeExtList.stream().map(CostTapeExt::getSubGeo).distinct().count() == 1) {
                            result = costTapeExtList.stream().max(Comparator.comparing(CostTapeExt::getPriority)).orElse(null);
                            if(result == null) {
                                logger.error("Cost tape is null for same geo diverse priority");
                                throw new Exception("Can not filter cost tape");
                            }
                            redisTemplate.opsForHash().put(COST_DATA, key, result);
                        } else {
                            logger.error("Same mtm found diverse priority and geos");
                            throw new Exception("Same mtm found diverse priority and geos");
                        }
                    }
                } else {
                    logger.error("Family is not same! for mtm: {}", partNumber);
                    throw new Exception("Family is not same!");
                }
            } else {
                result = costTapeExtList.get(0);
                redisTemplate.opsForHash().put(COST_DATA, key, result);
            }
        }

        // warranty - nbmc
        if(result != null && !StringUtils.isEmpty(costTape.getWarrantyCode())) {
            Warranty form = new Warranty();
            form.setCountry(country);
            form.setWarrantyCode(costTape.getWarrantyCode());
            Warranty warranty = warrantyMapperExt.selectByPrimaryKey(form);

            if(warranty != null) {
                result.setNbmc(warranty.getNbmc());
            }
        }

        return result;
    }

    @Override
    public List<String> getCountryList(String region) throws Exception {
        if(!StringUtils.isEmpty(region)) {
            String key = "country - " + region;
            //noinspection ConstantConditions
            if(stringRedisTemplate.hasKey("key")) {
                return stringRedisTemplate.opsForList().range(key, 0, -1);
            }

            List<String> list = regionCountryMappingMapperExt.getCountryByRegion(region);
            stringRedisTemplate.opsForList().rightPushAll(key, list);
            return list;
        } else {
            throw new Exception("Region is empty");
        }
    }

    @Override
    public List<String> getWarrantyCodeList(String country) throws Exception {
        if(!StringUtils.isEmpty(country)) {
            String key = "warranty - " + country;
            //noinspection ConstantConditions
            if(stringRedisTemplate.hasKey(key)) {
                return stringRedisTemplate.opsForList().range(key, 0, -1);
            }

            List<String> list = warrantyMapperExt.getWarrantyCodeList(country);
            stringRedisTemplate.opsForList().rightPushAll(key, list);
            return list;
        } else {
            throw new Exception("Country is empty");
        }
    }

    private CostTapeExt filterSameGeoList(List<CostTapeExt> dataList) {
        return dataList.stream().max(Comparator.comparing(CostTapeExt::getBmc)).orElse(null);
    }

}
