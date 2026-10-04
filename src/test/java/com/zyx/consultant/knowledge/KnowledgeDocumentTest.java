package com.zyx.consultant.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KnowledgeDocumentTest {

    /**
     * 验证文档可以按规定顺序从草稿进入处理、发布和归档。
     */
    @Test
    void documentFollowsProcessingLifecycle() {
        KnowledgeDocument document = new KnowledgeDocument(
                "tenant-a",
                "user-1",
                "handbook",
                "员工手册",
                "s3://knowledge/handbook.pdf",
                "hash-1",
                1);

        assertEquals(DocumentStatus.DRAFT, document.getStatus());

        // 草稿文档允许开始处理。
        document.startProcessing();
        assertEquals(DocumentStatus.PROCESSING, document.getStatus());

        // 处理中的文档允许发布。
        document.publish();
        assertEquals(DocumentStatus.PUBLISHED, document.getStatus());

        // 已发布文档允许归档。
        document.archive();
        assertEquals(DocumentStatus.ARCHIVED, document.getStatus());
    }

    /**
     * 验证不能绕过 PROCESSING，直接把草稿发布。
     */
    @Test
    void invalidTransitionIsRejected() {
        KnowledgeDocument document = new KnowledgeDocument(
                "tenant-a",
                "user-1",
                "handbook",
                "员工手册",
                "s3://knowledge/handbook.pdf",
                "hash-1",
                1);

        assertThrows(IllegalStateException.class, document::publish);
    }
}
