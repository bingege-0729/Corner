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
        // 1. 记录用户当前的心情标签
        recordUserMood(userId, request);

        // 2. 调用 AI 推荐（分参数传递，提高工具调用准确率）
        return recommendAIService.getRecommend(
                request.getUserInput() != null ? request.getUserInput() : "",
                userId,
                request.getUserLat(),
                request.getUserLng(),
                USER_MEMORY_KEY_PREFIX + userId
        );
    }

        RecommendResponse response = recommendAIService.getRecommend(userMessage, USER_MEMORY_KEY_PREFIX + userId);

        // 后处理：清理 understanding 字段，确保不包含思考过程
        if (response != null && response.getUnderstanding() != null) {
            String understanding = response.getUnderstanding();

            // 移除常见的思考过程关键词
            understanding = understanding
                .replaceAll("(?m)^让我.*?\\n", "")  // 移除“让我...”开头的行
                .replaceAll("(?m)^首先.*?\\n", "")  // 移除“首先...”开头的行
                .replaceAll("(?m)^我需要.*?\\n", "")  // 移除“我需要...”开头的行
                .replaceAll("根据工具返回.*?[,，]", "")  // 移除“根据工具返回”
                .trim();

            response.setUnderstanding(understanding);
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
