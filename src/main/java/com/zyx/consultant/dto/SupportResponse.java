package com.zyx.consultant.dto;

/**
 * AI 客服返回给 REST 层的结构化结果。
 */
public record SupportResponse(
        /** 面向用户展示的回答内容。 */
        String answer,
        /** 用户问题所属的业务分类。 */
        String category,
        /** 是否需要转交人工客服。 */
        boolean needHumanSupport,
        /** 模型自评的回答置信度，不能替代真正的质量评估。 */
        String confidence
) {
}
