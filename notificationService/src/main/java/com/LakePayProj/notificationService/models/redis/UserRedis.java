package com.LakePayProj.notificationService.models.redis;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.Map;

@RedisHash
@Data
@Builder
public class UserRedis {

    private String id;
    private String adCategory;
    private String chatId;
    private String tgId;
    private List<String> expectedCategories;
    private Map<String, String> categoryMessages;

}
