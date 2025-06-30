package com.ai.demo.controller;

import cn.hutool.core.util.IdUtil;
import com.ai.demo.advisor.MyLoggerAdvisor;
import io.modelcontextprotocol.client.McpSyncClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@Tag(name = "聊天demo")
@RequestMapping("/demo")
class DemoController {

    private final ChatClient chatClient;

    public DemoController(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpClients) {
        var mcpToolProvider = new SyncMcpToolCallbackProvider(mcpClients);
        // 基于本地内存的聊天记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        this.chatClient = chatClientBuilder
                .defaultTools(mcpToolProvider)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor())
                .build();
    }

    // 简单同步聊天
    @GetMapping("/chat/sync")
    public String chatSync(String userInput) {
        return chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    // 流式聊天
    @GetMapping("/chat/stream")
    public Flux<String> chatStream(String userInput) {
        return chatClient.prompt()
            .user(userInput)
            .stream()
            .content();
    }

    // 流式聊天+基于内存的多轮对话记忆
    @GetMapping("/chat/streamMemory")
    public Flux<String> chatStreamMemory(String userInput) {
        String chatId = IdUtil.fastSimpleUUID();
        return chatClient.prompt()
                .user(userInput)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    @Resource
    private ToolCallback[] allTools;

    // 流式聊天+基于内存的多轮对话记忆+工具调用
    @GetMapping("/chat/streamTools")
    public Flux<String> chatStreamTools(String userInput) {
        String chatId = IdUtil.fastSimpleUUID();
        return chatClient.prompt()
                .user(userInput)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .stream()
                .content();
    }

    @Resource
    private VectorStore vectorStore;

    // 流式聊天+基于内存的多轮对话记忆+工具调用+ARG检索增强
    @GetMapping("/chat/streamArg")
    public Flux<String> chatStreamArg(String userInput) {
        String chatId = IdUtil.fastSimpleUUID();
        return chatClient.prompt()
                .user(userInput)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .tools(allTools)
                .stream()
                .content();
    }
}