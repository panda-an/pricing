package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.SystemMenu;
import com.lenovo.vro.pricing.mapper.SystemMenuMapper;

import java.util.List;

public interface SystemMenuMapperExt extends SystemMenuMapper {
    List<SystemMenu> getUserMenuList(String userName);
}