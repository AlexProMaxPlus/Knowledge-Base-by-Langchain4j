package com.zyx.consultant.knowledge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 知识库文档的数据库对象。
 * MyBatis-Plus 使用本类的字段与 knowledge_document 表做映射。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String tenantId;

    private String uploadedBy;

    private String documentKey;

    private String title;

    private String sourceUri;

    private String contentHash;

    private Integer version;

    private DocumentStatus status;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String errorMessage;

    @TableField
    private Instant createdAt;

    @TableField
    private Instant updatedAt;

    /**
     * 创建新的文档登记记录，初始状态固定为 DRAFT。
     */
    public KnowledgeDocument(
            String tenantId,
            String uploadedBy,
            String documentKey,
            String title,
            String sourceUri,
            String contentHash,
            Integer version) {
        this.tenantId = tenantId;
        this.uploadedBy = uploadedBy;
        this.documentKey = documentKey;
        this.title = title;
        this.sourceUri = sourceUri;
        this.contentHash = contentHash;
        this.version = version;
        this.status = DocumentStatus.DRAFT;
    }

    /**
     * 将文档从草稿或上次失败状态改为处理中。
     */
    public void startProcessing() {
        // 只有新文档或失败后重试的文档允许重新处理。
        requireStatus(DocumentStatus.DRAFT, DocumentStatus.FAILED);
        this.status = DocumentStatus.PROCESSING;
        this.errorMessage = null;
        touch();
    }

    /**
     * 将已经处理成功的文档发布为可检索状态。
     */
    public void publish() {
        // 未经过处理的文档不能直接发布，避免半成品进入知识库。
        requireStatus(DocumentStatus.PROCESSING);
        this.status = DocumentStatus.PUBLISHED;
        this.errorMessage = null;
        touch();
    }

    /**
     * 记录文档处理失败的原因，便于后续排查和重试。
     */
    public void fail(String errorMessage) {
        // 只有正在处理的文档才能记录本次处理失败。
        requireStatus(DocumentStatus.PROCESSING);
        this.status = DocumentStatus.FAILED;
        this.errorMessage = errorMessage;
        touch();
    }

    /**
     * 将已经发布或失败的文档下线。
     */
    public void archive() {
        // 草稿和处理中状态不能直接归档，避免隐藏未完成任务。
        requireStatus(DocumentStatus.PUBLISHED, DocumentStatus.FAILED);
        this.status = DocumentStatus.ARCHIVED;
        touch();
    }

    /**
     * 新建文档时初始化创建时间和更新时间。
     */
    public void initializeTimestamps(Instant now) {
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 状态发生变化时刷新更新时间。
     */
    private void touch() {
        this.updatedAt = Instant.now();
    }

    /**
     * 判断当前状态是否允许执行目标操作。
     */
    private void requireStatus(DocumentStatus... allowed) {
        for (DocumentStatus candidate : allowed) {
            // 遍历允许状态，只要命中一个就允许当前操作。
            if (status == candidate) {
                return;
            }
        }
        throw new IllegalStateException(
                "Document " + id + " is in status " + status);
    }

}
