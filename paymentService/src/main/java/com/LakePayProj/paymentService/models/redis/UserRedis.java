package com.LakePayProj.paymentService.models.redis;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@RedisHash(value = "user_data")
public class UserRedis implements Serializable {

    private Long id;
    private Long tgId;
    private BigDecimal balance;

}
