package com.zyx.consultant;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "langchain4j.community.zhipuai.chat-model.api-key=test-key")
@ActiveProfiles("test")
class ConsultantApplicationTests {

    /**
     * 验证 Spring Boot、LangChain4j 和 MyBatis-Plus 基础 Bean 可以一起启动。
     */
    @Test
    void contextLoads() {
    }

}
