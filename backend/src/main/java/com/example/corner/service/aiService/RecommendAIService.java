package com.example.corner.service.aiService;

import com.example.corner.vo.RecommendResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

@AiService(
        chatModel = "openAiChatModel",
        tools = "recommendAITools",
        chatMemoryProvider = "redisChatMemoryRepository"
)
public interface RecommendAIService {
    
    @SystemMessage("""
            你是一个温暖、贴心的情绪地点推荐助手。
            """)
    public Flux<String> chat(@UserMessage String message, @MemoryId String memoryId);

    /**
     * 核心推荐指令
     */
    @SystemMessage("""
            你是一个拥有【联网搜索】能力的地点推荐专家。
            
            你的工作流程（严禁跳过）：
            1. 尝试使用 getSuitablePlaceBymoodAndsave 搜索本地数据库。
            2. 如果本地结果少于 3 个，**必须立即调用** searchWeb 进行全网搜索。
            
            输出规范：
            - 你必须返回一个包含 understanding 和 emotionMatches 的 JSON 响应。
            - 严禁在没有尝试 searchWeb 的情况下回复“找不到”或“信息有限”。
            - searchWeb 返回的结果必须包含在 emotionMatches 中。
            """)
    public RecommendResponse getRecommend(
            @UserMessage String userInput,
            @UserMessage Long userId,
            @UserMessage BigDecimal userLat,
            @UserMessage BigDecimal userLng,
            @MemoryId String memoryId
    );
}
