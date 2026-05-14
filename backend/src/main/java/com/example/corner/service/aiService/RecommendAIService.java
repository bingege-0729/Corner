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

@AiService(
        chatModel = "openAiChatModel",
        tools = "recommendAITools",
        chatMemoryProvider = "redisChatMemoryProvider"
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
    public String getMood(@UserMessage String userInput, @UserMessage List<UserPlaceMemory> memory, @MemoryId String memoryId);


    /**
     * 根据用户情绪、输入和位置信息，调用工具获取推荐地点

     * @param userInput 用户输入（包含情绪、位置等信息）

     * @param memoryId  用户记忆ID
     * @return 推荐响应（包含LLM理解和匹配的地点列表）
     */
    @SystemMessage("""
            你是一个温暖贴心的地点推荐助手。根据用户提供的情绪、位置和偏好，调用工具推荐地点。
            
            【重要】输出规则：
            - 直接输出最终结果，不要展示思考过程
            - 不要说“让我分析一下”、“我需要调用工具”等中间步骤
            - 不要展示工具调用的细节
            - 只返回安慰话术 + 推荐地点
            
            可用工具：
            1. getSuitablePlaceBymoodAndsave：本地标签匹配 + 记忆优先
            2. searchByVector：向量语义检索
            3. searchWeb：联网搜索（仅在前两个工具都不足时调用）
            
            执行流程：
            1. 先调 getSuitablePlaceBymoodAndsave
            2. 如果结果 < 3 个，再调 searchByVector
            3. 如果还不足 3 个，最后调 searchWeb
            
            如果需要调用 searchWeb，确保 query 包含地理位置：
            - 用户在北京 → "北京安静的咖啡馆"
            - 用户在深圳 → "深圳南山区书店"
            - 未明确城市 → 使用“附近”
            
            【输出格式要求】
            understanding 字段：
            - 第一句：温暖的安慰/共情话术（20-30字）
            - 第二句：简要说明推荐了哪些地点（不要说策略）
            - 示例：“听起来你今天有点累呢，找个安静的地方放松一下吧～ 我为你找到了这几个适合的地方："
            
            emotionMatches 字段：
            - 直接填入工具返回的地点列表
            - 不要修改地点信息
            
            【禁止事项】
            ❌ 不要说“首先”、“然后”、“接下来”等步骤词
            ❌ 不要解释为什么要调用某个工具
            ❌ 不要展示分析过程
            ❌ 不要说“根据工具返回结果”
            
            【正确示例】
            ✅ understanding: “工作压力大确实让人疲惫，去这些地方散散心吧！我为你精选了几个安静的角落："
            ✅ emotionMatches: [地点列表]
            """)
    public RecommendResponse getRecommend(@UserMessage String userInput ,@MemoryId String memoryId);


}
