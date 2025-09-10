package org.bsc.langgraph4j.mflix;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@   Component
public class MflixAgent implements AsyncNodeAction<MflixState> {

    private final ChatClient chatClient;

    public MflixAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(MflixState state) {
        return CompletableFuture.completedFuture(
                Map.of(MflixState.RESPONSE_STREAM, Flux.just("Not implemented yet!"))
        );
    }
}