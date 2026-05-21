package com.example.corner.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import static com.example.corner.common.RedisConstant.CHAT_MEMORY_KEY_PREFIX;

@Component
public class RedisChatMemoryRepository implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;

    public RedisChatMemoryRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取会话消息
     * 
     * @param memoryId
     *            会话ID
     * @return 会话消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;
        // 从Redis获取信息
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        // 反序列化并返回
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    /**
     * 更新会话消息
     * 
     * @param memoryId
     *            会话ID
     * @param list
     *            会话消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;
        // 序列化信息
        String json = ChatMessageSerializer.messagesToJson(list);
        // 存入缓存中，时间限制为1天
        redisTemplate.opsForValue().set(key, json, Duration.ofDays(1));
    }
    /**
     * 删除会话消息
     * 
     * @param memoryId
     *            会话ID
     */
    @Override
    public void deleteMessages(Object memoryId) {
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;
        redisTemplate.delete(key);
    }
}
