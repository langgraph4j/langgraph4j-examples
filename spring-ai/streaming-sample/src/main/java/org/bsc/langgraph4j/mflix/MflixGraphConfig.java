package org.bsc.langgraph4j.mflix;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.spring.ai.serializer.jackson.SpringAIJacksonStateSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Configuration(proxyBeanMethods = false)
public class MflixGraphConfig {

    @Bean
    public StateGraph<MflixState> mflixGraph(ReasoningAgent reasoningAgent,
                                             MflixAgent mflixAgent,
                                             KnowledgeAgent knowledgeAgent) throws GraphStateException {

        return new StateGraph<>(MflixState.SCHEMA, new SpringAIJacksonStateSerializer<>(MflixState::new))
                .addNode("reasoning_agent", reasoningAgent)
                .addNode("cinema_specialist", mflixAgent)
                .addNode("knowledge_assistant", knowledgeAgent)
                .addEdge(START, "reasoning_agent")
                .addConditionalEdges("reasoning_agent",
                        (AsyncEdgeAction<MflixState>) state ->
                                CompletableFuture.completedFuture(state.<String>value(MflixState.RESPONDER).orElseThrow()),
                        Map.of(
                                "cinema_specialist", "cinema_specialist",
                                "knowledge_assistant", "knowledge_assistant"
                        ))
                .addEdge("cinema_specialist", END)
                .addEdge("knowledge_assistant", END);
    }

    @Bean
    public CompiledGraph<MflixState> agenticCompiledGraph(StateGraph<MflixState> mflixGraph) throws GraphStateException {
        return mflixGraph.compile();
    }
}