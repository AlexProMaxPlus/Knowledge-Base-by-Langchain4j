package com.zyx.consultant.knowledge;

import java.time.Instant;

/**
 * 返回给管理端或前端的文档信息，不直接暴露 Mapper/数据库对象。
 */
public record DocumentResponse(
        String id,
        String tenantId,
        String uploadedBy,
        String documentKey,
        String title,
        String sourceUri,
        String contentHash,
        Integer version,
        DocumentStatus status,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * 将数据库对象转换成对外返回的 DTO，避免直接暴露数据库对象。
     */
    public static DocumentResponse from(KnowledgeDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getTenantId(),
                document.getUploadedBy(),
                document.getDocumentKey(),
                document.getTitle(),
                document.getSourceUri(),
                document.getContentHash(),
                document.getVersion(),
                document.getStatus(),
                document.getErrorMessage(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
