package com.zyx.consultant.knowledge;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * 负责把原始文本文件转换成 LangChain4j 的 TextSegment。
 *
 * <p>本服务暂时只完成“解析 + 切分”，不生成向量，也不写向量数据库。
 * 这样可以单独验证 RAG 索引链的每一步输入和输出。</p>
 */
@Service
public class DocumentIngestionService {

    /**
     * 每个片段的最大字符数。
     * 生产项目应结合 Embedding 模型的上下文限制和业务文本特点调整。
     */
    private static final int MAX_SEGMENT_SIZE = 300;

    /**
     * 相邻片段保留的重叠字符数，用于减少边界处的语义断裂。
     */
    private static final int MAX_OVERLAP_SIZE = 30;

    /**
     * 解析一个原始文本文件并切分成带来源元数据的 TextSegment。
     *
     * @param sourceFile 原始文本文件路径
     * @param document   第五课登记的文档业务对象
     * @return 文档 ID 和切分后的文本片段
     */
    public DocumentSegmentationResult loadAndSplit(
            Path sourceFile,
            KnowledgeDocument document) {
        // 使用 UTF-8 文本解析器，把文件字节转换成 LangChain4j Document，也就是能被 LangChain4j框架 处理的文本片段。
        Document langchainDocument = FileSystemDocumentLoader.loadDocument(
                sourceFile,
                new TextDocumentParser(StandardCharsets.UTF_8));

        // 用自定义方法，给 LangChain4j Document 增加业务来源信息（放元数据中），后续每个 TextSegment 都能追溯到原文。
        addSourceMetadata(langchainDocument, document);

        // recursive 会优先按段落切分，过长时再逐步按行、句子和单词切分。
        List<TextSegment> segments = DocumentSplitters
                .recursive(MAX_SEGMENT_SIZE, MAX_OVERLAP_SIZE)
                .split(langchainDocument);

        //
        return new DocumentSegmentationResult(document.getId(), segments);
    }

    /**
     * 将 MySQL 文档登记信息写入 LangChain4j Document 的元数据。
     */
    private void addSourceMetadata(
            Document langchainDocument,
            KnowledgeDocument document) {
        Metadata metadata = langchainDocument.metadata();
        metadata.put("knowledgeDocumentId", document.getId());
        metadata.put("tenantId", document.getTenantId());
        metadata.put("uploadedBy", document.getUploadedBy());
        metadata.put("documentKey", document.getDocumentKey());
        metadata.put("version", document.getVersion());
        metadata.put("sourceUri", document.getSourceUri());
    }
}
