package com.zyx.consultant.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import com.zyx.consultant.dto.SupportResponse;

/**
 * 企业知识库客服的 AI Service 接口。
 * LangChain4j 会根据接口注解创建代理实现，调用 ChatModel 完成回答。
 */
@AiService
public interface ConsultantService {

    @SystemMessage("""
            你是企业知识库客服。
            目前知识库尚未接入，因此不能声称答案来自企业资料，也不能编造企业政策、产品事实或承诺。
            如果用户询问具体企业信息且上下文没有提供依据，请在 answer 中说明当前没有可核验的知识库依据，
            并将 needHumanSupport 设为 true。
            category 使用简短的中文分类，例如“产品咨询”“账户问题”“其他”。
            answer 使用清楚、礼貌的中文。
            confidence 表示客服对答案的置信度，有low、medium、high三种值。
            """)
    SupportResponse chat(@MemoryId String conversationId, @UserMessage String message);
}
