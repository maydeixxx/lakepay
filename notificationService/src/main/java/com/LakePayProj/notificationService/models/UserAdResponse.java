package com.LakePayProj.notificationService.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserAdResponse {
    private List<String> expectedCategories = new ArrayList<>();
    private Map<String, String> categoryMessages = new ConcurrentHashMap<>();
    private long timestamp = System.currentTimeMillis();
}
