package com.example.corner.service.impl;

import com.example.corner.dto.RecommendRequest;

import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.RecommendService;
import com.example.corner.service.aiService.RecommendAIService;
import com.example.corner.vo.RecommendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.corner.common.RedisConstant.USER_MEMORY_KEY_PREFIX;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;

    @Autowired
    private RecommendAIService recommendAIService;

    /**
     * 核心推荐
     *
     * @param userId  用户ID
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

        return response;
    }
}
