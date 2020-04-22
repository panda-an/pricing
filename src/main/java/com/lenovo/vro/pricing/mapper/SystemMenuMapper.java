package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.SystemMenu;

public interface SystemMenuMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SystemMenu record);

    int insertSelective(SystemMenu record);

    SystemMenu selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SystemMenu record);

    int updateByPrimaryKey(SystemMenu record);
}