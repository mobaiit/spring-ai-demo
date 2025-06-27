package com.ai.demo.controller;

import cn.hutool.core.date.DateUtil;
import com.ai.demo.advisor.MyLoggerAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author yuchen
 * @date 2025/6/26 18:12
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Resource
    private ToolCallback[] allTools;

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor())
                .build();
    }


    @GetMapping(value = "/streamChat")
    public Flux<String> hello(@RequestParam("message") String message) {
        // 创建带占位符的提示模板
        String systemPrompt = """
        您是落墨留白的专属管家，请以友好且愉快的方式与用户聊天。
        请讲中文，今天的日期是%s
        """.formatted(DateUtil.now());

        Flux<String> content = chatClient.prompt(systemPrompt)
                .user(message)
                .tools(allTools)
                .stream()
                .content();
        return content.concatWith(Flux.just("[complete]"));
    }
}
