package com.ai.demo.tools.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Primary // 关键注解，解决 bean 冲突
public class UnifiedToolProvider implements ToolCallbackProvider {

    private final ToolCallback[] allTools;
    private final List<McpSyncClient> mcpSyncClients; // 注入自动配置的 MCP 工具提供器
    
    // 使用构造函数注入
    @Autowired
    public UnifiedToolProvider(
        @Qualifier("allTools") ToolCallback[] allTools,
        List<McpSyncClient> mcpSyncClients
    ) {
        this.allTools = allTools;
        this.mcpSyncClients = mcpSyncClients;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        // 获取 MCP 工具
        List<ToolCallback> mcpTools = SyncMcpToolCallbackProvider.syncToolCallbacks(mcpSyncClients);

        // 合并自定义工具和 MCP 工具
        List<ToolCallback> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(allTools));
        arrayList.addAll(mcpTools);
        
        return arrayList.toArray(new ToolCallback[0]);
    }
}