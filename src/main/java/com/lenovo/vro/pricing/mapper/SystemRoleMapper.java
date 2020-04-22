package com.lenovo.vro.pricing.mapper;


import com.lenovo.vro.pricing.entity.SystemRole;

public interface SystemRoleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SystemRole record);

    int insertSelective(SystemRole record);

    SystemRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SystemRole record);

    int updateByPrimaryKey(SystemRole record);
}