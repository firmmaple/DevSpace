package org.jeffrey.service.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jeffrey.api.dto.user.UserDTO;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    //    private final UserInfo user;
    private final Long userId;
    private final String username;
    private final String password;
    private final boolean isAdmin;


    public CustomUserDetails(UserDO userDO) {
        this.userId = userDO.getId();
        this.username = userDO.getUsername();
        this.password = userDO.getPassword();
        this.isAdmin = userDO.getIsAdmin();
    }

    public UserDTO toUserDTO(){
       return new UserDTO(userId, username, password, isAdmin);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
//        return user.getEnabled();
        return true;
    }

}
