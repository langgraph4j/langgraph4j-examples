package org.bsc.langgraph4j.mflix;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ReasoningAgent implements AsyncNodeAction<MflixState> {

    private final ChatClient reasoningChatClient;
    private final TaskExecutor taskExecutor;

    public ReasoningAgent(ChatClient reasoningChatClient, TaskExecutor taskExecutor) {
        this.reasoningChatClient = reasoningChatClient;
        this.taskExecutor = taskExecutor;
    }


    @Override
    public CompletableFuture<Map<String, Object>> apply(MflixState state) {
        return CompletableFuture.supplyAsync(() -> handle(state), taskExecutor);
    }

    private Map<String, Object> handle(MflixState state) {
        ResponderDto responder = reasoningChatClient.prompt()
                .options(ChatOptions.builder().build())
                .system("""
                        You are a query router that analyzes user questions and determines which expert should handle them.
                        
                        Your task: Examine the user's query and decide whether it relates to movies/entertainment or general topics.
                        
                        ROUTING RULES:
                        - Route to "cinema_specialist" for: movies, TV shows, actors, directors, film recommendations, reviews, ratings, streaming services, entertainment industry
                        - Route to "knowledge_assistant" for: all other topics (science, technology, history, general knowledge, etc.)
                        
                        RESPONSE FORMAT: You must respond with a JSON object containing only the expert name:
                        {"name": "cinema_specialist"} OR {"name": "knowledge_assistant"}
                        
                        EXAMPLES:
                        - "What's the best Marvel movie?" → {"name": "cinema_specialist"}
                        - "Who directed Inception?" → {"name": "cinema_specialist"}
                        - "How does photosynthesis work?" → {"name": "knowledge_assistant"}
                        - "What's the weather like?" → {"name": "knowledge_assistant"}
                        
                        Analyze the query carefully and respond with the appropriate expert name only.
                        """)
                .user(state.<String>value(MflixState.USER_QUERY_KEY).orElseThrow())
                .call()
                .entity(ResponderDto.class);
        return Map.of(MflixState.RESPONDER, responder.name());
    }

    record ResponderDto(String name) {}
}