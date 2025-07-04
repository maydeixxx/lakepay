package com.LakePayProj.notificationService.repos;

import com.LakePayProj.notificationService.models.redis.UserRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRedisRepo extends CrudRepository<UserRedis, String> {

    Optional<List<UserRedis>> findAllByAdCategory(String adCategory);
    Optional<UserRedis> findByTgId(String tgId);

}
