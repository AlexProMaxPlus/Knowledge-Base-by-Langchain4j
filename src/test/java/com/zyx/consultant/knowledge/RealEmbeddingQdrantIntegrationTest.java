package com.zyx.consultant.knowledge;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 真实集成测试：调用云端 Embedding API，并把结果写入 Qdrant。
 *
 * <p>只有设置 RUN_REAL_EMBEDDING_TEST=true 时才会执行，
 * 防止普通 mvn test 意外消耗 API 额度或写入真实向量库。</p>
 */
@SpringBootTest(properties = {
        "langchain4j.community.zhipuai.chat-model.api-key=${API_KEY_GLM}",
        "langchain4j.community.zhipuai.embedding-model.api-key=${API_KEY_GLM}"
})
@EnabledIfEnvironmentVariable(
        named = "RUN_REAL_EMBEDDING_TEST",
        matches = "true")
class RealEmbeddingQdrantIntegrationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 验证真实云端 Embedding API 能生成向量，并且 Qdrant 能保存该向量。
     */
    @Test
    void embedsTextSegmentAndStoresItInQdrant() {
        TextSegment segment = TextSegment.from(
                "真实集成测试：企业退款政策要求保留订单和用户信息。");

        // 调用真实云端 Embedding API，把文本片段转换成向量。
        Embedding embedding = embeddingModel.embed(segment).content();

        assertFalse(embedding.toString().isBlank());
        assertFalse(embedding.vectorAsList().isEmpty());

        // 把向量和原始 TextSegment 一起写入 Qdrant，保留后续检索所需的文本。
        String pointId = embeddingStore.add(embedding, segment);

        assertFalse(pointId.isBlank());
        System.out.println("真实 Embedding 已写入 Qdrant，pointId=" + pointId);
    }
}
