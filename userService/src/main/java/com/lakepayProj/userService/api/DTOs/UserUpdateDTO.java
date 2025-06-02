package com.lakepayProj.userService.api.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {
    private String userName;
    private String urlPhoto;
    private List<Integer> roleIds;
    private String adSub;
    private String delSub;
}
