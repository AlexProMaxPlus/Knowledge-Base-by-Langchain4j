package com.zyx.consultant.knowledge;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge/documents")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService service;

    public KnowledgeDocumentController(KnowledgeDocumentService service) {
        this.service = service;
    }

    /**
     * 登记一份新的知识库文档，初始状态为 DRAFT。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse create(@Valid @RequestBody CreateDocumentRequest request) {
        return service.create(request);
    }

    /**
     * 按文档 ID 查询文档登记信息。
     */
    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable String id) {
        return service.get(id);
    }

    /**
     * 查询指定租户下的文档列表。
     */
    @GetMapping
    public List<DocumentResponse> list(
            @RequestParam @NotBlank String tenantId) {
        return service.listByTenant(tenantId);
    }

    /**
     * 将文档从 DRAFT 或 FAILED 改为 PROCESSING。
     */
    @PostMapping("/{id}/processing")
    public DocumentResponse startProcessing(@PathVariable String id) {
        return service.startProcessing(id);
    }

    /**
     * 发布处理完成的文档，使它允许被知识库检索。
     */
    @PostMapping("/{id}/publish")
    public DocumentResponse publish(@PathVariable String id) {
        return service.publish(id);
    }

    /**
     * 记录文档处理失败和失败原因。
     */
    @PostMapping("/{id}/fail")
    public DocumentResponse fail(
            @PathVariable String id,
            @RequestParam @NotBlank String message) {
        return service.fail(id, message);
    }

    /**
     * 归档文档，使它不再作为当前知识被使用。
     */
    @PostMapping("/{id}/archive")
    public DocumentResponse archive(@PathVariable String id) {
        return service.archive(id);
    }
}
