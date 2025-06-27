package com.ai.demo.agent;

import com.ai.demo.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class AiManus extends ToolCallAgent {

    public AiManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("AiManus");
        String SYSTEM_PROMPT = """
                你是 AiManus，一个全能的 AI 助手，旨在解决用户提出的任何任务。
                你拥有各种工具，可以高效地完成复杂的请求。
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                根据用户需求，主动选择最合适的工具或工具组合。
                对于复杂的任务，您可以分解问题，并逐步使用不同的工具来解决。
                使用每个工具后，清晰地解释执行结果并建议后续步骤。
                如果您想在任何时候停止交互，请使用“terminate”工具/函数调用。
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
