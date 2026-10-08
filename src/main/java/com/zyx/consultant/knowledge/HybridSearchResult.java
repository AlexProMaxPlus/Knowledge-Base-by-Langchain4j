package com.zyx.consultant.knowledge;

import dev.langchain4j.data.segment.TextSegment;

import java.util.Set;

/**
 * 混合召回和重排后的结果。
 */
public record HybridSearchResult(
        double score,
        TextSegment segment,
        Set<String> sources
) {
}
