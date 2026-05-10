package com.example.corner.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String nickname;
    private String token;
}
