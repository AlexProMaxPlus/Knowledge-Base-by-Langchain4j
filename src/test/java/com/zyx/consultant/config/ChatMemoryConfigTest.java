package com.zyx.consultant.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;

class ChatMemoryConfigTest {

    /**
     * 验证会话 ID 能正确进入 ChatMemory 对象，
     * 并且不同会话不会复用同一个窗口对象。
     */
    @Test
    void providerCreatesMemoryObjectsWithTheRequestedIds() {
        ChatMemoryStore store = mock(ChatMemoryStore.class);
        ChatMemoryProvider provider = new ChatMemoryConfig().chatMemoryProvider(store);

        ChatMemory first = provider.get("session-1");
        ChatMemory same = provider.get("session-1");
        ChatMemory second = provider.get("session-2");

        // 当前 Provider 每次返回窗口对象，但对象内部使用同一个持久化 Store。
        assertNotSame(first, same);
        assertNotSame(first, second);
        // 相同会话 ID 必须保持一致，Store 才能读写同一份消息。
        assertEquals("session-1", first.id());
        assertEquals("session-1", same.id());
        assertEquals("session-2", second.id());
    }
}
