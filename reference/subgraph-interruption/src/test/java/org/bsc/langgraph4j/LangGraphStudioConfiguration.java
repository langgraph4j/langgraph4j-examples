package org.bsc.langgraph4j;

import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class LangGraphStudioConfiguration extends LangGraphStudioConfig {

    public LangGraphStudioConfiguration(  ) throws GraphStateException {
    }

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        try {
            return Map.ofEntries(agentWorkflow());
        } catch (GraphStateException e) {
            return Map.of();
        }
    }

    private Map.Entry<String, LangGraphStudioServer.Instance> agentWorkflow() throws GraphStateException {

        var saver = new MemorySaver();

        var workflow = AgenticWorkflowWithSubgraphInterruption.builder()
                .checkpointSaver( saver )
                .buildAsStateGraph();

        return Map.entry( "AgenticWorkflowWithSubgraphInterruption", LangGraphStudioServer.Instance.builder()
                .title("Agentic Workflow with Subgraph & Interruption")
                .compileConfig(CompileConfig.builder()
                        .releaseThread(false)
                        .checkpointSaver( saver )
                        .build())
                .graph( workflow )
                .build());
    }
}
