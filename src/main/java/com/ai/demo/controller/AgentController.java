package com.ai.demo.controller;

import com.ai.demo.agent.AiManus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author yuchen
 * @date 2025/6/26 18:12
 */
@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private ToolCallbackProvider provider;

    @Resource
    private ChatModel openAiChatModel;

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        AiManus aiManus = new AiManus(provider, openAiChatModel);
        return aiManus.runStream(message);
    }

}
