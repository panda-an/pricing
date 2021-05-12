package com.lenovo.vro.pricing.service.costtype.impl;

import com.lenovo.vro.pricing.common.snowflake.SnowflakeIdWorker;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.entity.ext.AirCostExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeExt;
import com.lenovo.vro.pricing.mapper.CostTapeBufferMapper;
import com.lenovo.vro.pricing.mapper.ext.*;
import com.lenovo.vro.pricing.service.costtype.CostTypeService;
import org.apache.commons.collections4.MapUtils;
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
    private MbgWarrantyCostMapperExt mbgWarrantyCostMapperExt;

    @Autowired
    private MbgFreightCostMapperExt mbgFreightCostMapperExt;

    @Autowired
    private CostTapeCryadMapperExt costTapeCryadMapperExt;

    @Autowired
    private CostTapeBuMappingMapperExt costTapeBuMappingMapperExt;

    @Autowired
    private CostTapeBufferMapper costTapeBufferMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *  查询costTape的方法，先查缓存，如果没有走db查询并放入缓存
     *  缓存依据country区分包含costTape和sbb，使用 type区分。0为PC 1为sbb
     *  costTape data的key是 region和partNumber组合，sbb data是 geo，partN，plant，family组合
     *  operationType分 0和1， 0是正常操作 1是mbg操作
     *  正常PC配置logic为：
     *  1.先查brand =service，option，thinkvision的数据
     *  2.没有1的情况再继续查询，多余1条要进行过滤
     *
     *  之后再计算warranty，freight的cost
     *
     * @param costTape 查询数据
     * @param type costTape或者是sbb
     * @param operationType 非mbg和mbg
     * @return hashmap 包含costTape 和 warranty
     * @throws Exception 各种异常
     */
    @Override
    public Map<String, Object> getCostType(CostTape costTape, String type, String operationType) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        String partNumber = costTape.getPartNumber();
        String country = costTape.getCountry();
        final String hashKey = "costTape" + "-" + country;

        CostTapeExt result;
        String key;

        if(type.equals(CodeConfig.COST_TAPE)) {
            String region = costTape.getRegion();
            key = region + "-" + partNumber;
            costTape.setType("1");
        } else {
            String plant = costTape.getPlant();
            String subGeo = costTape.getSubGeo();
            String family = costTape.getProductFamily();
            key = subGeo + "-" + partNumber + "-" + plant + "-" + family;
        }

        if(redisTemplate.opsForHash().hasKey(hashKey, key)) {
            result = (CostTapeExt) redisTemplate.opsForHash().get(hashKey, key);
        } else {
            List<CostTapeExt> costTapeExtList;
            if(operationType.equals("0")) {
                if(type.equals(CodeConfig.COST_TAPE)) {
                    costTapeExtList = costTapeMapperExt.getCostTapeData(costTape);

                    if(costTapeExtList.stream().map(CostTapeExt::getBrand).filter(brand -> !StringUtils.isEmpty(brand)).distinct().
                            anyMatch(n -> n.equalsIgnoreCase("service") || n.equalsIgnoreCase("option")
                                    || n.equalsIgnoreCase("thinkvision"))) {
                        result = filterSameGeoList(costTapeExtList);
                        if(result != null) {

                            if(result.getBrand().equalsIgnoreCase("option") && !StringUtils.isEmpty(result.getCostDescription())) {
                                CostTapeCryad cryadResult = costTapeCryadMapperExt.getCryad(costTape);

                                if(cryadResult != null) {
                                    BigDecimal cryadNbmc = result.getBmc().multiply(cryadResult.getCryadPercent()).add(cryadResult.getCryad());
                                    result.setNbmc(result.getNbmc()==null?BigDecimal.ZERO.add(cryadNbmc):result.getNbmc().add(cryadNbmc));
                                }
                            }
                            redisTemplate.opsForHash().put(hashKey, key, result);
                        }
                    } else {
                        costTape.setType(null);
                        costTapeExtList = costTapeMapperExt.getCostTapeData(costTape);
                        costTapeExtList = filterDataGeo(costTapeExtList);
                        result = filterResult(costTapeExtList, key, partNumber, hashKey);
                    }
                } else {
                    costTapeExtList = costTapeMapperExt.getSbbData(costTape);
                    costTapeExtList = filterDataGeo(costTapeExtList);
                    result = filterResult(costTapeExtList, key, partNumber, hashKey);
                }
            } else {
                if(type.equals(CodeConfig.COST_TAPE)) {
                    costTapeExtList = costTapeMapperExt.getMbgCostTapeData(costTape);
                    result = filterSameGeoList(costTapeExtList);

                    if(result != null) {
                        redisTemplate.opsForHash().put(hashKey, key, result);
                    }
                } else {
                    result = null;
                }
            }
        }

        //unique id
        if(type.equals(CodeConfig.COST_TAPE) && operationType.equals("0") && result != null) {
            SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);
            long id = idWorker.nextId();
            result.setPid(Long.toString(id));
        }

        // bu
        String bu = null;
        if(type.equals(CodeConfig.COST_TAPE) && result != null) {
            if(operationType.equals("0")) {
                bu = getBuInfo(result);
            } else {
                bu = getMbgBuInfo(result);
            }

            if(!StringUtils.isEmpty(bu)) {
                BigDecimal buffer = getBufferByBu(bu);
                result.setBmc(result.getBmc().add(buffer));
                result.setBu(bu);
            } else {
                result.setBu("");
            }
        }

        //非sbb
        if(type.equals(CodeConfig.COST_TAPE) && result != null) {
            // warranty - nbmc, mbg warranty cost has passed the value when the page is initialization
            if(partNumber.length() > 4 && operationType.equals("0")) {
                HashMap<String, List<Warranty>> warrantyMap = getWarrantyDataList(partNumber, country,
                        result.getBrand(), redisTemplate, warrantyMapperExt);
                if(!MapUtils.isEmpty(warrantyMap)) {
                    resultMap.put("warranty", warrantyMap);
                }
            }

            // non-mbg freight
            if(operationType.equals("0") && !StringUtils.isEmpty(bu)) {
                setAirCost(country, bu, result, costTape.getFulfilment());
            } else {
                // mbg freight only 'za' has value because cost tape don't have 'zg' value
                MbgFreightCost mbgForm = new MbgFreightCost();
                mbgForm.setProductFamily(result.getProductFamily());
                mbgForm.setCountry(country);
                mbgForm.setMot(costTape.getFulfilment());
                if(!costTape.getPartNumber().startsWith("ZG")) {
                    //mbgForm.setProductNumber(costTape.getPartNumber());
                    List<MbgFreightCost> mbgFreightCostList = mbgFreightCostMapperExt.getMbgFreightCost(mbgForm);
                    MbgFreightCost mbgFreightCost;
                    if(!CollectionUtils.isEmpty(mbgFreightCostList)) {
                        mbgFreightCost = mbgFreightCostList.stream().distinct().max(Comparator.comparing(MbgFreightCost::getFee)).orElse(null);

                        result.setAirCost(mbgFreightCost==null?BigDecimal.ZERO:mbgFreightCost.getFee());
                    }
                }
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
            String key = "country-" + region;
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
    public List<MbgWarrantyCost> getMbgWarrantyCostList(String region) throws Exception {
        if(!StringUtils.isEmpty(region)) {
            final String key = "mbgWarranty";
            if(redisTemplate.opsForHash().hasKey(key, region)) {
                return (List<MbgWarrantyCost>) redisTemplate.opsForHash().get(key, region);
            }

            List<MbgWarrantyCost> list = mbgWarrantyCostMapperExt.getWarrantyByRegion(region);
            if(!CollectionUtils.isEmpty(list)) {
                redisTemplate.opsForHash().put(key, region, list);
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

    /**
     * 具体的逻辑是： 分三种情况，
     * 1当product family一样，subgeo一样，priority一样的时候取cost最高
     * 2当product family一样， subgeo有ALL和非All的情况，priority一样选非all的
     * 3当product family一样，subgeo一样，priority不一样选priority高的
     *
     * All non null data will store redis database
     */
    private CostTapeExt filterResult(List<CostTapeExt> costTapeExtList, String key, String partNumber, String hashKey) throws Exception {
        CostTapeExt result;
        if(!CollectionUtils.isEmpty(costTapeExtList) && costTapeExtList.size() > 1) {
            if(partNumber.startsWith("ZA") || partNumber.startsWith("ZG") && costTapeExtList.stream().map(CostTapeExt::getProductFamily).distinct().count() > 1) {
                costTapeExtList = costTapeExtList.stream().filter(n -> !StringUtils.isEmpty(n.getProductFamily())).collect(Collectors.toList());
            }
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
                            redisTemplate.opsForHash().put(hashKey, key, result);
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
                            redisTemplate.opsForHash().put(hashKey, key, result);
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
                        redisTemplate.opsForHash().put(hashKey, key, result);
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
            redisTemplate.opsForHash().put(hashKey, key, result);
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

    @Override
    public List<TransportCost> changeTransportType(List<AirCostExt> list) {
        List<TransportCost> resultList = new ArrayList<>();

        for(AirCostExt form : list) {
            String country = form.getCountry();
            String mot = form.getMot();
            String partNumber = form.getPartNumber();
            String bu = form.getType();

            if(!StringUtils.isEmpty(country) && !StringUtils.isEmpty(mot)
                    &&!StringUtils.isEmpty(partNumber) &&!StringUtils.isEmpty(bu)) {
                String key = country + "-" + bu + "-" + mot;
                TransportCost transportCost = new TransportCost();
                AirCost cost;

                cost = getAirCost(country, bu, mot, key);
                transportCost.setPartNumber(partNumber);
                transportCost.setCost(cost!=null ? cost.getCost() : BigDecimal.ZERO);
                resultList.add(transportCost);
            }
        }

        return resultList;
    }

    @Override
    public List<CostTape> getSbbInfo(CostTape form) throws Exception {
        if(form == null || StringUtils.isEmpty(form.getCountry())) {
            throw new Exception("country is empty!");
        }

        List<CostTape> list;
        if(redisTemplate.opsForHash().hasKey("sbblist", form.getCountry())) {
            list = (List<CostTape>) redisTemplate.opsForHash().get("sbblist", form.getCountry());
        } else {
            list = costTapeMapperExt.getSbbInfo(form);
            redisTemplate.opsForHash().put("sbblist", form.getCountry(), list);
        }

        return list;
    }

    private void setAirCost(String country, String bu, CostTapeExt result, String fulfilment) {
        String key = country + "-" + bu + "-" + fulfilment;

        AirCost airCost = getAirCost(country, bu, fulfilment, key);
        if(airCost == null) {
            result.setAirCost(BigDecimal.ZERO);
        } else {
            result.setAirCost(airCost.getCost());
        }
    }

    private AirCost getAirCost(String country, String bu, String mot, String key) {
        if(redisTemplate.opsForHash().hasKey("airCost", key)) {
            return (AirCost) redisTemplate.opsForHash().get("airCost", key);
        }

        AirCost form = new AirCost();
        form.setCountry(country);
        form.setType(bu);
        form.setMot(mot);

        AirCost airCost = airCostMapperExt.getCostTapeAirCost1(form);
        if(airCost != null) {
            redisTemplate.opsForHash().put("airCost", key, airCost);
            return airCost;
        }
        return null;
    }

    private String getBuInfo(CostTapeExt result) {
        List<CostTapeBuMapping> mappingList = costTapeBuMappingMapperExt.getListCostTapeBuMapping();
        if(!CollectionUtils.isEmpty(mappingList)) {
            String partNumber = result.getPartNumber();
            String family = result.getProductFamily() == null ? "" : result.getProductFamily();
            String brand = result.getBrand() == null ? "" : result.getBrand();
            String mtm = result.getItemType() == null ? "" : result.getItemType();
            if(partNumber.startsWith("1") && (brand.equalsIgnoreCase("ThinkCentre") || brand.equalsIgnoreCase("ThinkEdge")) &&
                    mappingList.stream().noneMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "DT";
            }

            if(partNumber.startsWith("1") && (brand.equalsIgnoreCase("ThinkCentre") || brand.equalsIgnoreCase("ThinkEdge")) &&
                    mappingList.stream().anyMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "DT-Tiny";
            }

            if((partNumber.startsWith("2") || partNumber.startsWith("8")) && (brand.equalsIgnoreCase("ThinkPad") || brand.equalsIgnoreCase("ThinkReality")) &&
                    mappingList.stream().noneMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "NB-Think";
            }

            if((partNumber.startsWith("2") || partNumber.startsWith("8")) && brand.equalsIgnoreCase("ThinkPad") &&
                    mappingList.stream().anyMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "Tablet-Think";
            }

            if((partNumber.startsWith("1") || partNumber.startsWith("6")) &&
                    (brand.equalsIgnoreCase("ThinkVision") || brand.equalsIgnoreCase("Visual")) &&
                    mtm.equalsIgnoreCase("VISUAL")) {
                return "Visual";
            }

            if(brand.equalsIgnoreCase("Option") || brand.equalsIgnoreCase("VLH")) {
                return "Option";
            }

            if((partNumber.startsWith("9") || partNumber.startsWith("F")) &&
                    mappingList.stream().noneMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    brand.equalsIgnoreCase("IdeaCentre") && mtm.equalsIgnoreCase("mtm")) {
                return "DT-Idea";
            }

            if((partNumber.startsWith("9") || partNumber.startsWith("F")) &&
                    mappingList.stream().anyMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    brand.equalsIgnoreCase("IdeaCentre") && mtm.equalsIgnoreCase("mtm")) {
                return "DT-Tiny-Idea";
            }

            if((partNumber.startsWith("2") || partNumber.startsWith("8")) && brand.equalsIgnoreCase("IdeaPad") &&
                    mappingList.stream().noneMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "NB-Idea";
            }

            if((partNumber.startsWith("2") || partNumber.startsWith("8")) && brand.equalsIgnoreCase("IdeaPad") &&
                    mappingList.stream().anyMatch(n -> n.getFamily().equalsIgnoreCase(family)) &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "Tablet-Idea";
            }

            if(partNumber.startsWith("3") && brand.equalsIgnoreCase("ThinkStation") &&
                    mtm.equalsIgnoreCase("mtm")) {
                return "WS";
            }

        }
        return null;
    }

    private String getMbgBuInfo(CostTapeExt result) {
        String description = result.getCostDescription();
        String partNumber = result.getPartNumber();
        if(!StringUtils.isEmpty(partNumber)) {
            if(partNumber.startsWith("ZA") && !description.startsWith("Lenovo CD")) {
                return "MBG Tablet";
            }

            if(partNumber.startsWith("ZA") && description.startsWith("Lenovo CD")) {
                return "MBG Option";
            }
        }

        return null;
    }

    private BigDecimal getBufferByBu(String bu) {
        CostTapeBuffer costTapeBuffer = costTapeBufferMapper.selectByPrimaryKey(bu);
        return costTapeBuffer == null ? BigDecimal.ZERO : costTapeBuffer.getBuffer();
    }

    private String getTypeByBrand(String brand) {
        String result;
        switch (brand) {
            case "ThinkVision":
            case "Visual":
                result = "Visual";
                break;
            case "OPTION":
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
