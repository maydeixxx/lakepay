package com.LakePayProj.notificationService.services;

import com.LakePayProj.notificationService.exceptions.UserExistsInHash;
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
        if (userRedisRepo.findByChatId(userRedis.getChatId()).isEmpty()) {
            userRedisRepo.save(userRedis);
        } else {
            throw new UserExistsInHash(String.format("user(%s) already exists in hash", userRedis.getChatId()));
        }
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

    @Cacheable(value = "users", key = "#id")
    public UserRedis findUserById(String id) {
        return userRedisRepo.findUserRedisById(id).orElseThrow(
                () -> new NotFoundException(String.format("user by id {%s} not found", id))
        );
    }
}
