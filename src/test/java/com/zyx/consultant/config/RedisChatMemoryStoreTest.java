package com.zyx.consultant.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisChatMemoryStoreTest {

    @SuppressWarnings("unchecked")
    /**
     * 验证消息可以序列化写入 Redis，并按 TTL 规则读取回来。
     */
    @Test
    void storesSerializedMessagesWithTtlAndReadsThemBack() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);

        RedisChatMemoryStore store = new RedisChatMemoryStore(redisTemplate, Duration.ofHours(24));
        List<ChatMessage> messages = List.of(UserMessage.from("hello"));

        // 写入一条用户消息，验证 Store 会调用 Redis 的 value 操作。
        store.updateMessages("session-1", messages);
        when(values.get(anyString())).thenReturn(
                dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson(messages));

        // 模拟 Redis 返回刚才保存的 JSON，验证能还原为原始消息对象。
        assertEquals(messages, store.getMessages("session-1"));
        // 验证保存时携带了 24 小时过期时间。
        verify(values).set(
                org.mockito.ArgumentMatchers.startsWith("consultant:chat-memory:"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(Duration.ofHours(24)));
    }
}
