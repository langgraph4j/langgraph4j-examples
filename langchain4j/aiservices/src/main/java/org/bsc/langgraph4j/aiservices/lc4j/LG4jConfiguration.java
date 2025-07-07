package org.bsc.langgraph4j.aiservices.lc4j;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.utils.EdgeMappings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Configuration
public class LG4jConfiguration {

    @Bean
    public StateGraph<State> buildGraphNoStatic(
            SupervisorAgent supervisorAgent,
            CoderAgent coderAgent,
            ResearchAgent researchAgent
    ) throws GraphStateException {

        return new StateGraph<>( State.SCHEMA, State.serializer() )
                .addNode( "supervisor", node_async(supervisorAgent))
                .addNode( "coder", coderAgent )
                .addNode( "researcher",node_async(researchAgent) )
                .addEdge( START, "coder")
                .addConditionalEdges( "supervisor",
                        edge_async( state ->
                                state.next().orElseThrow( () -> new IllegalStateException("no next value is present"))
                        ),
                        EdgeMappings.builder()
                                .to( "coder" )
                                .to( "researcher")
                                .toEND( "FINISH")
                                .build())
                .addEdge( "coder", "supervisor")
                .addEdge( "researcher", "supervisor")
                ;
    }

}
