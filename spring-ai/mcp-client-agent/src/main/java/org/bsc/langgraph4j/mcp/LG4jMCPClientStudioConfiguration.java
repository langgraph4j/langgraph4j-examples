package org.bsc.langgraph4j.mcp;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphFlow;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;

@Configuration
public class LG4jMCPClientStudioConfiguration extends LangGraphStudioConfig {
    private Logger log = LoggerFactory.getLogger(LG4jMCPClientStudioConfiguration.class);

    private final LG4jMCPClientService agentService;

    public LG4jMCPClientStudioConfiguration( LG4jMCPClientService agentService ) {
        this.agentService = agentService;
    }

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        try {

            var instance =  LangGraphStudioServer.Instance.builder()
                    .title("LangGraph4j MCP Client")
                    .addInputStringArg( "messages", true, v -> new UserMessage( Objects.toString(v) ) )
                    .graph( agentService.getAgentWorkflow() )
                    .compileConfig( CompileConfig.builder()
                            .checkpointSaver( new MemorySaver() )
                            .releaseThread(true)
                            .build())
                    .build();

            return Map.of("agent", instance);

        } catch (GraphStateException e) {
            log.error(e.getMessage(), e);
            return Map.of();
        }


    }
}