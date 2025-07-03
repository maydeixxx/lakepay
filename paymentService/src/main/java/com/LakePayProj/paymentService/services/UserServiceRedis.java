package com.LakePayProj.paymentService.services;

import com.LakePayProj.paymentService.exceptions.UserNotFoundException;
import com.LakePayProj.paymentService.models.redis.UserRedis;
import com.LakePayProj.paymentService.repos.UserRepoRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceRedis {

    private final UserRepoRedis userRepoRedis;

    public void saveUser(UserRedis user) {
        userRepoRedis.save(user);
    }

    @Cacheable(value = "users", key = "#userId")
    public UserRedis getById(Long userId) {
        return userRepoRedis.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("user by id %s not found", userId))
        );
    }

}
