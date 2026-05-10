package com.example.corner.service.aiService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService(
        chatModel = "openAiChatModel"
)
public interface RecommendService {
    /**
     * 流式输出
     * @param message
     * @return
     */
    public Flux<String> chat(@UserMessage String message,@MemoryId String memoryId);
}
