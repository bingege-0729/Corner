package com.example.corner.service;

import com.example.corner.dto.LoginRequest;
import com.example.corner.vo.LoginResponse;
import com.example.corner.vo.UserStatsResponse;
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
    
    /**
     * 退出登录
     */
    void logout(Long userId);
    
    /**
     * 获取用户统计数据
     */
    UserStatsResponse getUserStats(Long userId);
}
