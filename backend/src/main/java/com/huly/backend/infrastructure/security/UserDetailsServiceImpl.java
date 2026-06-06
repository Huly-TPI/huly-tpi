package com.huly.backend.infrastructure.security;

import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsWithIdService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return toUserDetails(user);
    }

    @Override
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        AppUserEntity user = appUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        return toUserDetails(user);
    }

    private UserDetails toUserDetails(AppUserEntity user) {
        String authority = user.getRole() != null
                ? "ROLE_" + user.getRole().name()
                : "ROLE_USER";

        return new User(
                String.valueOf(user.getId()),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}