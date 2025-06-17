package com.LakePayProj.userService.service;

import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.security.UserDetailsImpl;
import com.LakePayProj.userService.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));
    }
}
