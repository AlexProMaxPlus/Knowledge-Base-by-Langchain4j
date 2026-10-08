package com.zyx.consultant.knowledge;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识片段 Mapper。
 * BaseMapper 提供常规插入/更新，自定义 XML 负责关键词搜索和状态过滤。
 */
public interface KnowledgeSegmentMapper extends BaseMapper<KnowledgeSegment> {

    /**
     * 查询租户内已发布文档的 ID，用于过滤 Qdrant 的向量候选。
     */
    List<String> findPublishedDocumentIds(
            @Param("tenantId") String tenantId,
            @Param("documentKey") String documentKey,
            @Param("version") Integer version);

    /**
     * 使用 MySQL FULLTEXT 对文本片段做关键词召回。
     */
    List<KnowledgeSegment> searchPublishedByKeyword(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("documentKey") String documentKey,
            @Param("version") Integer version,
            @Param("limit") int limit);
}
