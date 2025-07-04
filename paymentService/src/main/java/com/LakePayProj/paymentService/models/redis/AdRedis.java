package com.LakePayProj.paymentService.models.redis;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;

@Data
@Builder
@RedisHash(value = "ad_data")
public class AdRedis {

    private Long id;
    private BigDecimal price;
    private String login;
    private String password;

}
