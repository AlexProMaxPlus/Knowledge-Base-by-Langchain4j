package com.zyx.consultant.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String KEY_PREFIX = "consultant:chat-memory:";

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;  // 缓存过期时间

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate, @Value("${app.chat-memory.ttl:PT24H}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    /**
     * 从 Redis 获取指定会话的消息。
     * 如果 Redis 中没有数据，返回空列表表示新会话。
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redisTemplate.opsForValue().get(key(memoryId));
        // 避免把 null 继续传给 LangChain4j 的反序列化逻辑。
        return json == null ? List.of() : ChatMessageDeserializer.messagesFromJson(json);
    }

    /**
     * 把会话消息序列化为 JSON 后保存到 Redis，并刷新 TTL。
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        redisTemplate.opsForValue().set(key(memoryId), json, ttl);
    }

    /**
     * 删除指定会话保存的全部消息。
     */
    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(key(memoryId));
    }

    /**
     * 构造 Redis Key。
     * Base64 只用于让 Key 更适合传输和存储，不具备加密效果。
     */
    private String key(Object memoryId) {
        String encodedId = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(String.valueOf(memoryId).getBytes(StandardCharsets.UTF_8));
        return KEY_PREFIX + encodedId;
    }
}
