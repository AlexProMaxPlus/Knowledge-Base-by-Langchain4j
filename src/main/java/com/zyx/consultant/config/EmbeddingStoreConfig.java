package com.zyx.consultant.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建 Qdrant 向量存储 Bean。
 */
@Configuration
public class EmbeddingStoreConfig {

    /**
     * 创建连接本地 Docker Qdrant 的 EmbeddingStore。
     *
     * <p>这个方法返回的 Bean 会被 KnowledgeIndexingService 自动注入。
     * QdrantEmbeddingStore 内部负责把 Embedding、文本片段和元数据发送给 Qdrant。</p>
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            // 从 application.yaml 读取 Qdrant 主机名，默认是 localhost。
            @Value("${app.qdrant.host}") String host,
            // 从 application.yaml 读取 gRPC 端口，默认是 6334。
            @Value("${app.qdrant.port}") int port,
            // 从 application.yaml 读取向量集合名称。
            @Value("${app.qdrant.collection-name}") String collectionName) {
        // builder() 创建 Qdrant 存储对象的构建器。
        return QdrantEmbeddingStore.builder()
                // 指定 Qdrant 运行在哪台主机。
                .host(host)
                // 指定 Qdrant gRPC 服务端口。
                .port(port)
                // 指定向量写入哪个 collection。
                .collectionName(collectionName)
                // 指定文本片段在 Qdrant payload 中保存的字段名。
                .payloadTextKey("text")
                // build() 真正创建 EmbeddingStore 对象并交给 Spring 管理。
                .build();
    }
}
