package com.LakePayProj.userService.domain.model;

import com.LakePayProj.userService.application.interfaces.repos.IRoleRepository;
import com.LakePayProj.userService.domain.valueObject.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class User implements UserDetails {
    private final IRoleRepository roleRepository;

    private Long id;
    private String username;
    private List<String> subscriptions;
    private Long tgId;
    private Long chatId;
    private String urlPhoto;
    private LocalDate dateOfReg;
    private BigDecimal balance;
    private Collection<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        // Значение возвращаемое Authentication.getName()
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
