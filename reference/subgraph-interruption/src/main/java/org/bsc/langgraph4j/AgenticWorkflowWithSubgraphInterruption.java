package org.bsc.langgraph4j;

import org.bsc.langgraph4j.action.AsyncCommandAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncCommandAction.command_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mergeMap;

public interface AgenticWorkflowWithSubgraphInterruption {

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AgenticWorkflowWithSubgraphInterruption.class);

   class SubGraphInterruptionException extends Exception {
        final String parentNodeId;
        final String nodeId;
        final Map<String,Object> state;

        public SubGraphInterruptionException( String parentNodeId, String nodeId, Map<String,Object> state ) {
            super( format("interruption in subgraph: %s on node: %s", parentNodeId, nodeId));
            this.parentNodeId = parentNodeId;
            this.nodeId = nodeId;
            this.state = state;
        }

       public static Optional<SubGraphInterruptionException> from(Throwable throwable) {
           Throwable current = throwable;
           while (current != null) {
               if ( current instanceof SubGraphInterruptionException ex ) {
                   return Optional.of(ex);
               }
               current = current.getCause();
           }
           return Optional.empty();
       }
    }

    class MyState extends MessagesState<String> {

        public MyState(Map<String, Object> initData) {
            super(initData);
        }

        boolean resumeSubgraph( ) {
            return this.<Boolean>value( "resume_subgraph")
                        .orElse( false );
        }
    }
    static Builder builder() {
        return new Builder();
    }

    class Builder {
        BaseCheckpointSaver saver;

        public Builder checkpointSaver( BaseCheckpointSaver saver ) {
            this.saver = saver;
            return this;
        }

        private AsyncNodeAction<MyState> _makeNode( String withMessage ) {
            return node_async(state ->
                    Map.of("messages", format( "pass to: [%s]", withMessage) )
            );
        }

        private AsyncCommandAction<MyState> _makeCommandNode(Command command ) {
            return command_async((state, config) ->
                    requireNonNull(command)
            );
        }

        private AsyncCommandAction<MyState> _makeCommandNode( String goToNode ) {
            return _makeCommandNode(new Command(goToNode));
        }

        private AsyncNodeAction<MyState> _makeSubgraphNode( String parentNodeId, CompiledGraph<MyState> subGraph ) {
            final var runnableConfig = RunnableConfig.builder()
                    .threadId( format("%s_subgraph", parentNodeId ) )
                    .build();
            return node_async(state -> {

                var input = (state.resumeSubgraph()) ?
                        GraphInput.resume() :
                        GraphInput.args( state.data() );

                var output = subGraph.stream(input, runnableConfig).stream()
                        .reduce((a, b) -> b)
                        .orElseThrow();

                if( !output.isEND() ) {
                    throw new SubGraphInterruptionException( parentNodeId,
                                                             output.node(),
                                                             mergeMap( output.state().data(), Map.of("resume_subgraph", true) ) );
                }
                return mergeMap(output.state().data(), Map.of("resume_subgraph", AgentState.MARK_FOR_REMOVAL) );
            });
        }

        private CompiledGraph<MyState> subGraph( BaseCheckpointSaver saver ) throws Exception {

            var compileConfig = CompileConfig.builder()
                    .checkpointSaver(saver)
                    .interruptAfter("NODE3.2")
                    .build();

            var stateSerializer = new ObjectStreamStateSerializer<>( MyState::new );

            return new StateGraph<>(MyState.SCHEMA, stateSerializer)
                    .addEdge(START, "NODE3.1" )
                    .addNode( "NODE3.1", _makeNode("NODE3.1") )
                    .addNode("NODE3.2", _makeNode( "NODE3.2") )
                    .addNode("NODE3.3", _makeNode( "NODE3.3"))
                    .addNode("NODE3.4", _makeNode( "NODE3.4"))
                    .addEdge("NODE3.1", "NODE3.2")
                    .addEdge("NODE3.2", "NODE3.3")
                    .addEdge("NODE3.3", "NODE3.4")
                    .addEdge("NODE3.4", END)
                    .compile(compileConfig);
        }

        public CompiledGraph<MyState>  build() throws Exception {
            requireNonNull( saver, "checkpointSaver is required!");

            var stateSerializer = new ObjectStreamStateSerializer<>( MyState::new );

            var compileConfig = CompileConfig.builder()
                    .checkpointSaver(saver)
                    .build();

            var subGraph = subGraph(saver); // create subgraph

            return new StateGraph<>(MyState.SCHEMA, stateSerializer)
                    .addEdge(START, "NODE1")
                    .addNode("NODE1",_makeNode( "NODE1"))
                    .addNode("NODE2", _makeNode( "NODE2") )
                    .addNode("NODE3", _makeSubgraphNode( "NODE3", subGraph ))
                    .addNode("NODE4", _makeNode( "NODE4"))
                    .addNode("NODE5", _makeNode( "NODE5"))
                    .addEdge("NODE1", "NODE2")
                    .addEdge("NODE2", "NODE3")
                    .addEdge("NODE3", "NODE4")
                    .addEdge("NODE4", "NODE5")
                    .addEdge("NODE5", END)
                    .compile( compileConfig );

       }
    }

}
