package com.ai.demo.controller;

import com.ai.demo.agent.ChatDemo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * @author yuchen
 * @date 2025/6/26 18:12
 */
@Slf4j
@RestController
@Tag(name = "聊天demo")
@RequestMapping("/ai")
public class ChatController {

    @Resource
    private ChatDemo chatDemo;

    @Operation(summary = "同步聊天")
    @GetMapping("/chat/sync")
    public String doChatWithSync(String message, String chatId) {
        return chatDemo.doChat(message, chatId);
    }

    @Operation(summary = "流式调用 tools Arg")
    @GetMapping(value = "/chat/stream")
    public Flux<String> doChatStream(String message, String chatId) {
        return chatDemo.doChatWithRagStream(message, chatId);
    }

    /**
     * 从向量数据库中查找文档，并将查询的文档作为上下文回答。
     *
     * @param message 用户的提问
     * @return SSE流响应
     */
    @GetMapping(value = "chat/stream/database", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStreamWithDatabase(@RequestParam String message) {
        return chatDemo.chatStreamWithDatabase(message);
    }

    @Operation(summary = "SSE 流式调用,响应文件")
    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithSSE(String message, String chatId) {
        return chatDemo.doChatByStream(message, chatId);
    }

    @Operation(summary = "SSE 流式调用,响应")
    @GetMapping(value = "/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithServerSentEvent(String message, String chatId) {
        return chatDemo.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @Operation(summary = "SSE 流式调用")
    @GetMapping(value = "/chat/sse_emitter")
    public SseEmitter doChatWithServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        chatDemo.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

}
