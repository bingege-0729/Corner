package com.example.corner.service;

import com.example.corner.dto.LoginRequest;
import com.example.corner.dto.LoginResponse;

public interface UserService {
    
    /**
     * 手机号登录（自动注册）
     */
    LoginResponse login(LoginRequest request);
}
