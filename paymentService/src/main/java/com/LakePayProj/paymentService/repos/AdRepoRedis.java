package com.LakePayProj.paymentService.repos;

import com.LakePayProj.paymentService.models.redis.AdRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdRepoRedis extends CrudRepository<AdRedis, Long> {

}
