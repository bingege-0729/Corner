package com.example.corner.service;

import com.example.corner.dto.LoginRequest;
import com.example.corner.dto.LoginResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    
    /**
     * 手机号登录（自动注册）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 上传用户头像
     */
    String uploadAvatar(Long userId, MultipartFile file);


}
