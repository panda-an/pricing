package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.SystemRole;
import com.lenovo.vro.pricing.mapper.SystemRoleMapper;

import java.util.List;

public interface SystemRoleMapperExt extends SystemRoleMapper {
    List<SystemRole> selectSystemRoleList(String del);
}