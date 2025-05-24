package org.bsc.langgraph4j.example;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutor;
import org.springframework.ai.chat.model.ChatModel;

/**
 * @author lambochen
 */
public class MemoryAgent {

    private final StateGraph<AgentExecutor.State> graph;
    private final CompiledGraph<AgentExecutor.State> workflow;

    public MemoryAgent(ChatModel llm) throws GraphStateException {
        this(llm, new MemorySaver());
    }

    public MemoryAgent(ChatModel llm, BaseCheckpointSaver memorySaver) throws GraphStateException {
        this.graph = AgentExecutor.builder().chatModel(llm).build();
        this.workflow = graph.compile(
                CompileConfig.builder().checkpointSaver(memorySaver).build()
        );
    }

    public CompiledGraph<AgentExecutor.State> workflow() {
        return workflow;
    }

    public CompiledGraph<AgentExecutor.State> newWorkflow(CompileConfig config) throws GraphStateException {
        return graph.compile(config);
    }

    public CompiledGraph<AgentExecutor.State> newWorkflow(MemorySaver memory) throws GraphStateException {
        return graph.compile(
                CompileConfig.builder().checkpointSaver(memory).build()
        );
    }
}
