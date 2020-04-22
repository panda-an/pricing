package com.lenovo.vro.pricing.entity.ext;

import com.lenovo.vro.pricing.entity.SystemUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class SystemUserExt extends SystemUser {

    private List<SimpleGrantedAuthority> authorities;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<SimpleGrantedAuthority> authorities) {
        this.authorities = authorities;
    }
}
