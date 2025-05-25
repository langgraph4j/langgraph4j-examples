package org.bsc.langgraph4j.mcp;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpResourceContents;
import dev.langchain4j.mcp.client.McpTextResourceContents;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.agentexecutor.AgentExecutor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MCPClientAgent {

    enum AiModel {

        OPENAI_GPT_4O_MINI( () -> OpenAiChatModel.builder()
                .apiKey( System.getenv("OPENAI_API_KEY") )
                .modelName( "gpt-4o-mini" )
                .supportedCapabilities(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA))
                .logResponses(true)
                .maxRetries(2)
                .temperature(0.0)
                .build() ),
        OLLAMA_LLAMA3_1_8B( () -> OllamaChatModel.builder()
                .modelName( "llama3.1" )
                .baseUrl("http://localhost:11434")
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .logResponses(true)
                .maxRetries(2)
                .temperature(0.5)
                .build() ),
        OLLAMA_QWEN2_5_7B( () -> OllamaChatModel.builder()
                .modelName( "qwen2.5:7b" )
                .baseUrl("http://localhost:11434")
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .logResponses(true)
                .maxRetries(2)
                .temperature(0.0)
                .build() )
        ;

        private final Supplier<ChatModel> modelSupplier;

        public ChatModel model() {
            return modelSupplier.get();
        }

        AiModel(  Supplier<ChatModel> modelSupplier ) {
            this.modelSupplier = modelSupplier;
        }
    }

    static class MCPPostgres implements AutoCloseable {

        final McpClient mcpClient;

        McpClient client() {
            return mcpClient;
        }

        MCPPostgres() {
            var transport = new StdioMcpTransport.Builder()
                    .command(List.of(
                            "docker",
                            "run",
                            "-i",
                            "--rm",
                            "mcp/postgres",
                            "postgresql://admin:bsorrentino@host.docker.internal:5432/mcp_db"))
                    .logEvents(true) // only if you want to see the traffic in the log
                    .environment(Map.of())
                    .build();
            this.mcpClient = new DefaultMcpClient.Builder()
                    .transport(transport)
                    .build();
        }

        String readDBSchemaAsString() {
            // List of MCP resources ( ie. tables )
            var dbTableRes = mcpClient.listResources()
                    .stream()
                    .toList();

            // For each resource extract contents ( ie. columns )
            var dbColumnsRes = dbTableRes.stream()
                    .map( res -> mcpClient.readResource( res.uri()) )
                    .flatMap( res -> res.contents().stream())
                    .filter( content -> content.type() == McpResourceContents.Type.TEXT )
                    .map(McpTextResourceContents.class::cast)
                    .map(McpTextResourceContents::text)
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

        @Override
        public void close() throws Exception {
            mcpClient.close();
        }
    }


    public static void main( String[] args ) throws Exception {

        try( var mcpClient = new MCPPostgres() ) {

            var agent = AgentExecutor.builder()
                    .chatModel( AiModel.OLLAMA_QWEN2_5_7B.model() )
                    .tool( mcpClient.client() ) // add tools directly from MCP client
                    .build()
                    .compile();


            var prompt = PromptTemplate.from(
                    """
                            You have access to the following tables:
                            
                            {{schema}}
                            
                            Answer the question using the tables above.
                            
                            {{input}}
                            """
            );

            var message = prompt.apply( Map.of(
                            "schema", mcpClient.readDBSchemaAsString(),
                            "input", "get all issues names and project" ) )
                    .toUserMessage();

            var result = agent.invoke( Map.of( "messages", message) )
                    .flatMap(AgentExecutor.State::finalResponse)
                    .orElse("no response");

            System.out.println( result );
        }

    }

}
