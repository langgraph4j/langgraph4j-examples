package org.bsc.langgraph4j.mcp;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutorEx;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class LG4jMCPClientService {

    private final ChatModel chatModel;

    public LG4jMCPClientService(ChatModel chatModel ) {
        this.chatModel = chatModel;
    }


    public StateGraph<? extends AgentState> getAgentWorkflow() throws GraphStateException {

        return AgentExecutorEx.builder()
                .chatModel(chatModel)
                .toolsFromObject( new TestTool() )
                .build();

    }
}
