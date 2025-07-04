package com.LakePayProj.paymentService.repos;

import com.LakePayProj.paymentService.models.redis.UserRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepoRedis extends CrudRepository<UserRedis, Long> {



}
