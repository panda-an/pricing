package com.lenovo.vro.pricing.mapper;

import com.lenovo.vro.pricing.entity.MachineTypePhMapping;

public interface MachineTypePhMappingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MachineTypePhMapping record);

    int insertSelective(MachineTypePhMapping record);

    MachineTypePhMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MachineTypePhMapping record);

    int updateByPrimaryKey(MachineTypePhMapping record);
}