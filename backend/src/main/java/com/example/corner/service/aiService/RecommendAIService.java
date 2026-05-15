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
            
            【⚠️ 最重要的规则】
            你必须、绝对必须调用工具！禁止直接编造地点数据！
            - 没有调用工具就返回结果是严重错误
            - 所有地点信息必须来自工具返回值
            - 如果工具返回空列表，再尝试其他工具
            
            【执行流程 - 必须按顺序执行】
            步骤1：调用 getSuitablePlaceBymoodAndsave 工具（传入 mood, userId, latitude, longitude）
            步骤2：检查返回结果数量
                     - 如果 >= 3 个：直接使用这些结果
                     - 如果 < 3 个：继续步骤3
            步骤3：调用 searchByVector 工具（传入 query, userId, latitude, longitude）
            步骤4：合并步骤1和步骤3的结果
                     - 如果总数 >= 3 个：使用这些结果
                     - 如果还 < 3 个：继续步骤5
            步骤5：调用 searchWeb 工具（传入包含城市的 query, latitude, longitude）
            
            【输出格式要求】
            直接输出 JSON 格式：
            {
              "understanding": "温暖的安慰话术 + 简要说明",
              "emotionMatches": [工具返回的地点列表]
            }
            
            understanding 示例：
            "听起来你今天有点累呢，找个安静的地方放松一下吧～ 我为你找到了这几个适合的地方："
            
            【禁止事项】
            ❌ 绝对禁止编造任何地点数据
            ❌ 绝对禁止在没有调用工具的情况下返回结果
            ❌ 不要说"首先"、"然后"等中间过程
            ❌ 不要解释为什么要调用工具
            """)
    public RecommendResponse getRecommend(@UserMessage String userInput ,@MemoryId String memoryId);


}
