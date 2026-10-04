package com.zyx.consultant.knowledge;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

/**
 * 文档解析和切分后的结果。
 * 里面放了文档的 ID 和切分后的片段 segments 列表。
 * 当前只保存切分片段，下一课再把片段交给 Embedding 模型。
 */
public record DocumentSegmentationResult(
        String documentId,
        List<TextSegment> segments
) {
}
