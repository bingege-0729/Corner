package com.example.corner.config;

import com.example.corner.repository.RedisChatMemoryRepository;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Autowired
    private OpenAiChatModel model;

    @Autowired
    private RedisChatMemoryRepository redisChatMemoryRepository;

    /**
     * 配置 ChatMemoryProvider Bean，用于 LangChain4j 的会话记忆
     */
    @Bean
    public ChatMemoryProvider redisChatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder().id(memoryId).maxMessages(30)
                .chatMemoryStore(redisChatMemoryRepository).build();
    }

}
