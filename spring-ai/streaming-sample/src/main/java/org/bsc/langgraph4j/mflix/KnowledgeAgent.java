package org.bsc.langgraph4j.mflix;

import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.spring.ai.generators.StreamingChatGenerator;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class KnowledgeAgent implements AsyncNodeAction<MflixState> {

    private final ChatClient chatClient;
    private final TaskExecutor taskExecutor;

    public KnowledgeAgent(ChatClient chatClient, TaskExecutor taskExecutor) {
        this.chatClient = chatClient;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(MflixState state) {
        return CompletableFuture.supplyAsync(() -> handleKnowledgeQuery(state), taskExecutor);
    }

    private Map<String, Object> handleKnowledgeQuery(MflixState state) {
        String userQuery = state.<String>value(MflixState.USER_QUERY_KEY).orElseThrow();

        // Create the streaming response - no buffering, pure streaming!
        Flux<ChatResponse> responseStream = chatClient.prompt()
                .system("""
                        You are a knowledgeable assistant that provides helpful, accurate, and detailed responses
                        to general questions. You excel at explaining complex topics in a clear and understandable way.
                        
                        Guidelines:
                        - Provide accurate and well-researched information
                        - Structure your responses clearly with proper formatting
                        - Use examples when helpful
                        - Admit when you're uncertain about something
                        - Keep responses informative but concise
                        - Be engaging and educational in your explanations
                        """)
                .options(ChatOptions.builder().model(AnthropicApi.ChatModel.CLAUDE_3_5_HAIKU.getValue()).build())
                .user(userQuery)
                .stream()
                .chatResponse();


        AsyncGenerator<? extends NodeOutput<AgentState>> generator = StreamingChatGenerator.builder()
                .mapResult(response -> Map.of("messages", response.getResult().getOutput().getText()))
                .startingNode("agent")
                .startingState(state)
                .build(responseStream);

        return Map.of(MflixState.RESPONSE_STREAM, generator.async(taskExecutor));
    }
}