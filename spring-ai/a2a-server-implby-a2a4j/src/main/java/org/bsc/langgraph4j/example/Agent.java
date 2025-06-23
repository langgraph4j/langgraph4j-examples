package org.bsc.langgraph4j.example;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lambochen
 */
@Component
public class Agent {

    private final StateGraph<AgentExecutor.State> graph;
    private final CompiledGraph<AgentExecutor.State> workflow;

    public Agent(ChatModel llm) throws GraphStateException {
        this.graph = AgentExecutor.builder().chatModel(llm).build();
        this.workflow = graph.compile();
    }

    public AgentExecutor.State execute(String prompt) {
        return workflow.invoke(
                Map.of("messages", new UserMessage(prompt))
        ).orElseThrow();
    }
}
