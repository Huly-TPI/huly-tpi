package com.huly.backend.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsWithIdService extends UserDetailsService {
    UserDetails loadUserById(Long id) throws UsernameNotFoundException;
}
