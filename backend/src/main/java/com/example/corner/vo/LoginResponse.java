package com.example.corner.vo;

import lombok.Data;

// 登录响应
@Data
public class LoginResponse {
    private Long userId;
    private String nickname;
    private String token;
}
