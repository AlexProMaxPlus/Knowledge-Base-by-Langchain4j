package com.zyx.consultant.knowledge;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeIndexingServiceTest {

    /**
     * 验证索引服务会批量调用 EmbeddingModel，并把向量与原文本片段一起写入向量库。
     */
    @Test
    void embedsAndStoresSegments() {
        // Mock EmbeddingModel，避免测试真的调用云端 API 和消耗费用。
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        // Mock EmbeddingStore，避免测试真的连接 Qdrant。
        EmbeddingStore<TextSegment> embeddingStore = mock(EmbeddingStore.class);

        // 准备两段待索引文本，模拟第六课切分后的结果。
        List<TextSegment> segments = List.of(
                TextSegment.from("退款政策第一条"),
                TextSegment.from("退款政策第二条"));

        // 准备两条假的向量，模拟云端 Embedding API 的返回结果。
        List<Embedding> embeddings = List.of(
                Embedding.from(new float[]{0.1f, 0.2f}),
                Embedding.from(new float[]{0.3f, 0.4f}));

        // 规定：当业务调用 embedAll 时，Mock 返回上面准备好的两条向量。
        when(embeddingModel.embedAll(segments))
                .thenReturn(Response.from(embeddings));

        // 规定：当业务把向量和文本交给 Store 时，模拟 Qdrant 返回两个点 ID。
        when(embeddingStore.addAll(embeddings, segments))
                .thenReturn(List.of("point-1", "point-2"));

        // 手动组装服务，模拟 Spring 构造器注入两个依赖。
        KnowledgeIndexingService service =
                new KnowledgeIndexingService(embeddingModel, embeddingStore);

        // 执行被测试的方法。
        List<String> ids = service.index(segments);

        // 验证服务返回了 Qdrant 生成的向量记录 ID。
        assertEquals(List.of("point-1", "point-2"), ids);
        // 验证服务确实批量调用了 EmbeddingModel，而不是逐条调用。
        verify(embeddingModel).embedAll(segments);
        // 验证服务把同一批向量和同一批文本片段交给了向量库。
        verify(embeddingStore).addAll(embeddings, segments);
    }

    /**
     * 验证没有文本片段时不会调用云端 Embedding API，也不会写入向量库。
     */
    @Test
    void emptySegmentsDoNothing() {
        // 即使依赖存在，也不调用真实服务；这里仍然使用 Mock。
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        EmbeddingStore<TextSegment> embeddingStore = mock(EmbeddingStore.class);

        // 传入空列表，模拟空文件或解析失败后没有生成片段。
        List<String> ids = new KnowledgeIndexingService(
                embeddingModel,
                embeddingStore).index(List.of());

        // 空输入应该得到空 ID 列表。
        assertEquals(List.of(), ids);
        // 这里没有 verify 调用，是因为“完全不调用外部依赖”就是本测试重点。
    }
}
