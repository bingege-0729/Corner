package com.example.corner.service.impl;

import com.example.corner.dto.RecommendRequest;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.repository.UserMoodRecordRepository;
import com.example.corner.service.RecommendService;
import com.example.corner.service.aiService.RecommendAIService;
import com.example.corner.vo.RecommendResponse;
import com.example.corner.entity.UserMoodRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.example.corner.common.RedisConstant.USER_MEMORY_KEY_PREFIX;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;

    @Autowired
    private RecommendAIService recommendAIService;

    @Autowired
    private UserMoodRecordRepository userMoodRecordRepository;

    /**
     * 核心推荐
     *
     * @param userId  用户ID
     * @param request 请求DTO
     * @return 响应VO
     */
    @Override
    @Transactional
    public RecommendResponse recommend(Long userId, RecommendRequest request) {
        // 1. 记录用户当前的心情标签（映射到 好心情、平静、烦闷时）
        recordUserMood(userId, request);

        // 2. 调用 AI 推荐
        String userMessage = String.format(
                "userId=%d, 用户输入=%s, 纬度=%s, 经度=%s, 用户心情=%s",
                userId,
                request.getUserInput() != null ? request.getUserInput() : "",
                request.getUserLat(),
                request.getUserLng(),
                request.getMood() != null ? request.getMood() : ""
        );

        return recommendAIService.getRecommend(userMessage, USER_MEMORY_KEY_PREFIX + userId);
    }

    private void recordUserMood(Long userId, RecommendRequest request) {
        if (request.getMood() == null || request.getMood().isEmpty()) return;

        String[] tags = request.getMood().split(",");
        Set<String> categories = new HashSet<>();

        for (String tag : tags) {
            tag = tag.trim();
            // 映射逻辑
            if (tag.contains("烦闷") || tag.contains("枯竭")) {
                categories.add("烦闷时");
            } else if (tag.contains("安静")) {
                categories.add("平静");
            } else if (tag.contains("想被治愈") || tag.contains("烟火气")) {
                categories.add("好心情");
            }
        }

        // 保存心情记录
        for (String category : categories) {
            UserMoodRecord record = new UserMoodRecord();
            record.setUserId(userId);
            record.setMoodTag(category);
            record.setEnergyLevel(request.getEnergyLevel());
            record.setSocialLevel(request.getSocialLevel());
            record.setCreatedAt(LocalDateTime.now());
            userMoodRecordRepository.save(record);
        }
    }
}
