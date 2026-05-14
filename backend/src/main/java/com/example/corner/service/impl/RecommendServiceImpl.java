package com.example.corner.service.impl;

import com.example.corner.dto.RecommendRequest;
import com.example.corner.dto.RecommendResponse;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.RecommendService;
import com.example.corner.service.aiService.RecommendAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.corner.common.RedisConstant.USER_MOOD_KEY_PREFIX;
import static com.example.corner.common.RedisConstant.USER_MEMORY_KEY_PREFIX;

@Service
public class RecommendServiceImpl implements RecommendService {
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;

    @Autowired
    private RecommendAIService recommendAIService;
    
    /**
     * 核心推荐
     * @param userId 用户ID
     * @param request 请求DTO
     * @return 响应VO
     */
    @Override
    public RecommendResponse recommend(Long userId, RecommendRequest request) {
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
}
