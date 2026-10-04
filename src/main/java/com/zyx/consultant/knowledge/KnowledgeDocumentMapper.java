package com.zyx.consultant.knowledge;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    /**
     * 查询某个租户的文档，并按更新时间和版本倒序排列。
     */
    List<KnowledgeDocument> findByTenantIdOrderByUpdatedAtDesc(
            @Param("tenantId") String tenantId);

    /**
     * 判断同一租户、文档 Key 和版本是否已存在，防止重复登记。
     */
    boolean existsByTenantIdAndDocumentKeyAndVersion(
            @Param("tenantId") String tenantId,
            @Param("documentKey") String documentKey,
            @Param("version") Integer version);
}
