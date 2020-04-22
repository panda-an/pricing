package com.lenovo.vro.pricing.mapper.ext;

import com.lenovo.vro.pricing.entity.SystemUser;
import com.lenovo.vro.pricing.entity.ext.SystemUserExt;
import com.lenovo.vro.pricing.mapper.SystemUserMapper;

public interface SystemUserMapperExt extends SystemUserMapper {

    SystemUserExt selectSystemUser(SystemUser systemUser);

    int checkUserName(String userName);
}
