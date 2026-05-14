package com.example.corner.service.aiService;

import com.example.corner.dto.RecommendResponse;
import com.example.corner.entity.UserPlaceMemory;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

import java.util.List;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        tools = "recommendAITools",
        chatMemoryProvider = "redisChatMemoryProvider"


)
public interface RecommendAIService {
    /**
     * 流式对话 - AI 助手与用户进行自然对话
     * 可以回答用户问题、分析情绪、推荐地点等
     * @param message 用户消息
     * @param memoryId 会话记忆ID
     * @return 流式响应
     */
    @SystemMessage("""
            你是一个温暖、贴心的情绪地点推荐助手，名叫「Corner」。你的使命是帮助用户找到适合当前心情的去处。
            
            你的特点：
            - 善于倾听和理解用户的情绪
            - 语气温柔、有同理心，像朋友一样交流
            - 能够根据用户描述的心情和状态推荐合适的地点
            - 可以调用工具查询地点信息
            
            对话原则：
            1. 首先理解用户的情绪和需求
            2. 如果用户表达了心情，给予共情和回应
            3. 如果用户需要推荐，可以调用 getSuitablePlaceByMoodAndLocation 工具
            4. 如果用户提供位置信息（经纬度），优先推荐附近的地点
            5. 保持对话自然流畅，不要过于机械
            
            注意事项：
            - 当用户只是聊天时，正常对话即可，不需要每次都推荐地点
            - 当用户明确需要推荐时，才调用工具
            - 如果用户提到具体地点，可以查询该地点的详细信息
            - 记住用户的偏好和历史，提供更个性化的建议
            
            输出风格：
            - 简洁但有温度
            - 适当使用表情符号增加亲和力
            - 避免长篇大论，分段清晰
            """)
    public Flux<String> chat(@UserMessage String message, @MemoryId String memoryId);

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
     * @param userId    用户ID
     * @param userInput 用户输入（包含情绪、位置等信息）
     * @param latitude  用户纬度
     * @param longitude 用户经度
     * @param memoryId  用户记忆ID
     * @return 推荐响应（包含LLM理解和匹配的地点列表）
     */
    @SystemMessage("""
            你是一个地点推荐助手。当用户表达了任何想出门、想去某个地方的意图时，立即调用 getSuitablePlaceBymoodAndsave 工具。
            
                规则：
                1. 用户说"想去..."、"找个..."、"推荐..."、"有什么..." → 立即调工具
                2. 用户说心情但没说要出门 → 简短共情，调工具推荐（默认用户想出门）
                3. 只有用户在纯粹闲聊（如问天气、说笑话）时才不调工具
            
                调用工具的参数从用户输入中提取：
                - mood: 用户想要的情绪标签，如"安静"、"治愈"、"热闹"
                - userId: 用户ID
                - latitude: 纬度
                - longitude: 经度
            
                返回的 understanding 要包含共情语。
                    """)
    public RecommendResponse getRecommend(@UserMessage String userMessage, @MemoryId String memoryId);


}
