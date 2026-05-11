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
        // 1. 如果用户未直接给出情绪，通过LLM大模型根据用户画像进行匹配
        String mood = request.getMood();
        if (mood == null || mood.isEmpty()) {
            List<UserPlaceMemory> userPlaceMemory = userPlaceMemoryRepository.findByUserId(userId);
            mood = recommendAIService.getMood(request.getUserInput(), userPlaceMemory, USER_MOOD_KEY_PREFIX + userId);
        }
        
        // 2. 调用LLM的getRecommend方法，让LLM自动调用Tool获取推荐结果
        RecommendResponse response = recommendAIService.getRecommend(
                "用户情绪: " + mood + 
                ", 用户输入: " + request.getUserInput() +
                ", 用户位置: 纬度" + request.getUserLat() + ", 经度" + request.getUserLng(),
                USER_MEMORY_KEY_PREFIX + userId
        );
        
        return response;
    }
}
