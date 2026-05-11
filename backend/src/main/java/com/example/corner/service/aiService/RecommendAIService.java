package com.example.corner.service.aiService;

import com.example.corner.dto.RecommendResponse;
import com.example.corner.entity.UserPlaceMemory;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.util.List;

@AiService(
        chatModel = "openAiChatModel",
        tools = "recommendAITools",
        chatMemoryProvider = "redisChatMemoryRepository"
)
public interface RecommendAIService {
    /**
     * 流式输出
     * @param message
     * @return
     */
    //TODO:后续生成匹配的系统提示词
    @SystemMessage("You are a helpful assistant.")
    public Flux<String> chat(@UserMessage String message,@MemoryId String memoryId);

    /**
     * 获取大模型提炼用户输入并且结合用户个人画像进行情绪匹配
     * @param userInput 用户输入
     * @param memory    用户画像
     * @param memoryId  用户心情记录Id
     * @return 用户情绪
     */
    @SystemMessage("""
            你是一个情绪分析助手。请根据用户的输入和历史互动记录，分析并提取用户当前的情绪状态。
            
            你可以使用以下工具来辅助分析：
            - 如果用户输入中包含地点相关信息，可以调用工具查询该地点的情绪标签
            
            分析步骤：
            1. 理解用户的输入内容
            2. 参考用户的历史记忆（去过的地方、喜欢的地方等）
            3. 必要时调用工具获取更多信息
            4. 返回一个最贴切的情绪关键词
            
            输出要求：
            - 只返回一个情绪关键词（如：安静、疲惫、兴奋、放松、治愈、焦虑、孤独、愉快等）
            - 不要包含任何解释或额外文本
            """)
    public String getMood(@UserMessage String userInput, @UserMessage List<UserPlaceMemory> memory, @MemoryId String memoryId);


    /**
     * 根据用户情绪、输入和位置信息，调用工具获取推荐地点
     * @param userInput 用户输入（包含情绪、位置等信息）
     * @param memoryId  用户记忆ID
     * @return 推荐响应（包含LLM理解和匹配的地点列表）
     */
    @SystemMessage("""
            你是一个地点推荐助手。根据用户提供的情绪、位置和偏好，调用合适的工具来推荐地点。
            
            你可以使用以下工具：
            - getSuitablePlaceBymoodAndsave: 根据用户情绪、收藏记录和位置，推荐符合条件的地点
            
            请按照以下步骤操作：
            1. 分析用户输入，提取情绪标签、位置信息
            2. 调用 getSuitablePlaceBymoodAndsave 工具获取推荐地点
            3. 返回包含understanding（你的理解和分析）和emotionMatches（推荐的地点列表）的响应
            
            understanding 字段应该包含：
            - 对用户情绪的理解
            - 推荐策略说明
            - 为什么推荐这些地点
            
            emotionMatches 字段应该包含工具返回的地点列表。
            """)
    public RecommendResponse getRecommend(@UserMessage String userInput, @MemoryId String memoryId);


}
