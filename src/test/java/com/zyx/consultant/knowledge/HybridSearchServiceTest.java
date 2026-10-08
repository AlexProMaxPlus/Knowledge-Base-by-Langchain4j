package com.zyx.consultant.knowledge;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HybridSearchServiceTest {

    /**
     * 验证向量召回和关键词召回命中同一片段时，会合并为一个结果，
     * 并且保留 vector/keyword 两个召回来源。
     */
    @Test
    void fusesVectorAndKeywordResults() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        EmbeddingStore<TextSegment> embeddingStore = mock(EmbeddingStore.class);
        KnowledgeSegmentMapper segmentMapper = mock(KnowledgeSegmentMapper.class);

        TextSegment vectorSegment = TextSegment.from(
                "退款政策要求在订单完成后七天内提交申请。",
                new Metadata()
                        .put("knowledgeDocumentId", "doc-1")
                        .put("tenantId", "tenant-a")
                        .put("documentKey", "refund-policy")
                        .put("version", 2)
                        .put("segmentIndex", 0)
                        .put("sourceUri", "file:///refund.txt"));

        KnowledgeSegment keywordSegment = new KnowledgeSegment();
        keywordSegment.setKnowledgeDocumentId("doc-1");
        keywordSegment.setTenantId("tenant-a");
        keywordSegment.setDocumentKey("refund-policy");
        keywordSegment.setVersion(2);
        keywordSegment.setSegmentIndex(0);
        keywordSegment.setText("退款政策要求在订单完成后七天内提交申请。");
        keywordSegment.setSourceUri("file:///refund.txt");
        keywordSegment.setKeywordScore(0.8);

        when(segmentMapper.findPublishedDocumentIds(
                "tenant-a", "refund-policy", 2))
                .thenReturn(List.of("doc-1"));
        when(embeddingModel.embed("退款期限是多少？"))
                .thenReturn(Response.from(Embedding.from(new float[]{0.1f, 0.2f})));
        when(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .thenReturn(new EmbeddingSearchResult<>(
                        List.of(new EmbeddingMatch<>(
                                0.90,
                                "point-1",
                                Embedding.from(new float[]{0.1f, 0.2f}),
                                vectorSegment))));
        when(segmentMapper.searchPublishedByKeyword(
                "tenant-a", "退款期限是多少？", "refund-policy", 2, 20))
                .thenReturn(List.of(keywordSegment));

        HybridSearchService service = new HybridSearchService(
                embeddingModel,
                embeddingStore,
                segmentMapper);

        List<HybridSearchResult> results = service.search(
                new HybridSearchRequest(
                        "tenant-a",
                        "退款期限是多少？",
                        "refund-policy",
                        2,
                        5,
                        20,
                        0.0));

        assertEquals(1, results.size());
        assertEquals(
                "退款政策要求在订单完成后七天内提交申请。",
                results.get(0).segment().text());
        assertEquals(
                Set.of("vector", "keyword"),
                results.get(0).sources());
        assertTrue(results.get(0).score() > 0);

        // 捕获向量搜索请求，确认 Qdrant 搜索使用了传入的问题和候选数量。
        ArgumentCaptor<EmbeddingSearchRequest> captor =
                ArgumentCaptor.forClass(EmbeddingSearchRequest.class);
        verify(embeddingStore).search(captor.capture());
        assertEquals("退款期限是多少？", captor.getValue().query());
        assertEquals(20, captor.getValue().maxResults());
    }

    /**
     * 验证没有已发布文档时，服务不会调用云端 Embedding 或 Qdrant。
     */
    @Test
    void noPublishedDocumentsReturnsEmptyResult() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        EmbeddingStore<TextSegment> embeddingStore = mock(EmbeddingStore.class);
        KnowledgeSegmentMapper segmentMapper = mock(KnowledgeSegmentMapper.class);

        when(segmentMapper.findPublishedDocumentIds(
                "tenant-a", null, null))
                .thenReturn(List.of());

        List<HybridSearchResult> results = new HybridSearchService(
                embeddingModel,
                embeddingStore,
                segmentMapper).search(
                new HybridSearchRequest(
                        "tenant-a",
                        "退款期限是多少？",
                        null,
                        null,
                        null,
                        null,
                        null));

        assertTrue(results.isEmpty());
    }
}
