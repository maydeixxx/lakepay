package com.LakePayProj.userService.service;

import com.LakePayProj.userService.auth.LakepayUserDetails;
import com.LakePayProj.userService.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LakepayUserDetailsService implements UserDetailsService {
    private final IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(LakepayUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user with username: " + username));
    }
}
