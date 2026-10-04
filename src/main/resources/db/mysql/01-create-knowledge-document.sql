CREATE TABLE IF NOT EXISTS knowledge_document
(
    id            VARCHAR(36)   NOT NULL,
    tenant_id     VARCHAR(64)   NOT NULL,
    uploaded_by   VARCHAR(128)  NOT NULL,
    document_key  VARCHAR(128)  NOT NULL,
    title         VARCHAR(255)  NOT NULL,
    source_uri    VARCHAR(1024) NOT NULL,
    content_hash  VARCHAR(128)  NOT NULL,
    version       INT           NOT NULL,
    status        VARCHAR(32)   NOT NULL,
    error_message VARCHAR(2000) NULL,
    created_at    TIMESTAMP(6)  NOT NULL,
    updated_at    TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_document_tenant_key_version
        UNIQUE (tenant_id, document_key, version)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '企业知识库文档登记表';

ALTER TABLE knowledge_document
    MODIFY id VARCHAR(36) NOT NULL COMMENT '文档主键，MyBatis-Plus 生成的 UUID',
    MODIFY tenant_id VARCHAR(64) NOT NULL COMMENT '租户标识，代表公司/组织等数据隔离空间',
    MODIFY uploaded_by VARCHAR(128) NOT NULL COMMENT '上传者用户标识，不等同于租户',
    MODIFY document_key VARCHAR(128) NOT NULL COMMENT '文档稳定业务标识，不直接使用文件名',
    MODIFY title VARCHAR(255) NOT NULL COMMENT '文档标题',
    MODIFY source_uri VARCHAR(1024) NOT NULL COMMENT '原始文件位置，例如对象存储 URI',
    MODIFY content_hash VARCHAR(128) NOT NULL COMMENT '原始内容哈希，用于判断内容是否变化',
    MODIFY version INT NOT NULL COMMENT '同一租户同一文档 Key 的版本号',
    MODIFY status VARCHAR(32) NOT NULL COMMENT '文档状态：DRAFT/PROCESSING/PUBLISHED/FAILED/ARCHIVED',
    MODIFY error_message VARCHAR(2000) NULL COMMENT '最近一次处理失败的原因',
    MODIFY created_at TIMESTAMP(6) NOT NULL COMMENT '登记创建时间',
    MODIFY updated_at TIMESTAMP(6) NOT NULL COMMENT '最近更新时间';
