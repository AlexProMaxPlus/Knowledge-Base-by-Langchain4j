package com.zyx.consultant.knowledge;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 负责把文本片段转换为云端 Embedding，并写入 Qdrant。
 *
 * <p>本服务是离线索引流程的一部分，不负责在线问答。</p>
 */
@Service
public class KnowledgeIndexingService {

    /**
     * EmbeddingModel 是“文本转向量”的统一接口。
     * 当前实际注入的是智谱云端 Embedding 模型，业务代码不依赖智谱具体实现类。
     */
    private final EmbeddingModel embeddingModel;

    /**
     * EmbeddingStore 是“向量保存与检索”的统一接口。
     * 当前实际注入的是 QdrantEmbeddingStore。
     */
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 构造索引服务，并由 Spring 注入云端 Embedding 模型和 Qdrant 存储。
     */
    public KnowledgeIndexingService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        // 保存文本转向量的能力，后续 index 方法会调用它。
        this.embeddingModel = embeddingModel;
        // 保存向量写入 Qdrant 的能力，后续 index 方法会调用它。
        this.embeddingStore = embeddingStore;
    }

    /**
     * 批量生成向量并保存片段与来源元数据。
     *
     * @param segments 第六课生成的文本片段
     * @return 写入 Qdrant 后生成的向量记录 ID
     */
    public List<String> index(List<TextSegment> segments) {
        // 没有片段就没有必要调用云端 API，也没有必要创建空索引。
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        // 批量调用 EmbeddingModel：每个 TextSegment 获得一个 Embedding 向量。
        List<Embedding> embeddings =
                embeddingModel.embedAll(segments).content();

        // 将向量和原始片段按相同下标一起写入 Qdrant。
        // 这样 Qdrant 命中向量后，还能返回原文和来源元数据。
        return embeddingStore.addAll(
                embeddings,
                segments);
    }
}
