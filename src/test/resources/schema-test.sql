CREATE TABLE knowledge_document
(
    id           VARCHAR(36)   NOT NULL PRIMARY KEY,
    tenant_id    VARCHAR(64)   NOT NULL,
    uploaded_by  VARCHAR(128)  NOT NULL,
    document_key VARCHAR(128)  NOT NULL,
    title        VARCHAR(255)  NOT NULL,
    source_uri   VARCHAR(1024) NOT NULL,
    content_hash VARCHAR(128)  NOT NULL,
    version      INTEGER       NOT NULL,
    status       VARCHAR(32)   NOT NULL,
    error_message VARCHAR(2000),
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_document_tenant_key_version
        UNIQUE (tenant_id, document_key, version)
);

COMMENT ON TABLE knowledge_document IS '企业知识库文档登记表';
COMMENT ON COLUMN knowledge_document.tenant_id IS '租户标识，代表公司/组织等数据隔离空间';
COMMENT ON COLUMN knowledge_document.uploaded_by IS '上传者用户标识，不等同于租户';
COMMENT ON COLUMN knowledge_document.document_key IS '文档稳定业务标识，不直接使用文件名';
COMMENT ON COLUMN knowledge_document.title IS '文档标题';
COMMENT ON COLUMN knowledge_document.source_uri IS '原始文件位置，例如对象存储 URI';
COMMENT ON COLUMN knowledge_document.content_hash IS '原始内容哈希，用于判断内容是否变化';
COMMENT ON COLUMN knowledge_document.version IS '同一租户同一文档 Key 的版本号';
COMMENT ON COLUMN knowledge_document.status IS '文档状态：DRAFT/PROCESSING/PUBLISHED/FAILED/ARCHIVED';
COMMENT ON COLUMN knowledge_document.error_message IS '最近一次处理失败的原因';
