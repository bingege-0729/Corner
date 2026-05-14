package com.example.corner.service.impl;

import com.example.corner.common.Result;
import com.example.corner.dto.LoginRequest;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.UserInfo;
import com.example.corner.entity.UserMoodRecord;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.UserInfoRepository;
import com.example.corner.repository.UserMoodRecordRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.UserService;
import com.example.corner.util.JwtUtil;
import com.example.corner.vo.AvatarResponse;
import com.example.corner.vo.LoginResponse;
import com.example.corner.vo.UserStatsResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private UserMoodRecordRepository userMoodRecordRepository;

    /**
     * 文件上传目录
     */
    private static final String UPLOAD_DIR;
    
    static {
        // 获取用户目录，确保跨平台兼容
        String userDir = System.getProperty("user.dir");
        UPLOAD_DIR = userDir + File.separator + "uploads" + File.separator + "avatars" + File.separator;
    }

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

    /**
     * 头像上传
     * @param userId
     * @param file
     * @return
     */
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        File dest = new File(UPLOAD_DIR + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        UserInfo user = userInfoRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatarUrl("/api/user/avatar/" + fileName);
        user.setUpdatedAt(LocalDateTime.now());
        userInfoRepository.save(user);

        return user.getAvatarUrl();
    }
    
    /**
     * 退出登录
     */
    @Override
    public void logout(Long userId) {
        // JWT 是无状态的，客户端只需删除 token 即可
        // 这里可以做一些清理工作，比如清除 Redis 中的会话数据（如果有）
        // 目前简单返回，前端负责删除 token
    }
    
    /**
     * 获取用户统计数据
     */
    @Override
    public UserStatsResponse getUserStats(Long userId) {
        UserStatsResponse stats = new UserStatsResponse();
        
        // 0. 获取用户信息
        UserInfo user = userInfoRepository.findById(userId).orElse(null);
        if (user != null) {
            stats.setNickname(user.getNickname());
            stats.setAvatarUrl(user.getAvatarUrl());
        }
        
        // 1. 统计去过的不同地点数量（interaction_type = 'VISITED'）
        List<UserPlaceMemory> visitedMemories = userPlaceMemoryRepository
                .findByUserIdAndInteractionType(userId, "VISITED");
        
        // 去重统计不同的地点
        long visitedPlacesCount = visitedMemories.stream()
                .map(UserPlaceMemory::getPlaceId)
                .distinct()
                .count();
        
        stats.setVisitedPlacesCount(visitedPlacesCount);
        
        // 2. 统计各情绪标签使用次数
        List<UserMoodRecord> moodRecords = userMoodRecordRepository.findByUserId(userId);
        
        Map<String, Integer> moodStats = new HashMap<>();
        moodStats.put("好心情", 0);
        moodStats.put("平静", 0);
        moodStats.put("烦闷时", 0);
        
        // 统计每个情绪的出现次数
        for (UserMoodRecord record : moodRecords) {
            String mood = record.getMoodTag();
            if (moodStats.containsKey(mood)) {
                moodStats.put(mood, moodStats.get(mood) + 1);
            }
        }
        
        stats.setMoodStats(moodStats);
        
        return stats;
    }
}
