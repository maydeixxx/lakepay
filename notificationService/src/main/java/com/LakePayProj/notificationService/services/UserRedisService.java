package com.LakePayProj.notificationService.services;

import com.LakePayProj.notificationService.models.redis.UserRedis;
import com.LakePayProj.notificationService.repos.UserRedisRepo;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRedisService {

    private final UserRedisRepo userRedisRepo;

    public void saveUser(UserRedis userRedis) {
        userRedisRepo.save(userRedis);
    }

    @Cacheable(value = "users", key = "#category")
    public List<UserRedis> findUsersByCategory(String category) {
        return userRedisRepo.findAllByAdCategory(category).orElseThrow(
                () -> new NotFoundException(String.format("users by category {%s} not found", category))
        );
    }

    @Cacheable(value = "users", key = "#tgId")
    public UserRedis findUserByTgId(String tgId) {
        return userRedisRepo.findByTgId(tgId).orElseThrow(
                () -> new NotFoundException(String.format("user by tgId {%s} not found", tgId))
        );
    }

}
