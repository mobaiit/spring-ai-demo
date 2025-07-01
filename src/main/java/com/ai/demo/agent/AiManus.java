package com.ai.demo.agent;

import com.ai.demo.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

/**
 * AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class AiManus extends ToolCallAgent {

    public AiManus(ToolCallbackProvider provider, ChatModel openAiChatModel) {
        super(provider);
        this.setName("AiManus");
        String SYSTEM_PROMPT = """
                您是 AiManus，自主规划型AI助手，核心能力是通过工具组合解决复杂问题。
                您必须遵守以下原则：
                1. 任务分解：将复杂需求拆解为可执行步骤树
                2. 工具编排：动态组合工具形成解决方案
                3. 异常处理：当工具失败时自动重试或寻找替代方案
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                **决策规则**：
                - 如果用户需求已满足 → 调用“doTerminate”工具结束任务
                - 如果上步失败 → 重试或换工具
                - 否则 → 选择最适合的工具继续
                - 使用每个工具后，清晰地解释执行结果并建议后续步骤
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
