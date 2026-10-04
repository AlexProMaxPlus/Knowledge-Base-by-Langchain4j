package com.zyx.consultant.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 前端登记知识库文档时提交的请求参数。
 */
public record CreateDocumentRequest(
        /** 数据隔离空间，通常对应公司、组织或客户。 */
        @NotBlank String tenantId,
        /** 执行上传操作的用户标识。 */
        @NotBlank String uploadedBy,
        /** 文档稳定业务标识，不直接使用文件名。 */
        @NotBlank String documentKey,
        /** 文档展示标题。 */
        @NotBlank String title,
        /** 原始文件所在位置，例如对象存储 URI。 */
        @NotBlank String sourceUri,
        /** 原始内容哈希，用于判断内容是否变化。 */
        @NotBlank String contentHash,
        /** 同一 documentKey 在同一租户内的版本号。 */
        @NotNull @Positive Integer version
) {
}
