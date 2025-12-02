package org.bsc.langgraph4j.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutorEx;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class LG4jMCPClientService {

    private final ChatModel chatModel;
    private final List<McpSyncClient> mcpSyncClient;

    public LG4jMCPClientService(ChatModel chatModel, List<McpSyncClient> mcpSyncClient ) {
        this.chatModel = chatModel;
        this.mcpSyncClient = mcpSyncClient;
    }

    private String readDBSchemaAsString( McpSyncClient mcpSyncClient ) {
        // List of MCP resources ( ie. tables )
        var dbTableRes = mcpSyncClient.listResources().resources()
                .stream()
                .toList();

        // For each resource extract contents ( ie. columns )
        var dbColumnsRes = dbTableRes.stream()
                .map(mcpSyncClient::readResource)
                .flatMap( res -> res.contents().stream())
                .peek( content -> System.out.println(content.mimeType()))
                .filter( content -> Objects.equals(content.mimeType(), "application/json")  )
                .map( McpSchema.TextResourceContents.class::cast)
                .map(McpSchema.TextResourceContents::text)
                .toList();


        var schema = new StringBuilder();
        for( var i = 0; i < dbTableRes.size() ; ++i ) {

            schema.append( dbTableRes.get(i).name() )
                    .append(" = ")
                    .append( dbColumnsRes.get(i) )
                    .append("\n\n");

        }

        return schema.toString();
    }

    private String getSystemMessageWithDBSchema() {
        var prompt = new PromptTemplate("""
                        You have access to the following tables:
                        
                        {schema}
                        
                        Answer the question using the tables above.
                        """
        );

        return prompt.create(
                Map.of("schema", readDBSchemaAsString(mcpSyncClient.get(0)) ) )
                .getContents();

    }

    public StateGraph<? extends AgentState> getAgentWorkflow() throws GraphStateException {

        return AgentExecutorEx.builder()
                .chatModel(chatModel)
                .tools(SyncMcpToolCallbackProvider.builder()
                        .mcpClients(mcpSyncClient)
                        .build()) // add tools directly from MCP client
                .defaultSystem( getSystemMessageWithDBSchema() )
                .build();

    }
}
