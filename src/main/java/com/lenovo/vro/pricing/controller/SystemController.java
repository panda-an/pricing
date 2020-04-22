package com.lenovo.vro.pricing.controller;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.ResponseBean;
import com.lenovo.vro.pricing.entity.SystemMenu;
import com.lenovo.vro.pricing.entity.SystemRole;
import com.lenovo.vro.pricing.entity.SystemUser;
import com.lenovo.vro.pricing.service.system.SystemUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system")
public class SystemController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SystemUserService systemUserService;

    @GetMapping("/checkUserName/{userName}")
    public ResponseBean checkUserName(@PathVariable("userName") String userName) {
        try {
            int count = 0;
            if(!StringUtils.isEmpty(userName)) {
                count  = systemUserService.checkUserName(userName);
            }

            return new ResponseBean("查询成功!", count, CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseBean("查询失败!", CodeConfig.OPERATION_FAILED);
        }
    }

    @PostMapping("/insertUser")
    public ResponseBean insertUser(@RequestBody SystemUser user) {
        try {
            systemUserService.insertUser(user);

            return new ResponseBean("插入成功!", CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseBean("插入失败!", CodeConfig.OPERATION_FAILED);
        }
    }

    @PostMapping("/updateUser")
    public ResponseBean updateUser(@RequestBody SystemUser user) {
        try {
            systemUserService.updateUser(user);

            return new ResponseBean("修改成功!", CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseBean("修改失败!", CodeConfig.OPERATION_FAILED);
        }
    }

    @GetMapping("/getUserMenuList/{userName}")
    public ResponseBean getUserMenuList(@PathVariable("userName") String userName) {
        try {
            List<SystemMenu> menuList = systemUserService.getUserMenuList(userName);

            return new ResponseBean("获取菜单成功", menuList, CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseBean("获取菜单失败!", CodeConfig.OPERATION_FAILED);
        }
    }

    @GetMapping("/selectRoleList/{del}")
    public ResponseBean selectRoleList(@PathVariable("del") String del) {
        try {
            List<SystemRole> roleList = systemUserService.selectSystemRoleList(del);

            return new ResponseBean("获取角色成功", roleList, CodeConfig.OPERATION_SUCCESS);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseBean("获取角色失败!", CodeConfig.OPERATION_FAILED);
        }
    }

    @PostMapping("/hello")
    public void hello() {
        System.out.println(1);
    }
}
