package com.zyx.consultant.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class KnowledgeDocumentMapperTest {

    @Autowired
    private KnowledgeDocumentMapper mapper;

    /**
     * 验证 MyBatis-Plus 基础 INSERT、XML 查询和租户版本排序。
     */
    @Test
    void insertsAndQueriesVersionedDocuments() {
        KnowledgeDocument first = document("handbook", 1, "v1");
        KnowledgeDocument second = document("handbook", 2, "v2");

        // 插入两个版本，验证同一租户可以保留不同版本。
        mapper.insert(first);
        mapper.insert(second);

        // ASSIGN_UUID 策略应在插入前生成主键。
        assertNotNull(first.getId());
        assertTrue(mapper.existsByTenantIdAndDocumentKeyAndVersion(
                "tenant-a", "handbook", 2));

        List<KnowledgeDocument> documents =
                mapper.findByTenantIdOrderByUpdatedAtDesc("tenant-a");
        assertEquals(2, documents.size());
        // XML 按更新时间、版本倒序，所以版本 2 应排在前面。
        assertEquals("v2", documents.get(0).getTitle());
        assertEquals(DocumentStatus.DRAFT, documents.get(0).getStatus());
    }

    /**
     * 验证失败重试时，MyBatis-Plus 能把旧 error_message 更新为 NULL。
     */
    @Test
    void updateByIdCanClearPreviousErrorMessage() {
        KnowledgeDocument document = document("handbook", 1, "v1");
        // 先模拟一次处理失败，制造旧错误信息。
        document.startProcessing();
        document.fail("temporary failure");
        mapper.insert(document);

        // 再次开始处理，业务对象会清空错误信息。
        document.startProcessing();
        mapper.updateById(document);

        assertEquals("PROCESSING", mapper.selectById(document.getId()).getStatus().name());
        assertNull(mapper.selectById(document.getId()).getErrorMessage());
    }

    /**
     * 统一创建 Mapper 测试所需的文档对象。
     */
    private KnowledgeDocument document(String key, int version, String title) {
        KnowledgeDocument document = new KnowledgeDocument(
                "tenant-a",
                "user-1",
                key,
                title,
                "s3://knowledge/" + key + "-v" + version + ".pdf",
                "hash-v" + version,
                version);
        document.initializeTimestamps(Instant.now());
        return document;
    }
}
