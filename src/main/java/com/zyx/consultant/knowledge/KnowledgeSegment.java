package com.zyx.consultant.knowledge;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 知识库文本片段的持久化对象。
 * MySQL 保存它用于关键词召回，Qdrant 保存同一个片段的向量用于语义召回。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("knowledge_segment")
public class KnowledgeSegment {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String vectorId;
    private String knowledgeDocumentId;
    private String tenantId;
    private String documentKey;
    private Integer version;
    private Integer segmentIndex;
    private String text;
    private String sourceUri;

    @TableField(updateStrategy = FieldStrategy.ALWAYS, exist = false)
    private Double keywordScore;

    /**
     * 从 LangChain4j 文本片段和 Qdrant 点 ID 构造 MySQL 片段记录。
     */
    public static KnowledgeSegment from(
            String vectorId,
            TextSegment segment,
            int fallbackIndex) {
        Metadata metadata = segment.metadata();
        KnowledgeSegment result = new KnowledgeSegment();
        result.vectorId = vectorId;
        result.knowledgeDocumentId = metadata.getString("knowledgeDocumentId");
        result.tenantId = metadata.getString("tenantId");
        result.documentKey = metadata.getString("documentKey");
        result.version = metadata.getInteger("version");
        result.segmentIndex = metadata.getInteger("segmentIndex") == null
                ? fallbackIndex
                : metadata.getInteger("segmentIndex");
        result.text = segment.text();
        result.sourceUri = metadata.getString("sourceUri");
        return result;
    }

    /**
     * 将数据库片段重新包装为 LangChain4j TextSegment，供统一结果返回。
     */
    public TextSegment toTextSegment() {
        Metadata metadata = new Metadata()
                .put("knowledgeDocumentId", knowledgeDocumentId)
                .put("tenantId", tenantId)
                .put("documentKey", documentKey)
                .put("version", version)
                .put("segmentIndex", segmentIndex)
                .put("sourceUri", sourceUri);
        return TextSegment.from(text, metadata);
    }
}
