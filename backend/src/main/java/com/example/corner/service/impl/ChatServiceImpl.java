package com.example.corner.service.impl;

import com.example.corner.dto.RecommendRequest;
import com.example.corner.service.ChatService;
import com.example.corner.service.aiService.RecommendAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.example.corner.common.RedisConstant.CHAT_MEMORY_KEY_PREFIX;

@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private RecommendAIService recommendAIService;
    
    /**
     * AI 对话（流式输出）
     * @param userId 用户ID
     * @param request 请求DTO（使用 userInput 字段作为对话内容）
     * @return 流式响应
     */
    @Override
    public Flux<String> chat(Long userId, RecommendRequest request) {
        // 构建包含位置信息的消息（如果提供了位置）
        String message = request.getUserInput();
        if (request.getUserLat() != null && request.getUserLng() != null) {
            message += String.format("\n[当前位置: 纬度%.6f, 经度%.6f]", 
                    request.getUserLat(), request.getUserLng());
        }
        
        // 调用 AI 服务进行对话
        String memoryId = CHAT_MEMORY_KEY_PREFIX + userId;
        return recommendAIService.chat(message, memoryId);
    }
}
