package org.bsc.langgraph4j.mcp;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.studio.springboot.AbstractLangGraphStudioConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphFlow;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class LG4jMCPClientStudioConfiguration extends AbstractLangGraphStudioConfig {

    private final LangGraphFlow flow;

    public LG4jMCPClientStudioConfiguration( LG4jMCPClientService agentService ) throws GraphStateException {

        this.flow = agentWorkflow( agentService.getAgentWorkflow() );
    }

    private LangGraphFlow agentWorkflow( StateGraph<? extends AgentState> workflow ) throws GraphStateException {

        return  LangGraphFlow.builder()
                .title("LangGraph4j MCP Client")
                .addInputStringArg( "messages", true, v -> new UserMessage( Objects.toString(v) ) )
                .stateGraph( workflow )
                .compileConfig( CompileConfig.builder()
                        .checkpointSaver( new MemorySaver() )
                        .releaseThread(true)
                        .build())
                .build();

    }

    @Override
    public LangGraphFlow getFlow() {
        return this.flow;
    }
}