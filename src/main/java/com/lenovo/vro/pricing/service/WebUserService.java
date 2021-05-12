package com.lenovo.vro.pricing.service;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.SystemRole;
import com.lenovo.vro.pricing.entity.SystemUser;
import com.lenovo.vro.pricing.entity.ext.SystemUserExt;
import com.lenovo.vro.pricing.mapper.SystemRoleMapper;
import com.lenovo.vro.pricing.mapper.ext.SystemUserMapperExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class WebUserService implements UserDetailsService {

    @Autowired
    private SystemUserMapperExt systemUserMapperExt;

    @Autowired
    private SystemRoleMapper systemRoleMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        SystemUserExt user = getUserInfo(s);

        return User.builder().username(user.getUserName()).password(user.getPassword()).authorities(user.getAuthorities()).build();
    }

    public UserDetails getUserLoginInfo(String username, String token) {
        String salt = stringRedisTemplate.opsForValue().get(token);
        if(StringUtils.isEmpty(salt)) {
            throw new BadCredentialsException("User salt is empty");
        }

        UserDetails user = loadUserByUsername(username);

        return User.builder().username(user.getUsername()).password(salt).authorities(user.getAuthorities()).build();
    }

    public Map<String, String> saveUserInfo(UserDetails user, WebAuthenticationDetails webAuthenticationDetails) {
        String salt = generateUserSalt();

        Algorithm algorithm = Algorithm.HMAC256(salt);
        Date date = new Date(System.currentTimeMillis() + 3600 * 1000);

        String token = JWT.create().withSubject(user.getUsername()).withExpiresAt(date).withIssuedAt(new Date()).sign(algorithm);
        stringRedisTemplate.opsForValue().set(token, salt, 3600, TimeUnit.SECONDS);

        SystemUserExt systemUserExt = getUserInfo(user.getUsername());
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("region", systemUserExt.getCountry());
        map.put("id", systemUserExt.getId().toString());
        // 活跃人数
        if(webAuthenticationDetails != null) {
            updateUser(user.getUsername(), getTime(), webAuthenticationDetails);
        }
        
        return map;
    }

    private SystemUserExt getUserInfo(String userName) {
        SystemUser systemUser = new SystemUser();
        systemUser.setUserName(userName);
        systemUser.setDel(CodeConfig.DELETE_VALID);
        SystemUserExt user = systemUserMapperExt.selectSystemUser(systemUser);
        if(user == null) {
            throw new UsernameNotFoundException("username is not exist.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Integer roleId = user.getRole();
        if(roleId != null) {
            SystemRole role = systemRoleMapper.selectByPrimaryKey(roleId);
            authorities.add(new SimpleGrantedAuthority(CodeConfig.ROLE_PREFIX + role.getRoleName()));
        }

        user.setAuthorities(authorities);

        return user;
    }


    public String generateUserSalt() {
        return BCrypt.gensalt();
    }

    public void deleteUserInfo() {}

    private void updateUser(String userName, String time, WebAuthenticationDetails webAuthenticationDetails) {
        if(StringUtils.isEmpty(time)) {
            time = getTime();
        }

        String ip = webAuthenticationDetails.getRemoteAddress();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("ip", ip);

        stringRedisTemplate.opsForSet().add(CodeConfig.REDIS_PREFIX + CodeConfig.ACTIVE + time, jsonObject.toString());
    }

    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date());
    }
}
