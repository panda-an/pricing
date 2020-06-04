package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.MbgWarrantyCost;

public interface MbgWarrantyCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MbgWarrantyCost record);

    int insertSelective(MbgWarrantyCost record);

    MbgWarrantyCost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MbgWarrantyCost record);

    int updateByPrimaryKey(MbgWarrantyCost record);
}