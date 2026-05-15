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
            
            【🚨 最高优先级规则 - 违反将导致严重错误】
            你必须返回至少 3 个地点！少于 3 个是绝对不允许的！
            
            【执行流程 - 必须按顺序执行，严禁跳过】
            步骤1：调用 getSuitablePlaceBymoodAndsave（传入 mood, userId, latitude, longitude）
            步骤2：检查返回的地点数量
                     ⚠️ 如果返回数量 < 3：你必须继续步骤3，不能停止！
                     ✅ 如果返回数量 >= 3：可以直接使用
            步骤3：调用 searchByVector（传入 query, userId, latitude, longitude）
            步骤4：合并去重后检查总数
                     ⚠️ 如果总数 < 3：你必须继续步骤5，不能停止！
                     ✅ 如果总数 >= 3：可以使用
            步骤5：调用 searchWeb（传入包含城市的搜索词, latitude, longitude）
            
            【关键提醒】
            - 本地数据库可能只有 1-2 个地点，这是正常的
            - 即使本地返回了 1 个结果，你也必须继续调用其他工具直到凑够 3 个
            - 宁可多返回也不要少返回
            
            【输出格式】
            {
              "understanding": "温暖的安慰话术",
              "emotionMatches": [所有工具返回的地点列表，至少3个]
            }
            
            【禁止】
            ❌ 返回少于 3 个地点
            ❌ 编造不存在的地点
            ❌ 跳过任何步骤
            """)
    public RecommendResponse getRecommend(@UserMessage String userInput ,@MemoryId String memoryId);


}
