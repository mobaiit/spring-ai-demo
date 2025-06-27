package com.ai.demo.controller;

import com.ai.demo.advisor.MyLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultAdvisors(new MyLoggerAdvisor()).build();
    }


    @GetMapping(value = "/streamChat")
    public Flux<String> hello(@RequestParam("message") String message) {
        Flux<String> content = chatClient.prompt()
                .user(message)
                .stream()
                .content();
        return content.concatWith(Flux.just("[complete]"));
    }

}
