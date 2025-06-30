package com.ai.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author yuchen
 * @date 2025/6/26 18:53
 */

@Configuration
public class ModelConfig {

    // openAI 协议模型自动配置
    @Bean
    @Primary
    public ChatClient chatClient() {
        OpenAiApi openaiApiKey = OpenAiApi.builder()
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .completionsPath("/chat/completions")
                .apiKey("demo")
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("glm-4-flash")
                .temperature(0.7)
                .maxTokens(100)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openaiApiKey)
                .defaultOptions(options)
                .build();

        // 3. 创建流式聊天客户端
        return ChatClient.create(chatModel);
    }
}
