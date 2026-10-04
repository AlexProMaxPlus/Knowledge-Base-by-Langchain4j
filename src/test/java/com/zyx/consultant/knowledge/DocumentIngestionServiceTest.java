// 当前测试类与知识库文档相关代码放在同一个包中，便于直接使用同包业务类型。
package com.zyx.consultant.knowledge;

// TextSegment 是 LangChain4j 文档切分后的文本片段类型。
import dev.langchain4j.data.segment.TextSegment;
// @Test 用来标记一个需要 JUnit 执行的测试方法。
import org.junit.jupiter.api.Test;
// @TempDir 用来让 JUnit 自动创建并清理临时目录。
import org.junit.jupiter.api.io.TempDir;

// IOException 表示临时文件读写可能失败。
import java.io.IOException;
// Files 提供创建、写入临时文本文件的方法。
import java.nio.file.Files;
// Path 表示跨平台的文件路径。
import java.nio.file.Path;
// Instant 用来给模拟的文档登记时间字段赋值。
import java.time.Instant;

// assertEquals 验证两个值是否相等。
import static org.junit.jupiter.api.Assertions.assertEquals;
// assertFalse 验证条件是否为 false。
import static org.junit.jupiter.api.Assertions.assertFalse;
// assertNotNull 验证结果是否不是 null。
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 文档解析和切分服务的单元测试。
 *
 * 这个测试不调用大模型，也不访问数据库和向量数据库，
 * 只验证“原始文本文件 -> Document -> TextSegment”的本地处理链。
 */
class DocumentIngestionServiceTest {

    /**
     * JUnit 在每次测试前自动创建的临时目录。
     * 测试结束后会自动清理，避免把测试文件写进项目目录。
     */
    @TempDir
    Path temporaryDirectory;

    /**
     * 验证三个核心行为：
     * 1. 原始文本文件可以被读取；
     * 2. 文本可以被切分成 TextSegment；
     * 3. 每个 TextSegment 都保留租户、文档 Key、版本和文档 ID。
     */
    @Test
    void parsesAndSplitsTextWithSourceMetadata() throws IOException {
        // 在 JUnit 提供的临时目录下创建一个测试文本文件路径。
        Path sourceFile = temporaryDirectory.resolve("refund-policy.txt");

        // 写入三段退款政策文本，故意让内容包含多个段落，方便切分器处理。
        Files.writeString(sourceFile, """
                退款政策第一条：用户可以在规定时间内提交退款申请。

                退款政策第二条：客服需要先核验订单状态，再决定是否进入退款流程。

                退款政策第三条：所有处理结果都必须记录在业务系统中，方便后续审计。
                """);

        // 创建一条模拟第五课 MySQL 文档登记记录。
        // 这条对象代表“数据库中已经登记过的原始文档”。
        KnowledgeDocument document = new KnowledgeDocument(
                // tenantId：文档所属的数据隔离空间。
                "tenant-a",
                // uploadedBy：执行上传操作的用户。
                "user-1",
                // documentKey：文档稳定的业务标识。
                "refund-policy",
                // title：文档展示标题。
                "退款政策",
                // sourceUri：原始文件的位置。
                sourceFile.toUri().toString(),
                // contentHash：原始内容哈希，当前测试使用示例值。
                "hash-demo",
                // version：这份业务文档的版本号。
                1);

        // 模拟文档已经完成 MyBatis-Plus 登记，因此此时已经拥有数据库主键。
        document.setId("document-1");

        // 模拟数据库记录创建时间已经生成。
        document.initializeTimestamps(Instant.now());

        // 模拟文档已经进入 PROCESSING 状态，表示可以开始解析和切分。
        document.startProcessing();

        // 创建待测试的文档处理服务，并执行“读取文件 + 解析 + 添加元数据 + 切分”流程。
        DocumentSegmentationResult result =
                new DocumentIngestionService().loadAndSplit(sourceFile, document);

        // 验证处理结果仍然关联到刚才登记的业务文档 ID。
        assertEquals(document.getId(), result.documentId());

        // 验证文本确实被切出了至少一个片段，不能得到空结果。
        assertFalse(result.segments().isEmpty());

        // 遍历每一个切分片段，确保不是只检查第一个片段。
        for (TextSegment segment : result.segments()) {
            // knowledgeDocumentId 用来把片段追溯回 MySQL 中的业务文档记录。
            assertNotNull(segment.metadata().getString("knowledgeDocumentId"));

            // tenantId 用来保证后续检索时可以做租户隔离。
            assertEquals("tenant-a", segment.metadata().getString("tenantId"));

            // documentKey 用来知道片段属于哪一份业务文档。
            assertEquals("refund-policy", segment.metadata().getString("documentKey"));

            // version 用来区分同一文档的不同版本，避免引用旧知识。
            assertEquals(1, segment.metadata().getInteger("version"));
        }
    }
}
