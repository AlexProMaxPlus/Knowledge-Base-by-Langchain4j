package com.zyx.consultant.knowledge;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class KnowledgeDocumentService {

    private final KnowledgeDocumentMapper mapper;

    public KnowledgeDocumentService(KnowledgeDocumentMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 登记文档元数据，并阻止同一租户重复登记同一版本。
     */
    @Transactional
    public DocumentResponse create(CreateDocumentRequest request) {
        // 先做业务层检查，为调用方提供明确的重复版本提示。
        if (mapper.existsByTenantIdAndDocumentKeyAndVersion(
                request.tenantId(), request.documentKey(), request.version())) {
            throw new IllegalArgumentException(
                    "Document version already exists: " + request.documentKey()
                            + " v" + request.version());
        }

        KnowledgeDocument document = new KnowledgeDocument(
                request.tenantId(),
                request.uploadedBy(),
                request.documentKey(),
                request.title(),
                request.sourceUri(),
                request.contentHash(),
                request.version());
        document.initializeTimestamps(Instant.now());
        mapper.insert(document);
        return DocumentResponse.from(document);
    }

    /**
     * 按主键查询文档；找不到时抛出明确异常。
     */
    @Transactional(readOnly = true)
    public DocumentResponse get(String id) {
        return DocumentResponse.from(find(id));
    }

    /**
     * 查询指定租户下的全部文档，供后台管理和列表页面使用。
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> listByTenant(String tenantId) {
        return mapper.findByTenantIdOrderByUpdatedAtDesc(tenantId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    /**
     * 开始处理文档，后续可在此状态执行解析、切分和向量化。
     */
    @Transactional
    public DocumentResponse startProcessing(String id) {
        KnowledgeDocument document = find(id);
        document.startProcessing();
        mapper.updateById(document);
        return DocumentResponse.from(document);
    }

    /**
     * 发布处理成功的文档，使它进入可检索状态。
     */
    @Transactional
    public DocumentResponse publish(String id) {
        KnowledgeDocument document = find(id);
        document.publish();
        mapper.updateById(document);
        return DocumentResponse.from(document);
    }

    /**
     * 记录文档处理失败及失败原因。
     */
    @Transactional
    public DocumentResponse fail(String id, String message) {
        KnowledgeDocument document = find(id);
        document.fail(message);
        mapper.updateById(document);
        return DocumentResponse.from(document);
    }

    /**
     * 归档文档，使它不再作为当前知识参与检索。
     */
    @Transactional
    public DocumentResponse archive(String id) {
        KnowledgeDocument document = find(id);
        document.archive();
        mapper.updateById(document);
        return DocumentResponse.from(document);
    }

    /**
     * 使用 MyBatis-Plus 按主键查找文档。
     */
    private KnowledgeDocument find(String id) {
        KnowledgeDocument document = mapper.selectById(id);
        // selectById 找不到记录时返回 null，需要先判断再继续使用对象。
        if (document == null) {
            throw new IllegalArgumentException("Knowledge document not found: " + id);
        }
        return document;
    }
}
