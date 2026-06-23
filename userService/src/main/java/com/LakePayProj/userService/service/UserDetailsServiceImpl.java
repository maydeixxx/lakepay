package com.LakePayProj.userService.service;

import com.LakePayProj.userService.exception.UserNotFoundException;
import com.LakePayProj.userService.repository.IUserRepository;
import com.LakePayProj.userService.security.UserDetailsImpl;
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
        /*
        E!аная хрень я конвертирую Long в String в JwtTokenService, чтобы конвертировать String в Long здесь.
        А все потому что пользователь может поменять свой username и я не хочу каждый раз
        делать инвалидацию токенов доступа и выкидывать его из аккаунта.
        **/
        return userRepository.findById(Long.parseLong(username))
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UserNotFoundException("Can't find user with username: " + username));
    }
}
