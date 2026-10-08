package com.zyx.consultant.knowledge;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.filter.logical.And;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 企业知识库的混合召回服务。
 *
 * <p>它同时执行：
 * <ol>
 *     <li>Qdrant 语义向量召回；</li>
 *     <li>MySQL FULLTEXT 关键词召回；</li>
 *     <li>租户、发布状态和版本过滤；</li>
 *     <li>RRF 倒数排名融合和规则重排。</li>
 * </ol>
 */
@Service
public class HybridSearchService {

    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final int DEFAULT_CANDIDATE_LIMIT = 20;
    private static final double DEFAULT_MIN_VECTOR_SCORE = 0.0;
    private static final int RRF_K = 60;

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final KnowledgeSegmentMapper segmentMapper;

    public HybridSearchService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            KnowledgeSegmentMapper segmentMapper) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.segmentMapper = segmentMapper;
    }

    /**
     * 执行两路召回并返回重排后的片段。
     */
    public List<HybridSearchResult> search(HybridSearchRequest request) {
        int candidateLimit = defaultIfNull(
                request.candidateLimit(), DEFAULT_CANDIDATE_LIMIT);
        int maxResults = defaultIfNull(
                request.maxResults(), DEFAULT_MAX_RESULTS);
        double minVectorScore = defaultIfNull(
                request.minVectorScore(), DEFAULT_MIN_VECTOR_SCORE);

        // 先查 MySQL 中已发布文档 ID，防止 Qdrant 返回未发布版本。
        Set<String> publishedDocumentIds = new HashSet<>(
                segmentMapper.findPublishedDocumentIds(
                        request.tenantId(),
                        request.documentKey(),
                        request.version()));
        if (publishedDocumentIds.isEmpty()) {
            return List.of();
        }

        Embedding queryEmbedding =
                embeddingModel.embed(request.question()).content();
        Filter vectorFilter = buildVectorFilter(request);
        EmbeddingSearchRequest vectorRequest =
                EmbeddingSearchRequest.builder()
                        .query(request.question())
                        .queryEmbedding(queryEmbedding)
                        .maxResults(candidateLimit)
                        .minScore(minVectorScore)
                        .filter(vectorFilter)
                        .build();

        EmbeddingSearchResult<TextSegment> vectorResult =
                embeddingStore.search(vectorRequest);
        List<Content> vectorContents = new ArrayList<>();
        Map<String, Set<String>> sourcesByKey = new HashMap<>();

        // 只保留 MySQL 确认属于已发布文档的向量候选。
        int vectorRank = 0;
        for (EmbeddingMatch<TextSegment> match : vectorResult.matches()) {
            String documentId = match.embedded()
                    .metadata()
                    .getString("knowledgeDocumentId");
            if (!publishedDocumentIds.contains(documentId)) {
                continue;
            }
            String key = key(match.embedded());
            vectorContents.add(Content.from(match.embedded()));
            sourcesByKey.computeIfAbsent(key, ignored -> new HashSet<>())
                    .add("vector");
        }

        List<KnowledgeSegment> keywordMatches =
                segmentMapper.searchPublishedByKeyword(
                        request.tenantId(),
                        request.question(),
                        request.documentKey(),
                        request.version(),
                        candidateLimit);
        List<Content> keywordContents = new ArrayList<>();
        for (KnowledgeSegment match : keywordMatches) {
            TextSegment segment = match.toTextSegment();
            String key = key(segment);
            keywordContents.add(Content.from(segment));
            sourcesByKey.computeIfAbsent(key, ignored -> new HashSet<>())
                    .add("keyword");
        }

        Query query = Query.from(request.question());
        Map<Query, Collection<List<Content>>> candidateLists = Map.of(
                query,
                List.of(vectorContents, keywordContents));

        // 官方 ReRankingContentAggregator 内部先用 RRF 融合多路召回，再执行重排。
        ScoringModel scoringModel = new KeywordOverlapScoringModel();
        List<Content> reranked = ReRankingContentAggregator.builder()
                .scoringModel(scoringModel)
                .minScore(0.0)
                .maxResults(maxResults)
                .build()
                .aggregate(candidateLists);

        List<HybridSearchResult> results = new ArrayList<>();
        for (Content content : reranked) {
            String contentKey = key(content.textSegment());
            results.add(new HybridSearchResult(
                    content.metadata().getOrDefault(
                            dev.langchain4j.rag.content.ContentMetadata.RERANKED_SCORE,
                            0.0) instanceof Number number
                            ? number.doubleValue()
                            : 0.0,
                    content.textSegment(),
                    sourcesByKey.getOrDefault(contentKey, Set.of())));
        }
        return results;
    }

    /**
     * 构建 Qdrant 侧的租户、文档和版本过滤。
     */
    private Filter buildVectorFilter(HybridSearchRequest request) {
        Filter filter = new And(
                MetadataFilterBuilder.metadataKey("tenantId")
                        .isEqualTo(request.tenantId()),
                MetadataFilterBuilder.metadataKey("status")
                        .isEqualTo("PUBLISHED"));
        if (request.documentKey() != null) {
            filter = new And(filter, MetadataFilterBuilder
                    .metadataKey("documentKey")
                    .isEqualTo(request.documentKey()));
        }
        if (request.version() != null) {
            filter = new And(filter, MetadataFilterBuilder
                    .metadataKey("version")
                    .isEqualTo(request.version()));
        }
        return filter;
    }

    private String key(TextSegment segment) {
        return key(
                segment.metadata().getString("knowledgeDocumentId"),
                segment.metadata().getInteger("segmentIndex"));
    }

    private String key(String documentId, Integer segmentIndex) {
        return documentId + ":" + segmentIndex;
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
