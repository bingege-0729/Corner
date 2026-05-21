package com.example.corner.service.impl;

import com.example.corner.common.Result;
import com.example.corner.dto.LoginRequest;
import com.example.corner.entity.EmotionTagDict;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.PlaceTagRelation;
import com.example.corner.entity.UserInfo;
import com.example.corner.entity.UserMoodRecord;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.PlaceTagRelationRepository;
import com.example.corner.repository.UserInfoRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.UserService;
import com.example.corner.util.JwtUtil;
import com.example.corner.vo.AvatarResponse;
import com.example.corner.vo.LoginResponse;
import com.example.corner.vo.UserStatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;
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
    private PlaceTagRelationRepository placeTagRelationRepository;

    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
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
    public LoginResponse login(LoginRequest request) { // 简单密码校验

        UserInfo user = userInfoRepository.findByPhone(request.getPhone()).orElse(null);
        if (user == null) {
            user = new UserInfo();
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setNickname("用户" + request.getPhone().substring(7));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userInfoRepository.save(user);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误，请重试");
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
     * 
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
        String extension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        File dest = new File(UPLOAD_DIR + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        UserInfo user = userInfoRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatarUrl("/uploads/avatars/" + fileName);
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

        // 1. 统计去过的地点数量（包含 VISITED 和 已去过的 BOOKMARKED）
        List<UserPlaceMemory> memories = userPlaceMemoryRepository.findByUserId(userId);
        long visitedPlacesCount = memories.stream()
                .filter(m -> "VISITED".equals(m.getInteractionType())
                        || ("BOOKMARKED".equals(m.getInteractionType()) && m.getVisitedAt() != null))
                .map(UserPlaceMemory::getPlaceId).distinct().count();
        stats.setVisitedPlacesCount(visitedPlacesCount);

        // 2. 统计各情绪标签使用次数 (动态统计，仅基于去过的地点)
        Map<String, Integer> moodStats = new HashMap<>();

        // 统计去过地点的心情标签
        List<UserPlaceMemory> visitedMemories = memories.stream()
                .filter(m -> "VISITED".equals(m.getInteractionType()) || m.getVisitedAt() != null).toList();

        for (UserPlaceMemory memory : visitedMemories) {
            // 查询该地点的所有标签
            List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(memory.getPlaceId());

            // 使用 Set 确保对于同一个地点，每种分类只计 1 次分
            Set<String> uniqueCategoriesPerVisit = new HashSet<>();

            for (PlaceTagRelation rel : relations) {
                emotionTagDictRepository.findById(rel.getTagId()).ifPresent(tag -> {
                    String category = mapToMainCategory(tag.getTagName());
                    uniqueCategoriesPerVisit.add(category);
                });
            }

            // 将该次游历涉及的分类计入总统计
            for (String category : uniqueCategoriesPerVisit) {
                moodStats.put(category, moodStats.getOrDefault(category, 0) + 1);
            }
        }

        // 如果没有任何统计数据，给一个默认值
        if (moodStats.isEmpty()) {
            moodStats.put("探索中", 0);
        }

        stats.setMoodStats(moodStats);
        return stats;
    }

    /**
     * 将细分标签映射到三大核心心情分类
     */
    private String mapToMainCategory(String tag) {
        if (tag == null)
            return "平静";

        // 1. 好心情系列 (充满活力、积极、治愈)
        if (tag.contains("好心情") || tag.contains("治愈") || tag.contains("开心") || tag.contains("惊喜") || tag.contains("浪漫")
                || tag.contains("阳光") || tag.contains("出片") || tag.contains("精致") || tag.contains("美好")
                || tag.contains("chill") || tag.contains("活力")) {
            return "好心情";
        }

        // 2. 烦闷时系列 (发泄、出口、深沉、共鸣)
        if (tag.contains("烦闷") || tag.contains("孤独") || tag.contains("难过") || tag.contains("想哭") || tag.contains("压抑")
                || tag.contains("忧郁") || tag.contains("避世") || tag.contains("隐世") || tag.contains("深夜")
                || tag.contains("微醺") || tag.contains("宣泄")) {
            return "烦闷时";
        }

        // 3. 平静系列 (稳定、思考、休息、独处)
        if (tag.contains("平静") || tag.contains("安静") || tag.contains("放空") || tag.contains("思考") || tag.contains("读书")
                || tag.contains("发呆") || tag.contains("独处") || tag.contains("放松") || tag.contains("慵懒")
                || tag.contains("惬意") || tag.contains("复古") || tag.contains("书香")) {
            return "平静";
        }

        // 如果实在匹配不上，返回原标签或者归入平静
        return "平静";
    }
}
