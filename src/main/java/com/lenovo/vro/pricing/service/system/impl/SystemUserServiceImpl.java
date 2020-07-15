package com.lenovo.vro.pricing.service.system.impl;

import com.lenovo.vro.pricing.entity.SystemMenu;
import com.lenovo.vro.pricing.entity.SystemRole;
import com.lenovo.vro.pricing.entity.SystemUser;
import com.lenovo.vro.pricing.entity.ext.SystemUserExt;
import com.lenovo.vro.pricing.mapper.ext.SystemMenuMapperExt;
import com.lenovo.vro.pricing.mapper.ext.SystemRoleMapperExt;
import com.lenovo.vro.pricing.mapper.ext.SystemUserMapperExt;
import com.lenovo.vro.pricing.service.system.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemUserServiceImpl implements SystemUserService {

    @Autowired
    private SystemUserMapperExt systemUserMapperExt;

    @Autowired
    private SystemRoleMapperExt systemRoleMapperExt;

    @Autowired
    private SystemMenuMapperExt systemMenuMapperExt;

    @Override
    public SystemUserExt selectSystemUser(SystemUser systemUser) {
        return systemUserMapperExt.selectSystemUser(systemUser);
    }

    @Override
    public SystemRole selectSystemRole(int roleId) {
        return systemRoleMapperExt.selectByPrimaryKey(roleId);
    }

    @Override
    public void insertUser(SystemUser systemUser) throws Exception {
        if(checkUserName(systemUser.getUserName()) == 0 ) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            systemUser.setPassword(encoder.encode(systemUser.getPassword()));
            systemUserMapperExt.insertSelective(systemUser);
        } else {
            throw new Exception("用户名已存在！");
        }
    }

    @Override
    public void updateUser(SystemUser systemUser) {
        systemUserMapperExt.updateByPrimaryKeySelective(systemUser);
    }

    @Override
    public List<SystemMenu> getUserMenuList(String userName) {
        return systemMenuMapperExt.getUserMenuList(userName);
    }

    @Override
    public int checkUserName(String userName) {
        return systemUserMapperExt.checkUserName(userName);
    }

    @Override
    public List<SystemRole> selectSystemRoleList(String del) {
        return systemRoleMapperExt.selectSystemRoleList(del);
    }

    @Override
    public List<SystemUser> getSystemUsers(SystemUser form) {
        return systemUserMapperExt.getSystemUsers(form);
    }

    @Override
    public void resetPassword(SystemUser systemUser) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        systemUser.setPassword(encoder.encode("123456"));
        systemUserMapperExt.updateByPrimaryKeySelective(systemUser);
    }

    @Override
    public void deleteUser(SystemUser systemUser) {
        if(systemUser.getId() != null && systemUser.getId() != 0 && systemUser.getId() != 1) {
            systemUserMapperExt.deleteByPrimaryKey(systemUser.getId());
        }
    }
}
