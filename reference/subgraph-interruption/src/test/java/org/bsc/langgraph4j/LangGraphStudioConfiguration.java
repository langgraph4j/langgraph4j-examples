package org.bsc.langgraph4j;

import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.studio.springboot.AbstractLangGraphStudioConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphFlow;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class LangGraphStudioConfiguration extends AbstractLangGraphStudioConfig {

    final LangGraphFlow flow;

    public LangGraphStudioConfiguration(  ) throws GraphStateException {

        // Create workflow
        this.flow = null;
    }

    private LangGraphFlow agentWorkflow( StateGraph<? extends AgentState> workflow ) throws GraphStateException {

        return  LangGraphFlow.builder()
                .title("LangGraph Studio")
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
