package com.lenovo.vro.pricing.service.system;

import com.lenovo.vro.pricing.entity.SystemMenu;
import com.lenovo.vro.pricing.entity.SystemRole;
import com.lenovo.vro.pricing.entity.SystemUser;
import com.lenovo.vro.pricing.entity.ext.SystemUserExt;

import java.util.List;

public interface SystemUserService {

    SystemUserExt selectSystemUser(SystemUser systemUser);

    SystemRole selectSystemRole(int roleId);

    List<SystemUser> getSystemUsers(SystemUser form);

    void insertUser(SystemUser systemUser) throws Exception;

    void updateUser(SystemUser systemUser);

    List<SystemMenu> getUserMenuList(String userName);

    int checkUserName(String userName);

    List<SystemRole> selectSystemRoleList(String del);

    void resetPassword(SystemUser systemUser);

    void deleteUser(SystemUser systemUser);
}
