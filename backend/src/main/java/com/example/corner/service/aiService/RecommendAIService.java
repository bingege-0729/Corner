package com.example.corner.service.aiService;

import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.vo.RecommendResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

@AiService(chatModel = "openAiChatModel", tools = "recommendAITools", chatMemoryProvider = "redisChatMemoryProvider")
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
    public String getMood(@UserMessage String userInput, @UserMessage List<UserPlaceMemory> memory,
            @MemoryId String memoryId);

    /**
     * 根据用户情绪、输入和位置信息，调用工具获取推荐地点
     * 
     * @param userInput
     *            用户输入（包含情绪、位置等信息）
     * 
     * @param memoryId
     *            用户记忆ID
     * @return 推荐响应（包含LLM理解和匹配的地点列表）
     */
    @SystemMessage("""
        你是地点推荐助手。根据用户情绪和位置推荐地点。
        
        【重要要求】
        - 必须返回至少 3-5 个地点！
        - 如果本地数据库结果不足，必须调用 searchByVector 或 searchWeb 补充
        - 最终 emotionMatches 中必须包含 3-5 个地点
        
        执行流程：
        1. 调用 getSuitablePlaceBymoodAndsave 搜索本地数据库
        2. 如果结果不足3个，调用 searchByVector 补充
        3. 如果仍不足3个，调用 searchWeb 补充（max_results=8，会返回多个结果）
        4. 合并所有结果，去重后返回
        
        输出要求：
        - understanding：一句话总结用户需求（20字以内）
        - emotionMatches：推荐地点列表（必须3-5个）
        - 不要输出思考过程，直接返回结果
        """)
    public RecommendResponse getRecommend(@UserMessage String userInput, @MemoryId String memoryId);

}
