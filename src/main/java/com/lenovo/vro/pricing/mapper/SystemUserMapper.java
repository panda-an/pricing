package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.SystemUser;

public interface SystemUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SystemUser record);

    int insertSelective(SystemUser record);

    SystemUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SystemUser record);

    int updateByPrimaryKey(SystemUser record);
}