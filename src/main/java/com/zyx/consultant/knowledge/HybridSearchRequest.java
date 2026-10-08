package com.zyx.consultant.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * 混合检索请求：同时驱动向量召回和关键词召回。
 */
public record HybridSearchRequest(
        @NotBlank String tenantId,
        @NotBlank String question,
        String documentKey,
        @Positive Integer version,
        @Positive Integer maxResults,
        @Positive Integer candidateLimit,
        Double minVectorScore
) {
}
