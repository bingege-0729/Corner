package com.example.corner.service.impl;

import com.example.corner.dto.LoginRequest;
import com.example.corner.dto.LoginResponse;
import com.example.corner.entity.UserInfo;
import com.example.corner.repository.UserInfoRepository;
import com.example.corner.service.UserService;
import com.example.corner.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 手机号登录（自动注册）
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserInfo user = userInfoRepository.findByPhone(request.getPhone()).orElse(null);
        
        if (user == null) {
            user = new UserInfo();
            user.setPhone(request.getPhone());
            user.setNickname("用户" + request.getPhone().substring(7));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userInfoRepository.save(user);
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setToken(token);
        
        return response;
    }
}
