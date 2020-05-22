package com.lenovo.vro.pricing.service.costtype.impl;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;
import com.lenovo.vro.pricing.mapper.ext.*;
import com.lenovo.vro.pricing.service.costtype.CostTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CostTypeServiceImpl extends CostTapeBaseService implements CostTypeService {

    @Autowired
    private CostTapeMapperExt costTapeMapperExt;

    @Autowired
    private WarrantyMapperExt warrantyMapperExt;

    @Autowired
    private RegionCountryMappingMapperExt regionCountryMappingMapperExt;

    @Autowired
    private RegionCountryRebateMapperExt regionCountryRebateMapperExt;

    @Autowired
    private AirCostMapperExt airCostMapperExt;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String COST_DATA = "cost_hash";

    @Override
    public Map<String, Object> getCostType(CostTape costTape, String type) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        String partNumber = costTape.getPartNumber();
        String country = costTape.getCountry();
        CostTapeExt result;
        String key;
        if(type.equals(CodeConfig.COST_TAPE)) {
            String region = costTape.getRegion();
            key = region + "-" + country + "-" + partNumber;
            costTape.setType("1");
        } else {
            String plant = costTape.getPlant();
            String subGeo = costTape.getSubGeo();
            String family = costTape.getProductFamily();
            key = subGeo + "-" + partNumber + "-" + plant + "-" + family;
        }

        if(redisTemplate.opsForHash().hasKey(COST_DATA, key)) {
            result = (CostTapeExt) redisTemplate.opsForHash().get(COST_DATA, key);
        } else {
            List<CostTapeExt> costTapeExtList;
            if(type.equals(CodeConfig.COST_TAPE)) {
                costTapeExtList = costTapeMapperExt.getCostTapeData(costTape);

                if(costTapeExtList.stream().map(CostTapeExt::getBrand).distinct().
                        anyMatch(n -> n.equalsIgnoreCase("service") || n.equalsIgnoreCase("option ") || n.equalsIgnoreCase("thinkvision"))) {
                    result = filterSameGeoList(costTapeExtList);
                } else {
                    costTape.setType(null);
                    costTapeExtList = costTapeMapperExt.getCostTapeData(costTape);
                    costTapeExtList = filterDataGeo(costTapeExtList);
                    result = filterResult(costTapeExtList, key, partNumber);
                }
            } else {
                costTapeExtList = costTapeMapperExt.getSbbData(costTape);
                costTapeExtList = filterDataGeo(costTapeExtList);
                result = filterResult(costTapeExtList, key, partNumber);
            }
        }

        if(type.equals(CodeConfig.COST_TAPE)) {
            // warranty - nbmc
            if(result != null && partNumber.length() > 4) {
                List<Warranty> warrantyList = getWarrantyDataList(partNumber, country, redisTemplate, warrantyMapperExt);
                if(!CollectionUtils.isEmpty(warrantyList)) {
                    resultMap.put("warranty", warrantyList);
                }
            }

            // air cost
            if(costTape.getFulfilment().equals(CodeConfig.FULFILMENT_AIR)) {
                if(result != null && partNumber.length() > 4) {
                    String machineType = partNumber.substring(0, 4);
                    setAirCost(country, machineType, result);
                }
            } else {
                if(result != null)
                    result.setAirCost(BigDecimal.ZERO);
            }
        }

        if(result != null) {
            resultMap.put("costTape", result);
        }

        return resultMap;
    }

    @Override
    public List<String> getCountryList(String region) throws Exception {
        if(!StringUtils.isEmpty(region)) {
            String key = "country - " + region;
            //noinspection ConstantConditions
            if(stringRedisTemplate.hasKey(key)) {
                return stringRedisTemplate.opsForList().range(key, 0, -1);
            }

            List<String> list = regionCountryMappingMapperExt.getCountryByRegion(region);
            if(!CollectionUtils.isEmpty(list)) {
                stringRedisTemplate.opsForList().rightPushAll(key, list);
            }

            return list;
        } else {
            throw new Exception("Region is empty");
        }
    }

    @Override
    public Warranty getWarranty(String country, String warrantyCode) throws Exception {
        if(!StringUtils.isEmpty(country) && !StringUtils.isEmpty(warrantyCode)) {
            String key = warrantyCode + "-" + country;

            if(redisTemplate.opsForHash().hasKey("warranty", key)) {
                return (Warranty) redisTemplate.opsForHash().get("warranty", key);
            }

            WarrantyKey form = new WarrantyKey();
            form.setCountry(country);

            Warranty result = warrantyMapperExt.selectByPrimaryKey(form);
            if(result != null) {
                redisTemplate.opsForHash().put("warranty", key, result);
            }

            return result;
        } else {
            throw new Exception("Country or warranty code is empty");
        }
    }

    private CostTapeExt filterSameGeoList(List<CostTapeExt> dataList) {
        return dataList.stream().max(Comparator.comparing(CostTapeExt::getBmc)).orElse(null);
    }

    private CostTapeExt filterResult(List<CostTapeExt> costTapeExtList, String key, String partNumber) throws Exception {
        CostTapeExt result;
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
        } else if(!CollectionUtils.isEmpty(costTapeExtList) && costTapeExtList.size() == 1){
            result = costTapeExtList.get(0);
            redisTemplate.opsForHash().put(COST_DATA, key, result);
        } else {
            result = null;
        }

        return result;
    }

    @Override
    public List<RegionCountryRebate> getRebateByCountry(String country) {

        List<RegionCountryRebate> regionCountryRebateList;
        if(redisTemplate.opsForHash().hasKey("rebate", country)) {
            regionCountryRebateList =  (List<RegionCountryRebate>) redisTemplate.opsForHash().get("rebate", country);
        } else {
            regionCountryRebateList = regionCountryRebateMapperExt.selectRebateListByCountry(country);
            redisTemplate.opsForHash().put("rebate", country, regionCountryRebateList);
        }

        return regionCountryRebateList;
    }

    private void setAirCost(String country, String machineType, CostTapeExt result) {
        AirCostForm form = new AirCostForm();
        form.setCountry(country);
        form.setMachineType(machineType);

        String key = country + "-" + machineType;

        if(redisTemplate.opsForHash().hasKey("airCost", key)) {
            AirCost airCost = (AirCost) redisTemplate.opsForHash().get("airCost", key);
            if(airCost != null) {
                result.setAirCost(airCost.getCost());
                return;
            }
        }

        AirCost airCost = airCostMapperExt.getCostTapeAirCost1(form);
        if(airCost == null) {
            String subType;
            if (machineType.startsWith("8") || machineType.startsWith("2")) {
                subType = "NB";
            } else if(machineType.startsWith("9") || machineType.startsWith("F")) {
                subType = "DT";
            } else if(machineType.startsWith("6")) {
                subType = "Visual";
            } else {
                subType = null;
            }

            if(!StringUtils.isEmpty(subType)) {
                AirCost aForm = new AirCost();
                aForm.setCountry(country);
                aForm.setType(subType);

                AirCost subAirCost = airCostMapperExt.getCostTapeAirCost2(aForm);

                if(subAirCost != null) {
                    result.setAirCost(subAirCost.getCost());
                    redisTemplate.opsForHash().put("airCost", key, subAirCost);
                } else {
                    String brand = result.getBrand();
                    if(StringUtils.isEmpty(brand)) {
                        result.setAirCost(BigDecimal.ZERO);
                    } else {
                        String mappingType = getTypeByBrand(brand);
                        aForm.setCountry(country);
                        aForm.setType(mappingType);

                        subAirCost = airCostMapperExt.getCostTapeAirCost2(aForm);

                        if(subAirCost != null) {
                            result.setAirCost(subAirCost.getCost());
                            redisTemplate.opsForHash().put("airCost", key, subAirCost);
                        } else {
                            result.setAirCost(BigDecimal.ZERO);
                        }
                    }
                }
            } else {
                String brand = result.getBrand();
                String mappingType = getTypeByBrand(brand);

                AirCost aForm = new AirCost();
                aForm.setCountry(country);
                aForm.setType(mappingType);

                AirCost subAirCost = airCostMapperExt.getCostTapeAirCost2(aForm);

                if(subAirCost != null) {
                    result.setAirCost(subAirCost.getCost());
                    redisTemplate.opsForHash().put("airCost", key, subAirCost);
                } else {
                    result.setAirCost(BigDecimal.ZERO);
                }
            }
        } else {
            result.setAirCost(airCost.getCost());
            redisTemplate.opsForHash().put("airCost", key, airCost);
        }
    }

    private String getTypeByBrand(String brand) {
        String result;
        switch (brand) {
            case "ThinkVision":
            case "Visual":
                result = "Visual";
                break;
            case "Option":
                result = "Option";
                break;
            default:
                result = "";
        }

        return result;
    }

    private List<CostTapeExt> filterDataGeo(List<CostTapeExt> dataList) {
        return dataList.stream().
                filter(n -> n.getGeo().equals(n.getSubGeo()) || n.getSubGeo().equals("ALL")).collect(Collectors.toList());
    }
}
