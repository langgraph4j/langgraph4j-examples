package org.bsc.langgraph4j.mcp;


import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
public class MCPClientAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPClientAgentApplication.class, args);
    }


    String readDBSchemaAsString( McpSyncClient mcpSyncClient ) {
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

    @Bean
    CommandLineRunner memoryChatbot(ChatModel llm, List<McpSyncClient> mcpSyncClient) throws GraphStateException {
        return args -> {
            var agent = AgentExecutor.builder()
                    .chatModel(llm)
                    .tools(new SyncMcpToolCallbackProvider(mcpSyncClient)) // add tools directly from MCP client
                    .build()
                    .compile();

            var prompt = new PromptTemplate(
                    """
                            You have access to the following tables:
                            
                            {schema}
                            
                            Answer the question using the tables above.
                            
                            {input}
                            """
            );

            var message = prompt.create(Map.of(
                            "schema", readDBSchemaAsString(mcpSyncClient.get(0)),
                            "input", "get all issues names and project"))
                    .getUserMessage();

            var result = agent.invoke(Map.of("messages", message))
                    .orElseThrow();

            System.out.println(result);
        };
    }

}
