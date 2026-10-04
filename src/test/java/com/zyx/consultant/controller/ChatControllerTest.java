package com.zyx.consultant.controller;

import com.zyx.consultant.dto.SupportResponse;
import com.zyx.consultant.service.ConsultantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ConsultantService consultantService;

    private MockMvc mockMvc;

    /**
     * 每个测试前创建独立的 MockMvc，并注入模拟的 AI Service。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatController(consultantService))
                .build();
    }

    /**
     * 验证 HTTP 参数会传给 AI Service，并且结构化结果能转换成 JSON。
     */
    @Test
    void chatReturnsStructuredSupportResponse() throws Exception {
        when(consultantService.chat("session-1", "如何重置密码？"))
                .thenReturn(new SupportResponse("请联系管理员重置密码。", "账户问题", true, "medium"));

        // 发起 MVC 模拟请求，不访问真实大模型，专门验证 Controller 输入输出。
        mockMvc.perform(get("/chat")
                        .param("conversationId", "session-1")
                        .param("message", "如何重置密码？"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.answer").value("请联系管理员重置密码。"))
                .andExpect(jsonPath("$.category").value("账户问题"))
                .andExpect(jsonPath("$.needHumanSupport").value(true))
                .andExpect(jsonPath("$.confidence").value("medium"));
    }
}
