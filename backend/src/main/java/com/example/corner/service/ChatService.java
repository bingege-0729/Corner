package com.example.corner.service;

import com.example.corner.dto.RecommendRequest;
import reactor.core.publisher.Flux;

public interface ChatService {
    
    /**
     * AI 对话（流式输出）
     * @param userId 用户ID
     * @param request 请求DTO（使用 userInput 字段作为对话内容）
     * @return 流式响应
     */
    Flux<String> chat(Long userId, RecommendRequest request);
}
