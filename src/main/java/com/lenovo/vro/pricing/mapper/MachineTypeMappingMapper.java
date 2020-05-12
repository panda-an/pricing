package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.MachineTypeMapping;

public interface MachineTypeMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MachineTypeMapping record);

    int insertSelective(MachineTypeMapping record);

    MachineTypeMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MachineTypeMapping record);

    int updateByPrimaryKey(MachineTypeMapping record);
}