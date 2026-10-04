package com.zyx.consultant.controller;

import com.zyx.consultant.dto.SupportResponse;
import com.zyx.consultant.service.ConsultantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ConsultantService consultantService;

    public ChatController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    /**
     * 接收前端的会话 ID 和问题，并交给 LangChain4j AI Service。
     */
    @GetMapping("/chat")
    public SupportResponse chat(
            @RequestParam(value = "conversationId", defaultValue = "default") String conversationId,
            @RequestParam(value = "message", defaultValue = "Hello") String message) {
        return consultantService.chat(conversationId, message);
    }
}
