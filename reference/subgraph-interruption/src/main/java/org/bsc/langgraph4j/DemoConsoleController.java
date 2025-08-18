package org.bsc.langgraph4j;

import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * Demonstrates the use of Spring Boot CLI to execute a task using an agent executor.
 */
@Controller
public class DemoConsoleController implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DemoConsoleController.class);


    /**
     * Executes the command-line interface to demonstrate a Spring Boot application.
     * This method logs a welcome message, constructs a graph using an agent executor,
     * compiles it into a workflow, invokes the workflow with a specific input,
     * and then logs the final result.
     *
     * @param args Command line arguments (Unused in this context)
     * @throws Exception If any error occurs during execution
     */
    @Override
    public void run(String... args) throws Exception {

        log.info("Welcome to the Spring Boot CLI application!");

        var console = System.console();

        runAgent( console  );
    }

    public void runAgent( java.io.Console console ) throws Exception {

        var saver = new MemorySaver();

        var runnableConfig = RunnableConfig.builder().build();

        var workflow = AgenticWorkflowWithSubgraphInterruption.builder()
                            .checkpointSaver(saver)
                            .build();
        console.printf( "\n%s\n", workflow.getGraph( GraphRepresentation.Type.MERMAID, "Manage SubGraph Interruption", false).content());
        var input = GraphInput.args(Map.of());

        do {
            try {
                for (var output : workflow.stream(input, runnableConfig)) {

                    console.format("output: %s\n", output);
                }

                break;
            }
            catch( Exception ex ) {
                var interruptException = AgenticWorkflowWithSubgraphInterruption.SubGraphInterruptionException.from(ex);
                if( interruptException.isPresent() ) {

                    console.format("SubGraphInterruptionException: %s\n", interruptException.get().getMessage());
                    var interruptionState = interruptException.get().state;


                    // ==== METHOD 1 =====
                    // FIND NODE BEFORE SUBGRAPH AND RESUME
                    /*
                    StateSnapshot<?> lastNodeBeforeSubGraph = workflow.getStateHistory(runnableConfig).stream()
                                                                .skip(1)
                                                                .findFirst()
                                                                .orElseThrow( () -> new IllegalStateException("lastNodeBeforeSubGraph is null"));
                    var nodeBeforeSubgraph = lastNodeBeforeSubGraph.node();
                    runnableConfig = workflow.updateState( lastNodeBeforeSubGraph.config(), interruptionState );
                    */

                    // ===== METHOD 2 =======
                    // UPDATE STATE ASSUMING TO BE ON NODE BEFORE SUBGRAPH ('NODE2') AND RESUME
                    var nodeBeforeSubgraph = "NODE2";
                    runnableConfig = workflow.updateState( runnableConfig, interruptionState, nodeBeforeSubgraph );
                    input = GraphInput.resume();

                    console.format( "RESUME GRAPH FROM END OF NODE: %s\n", nodeBeforeSubgraph);
                    continue;
                }

                throw ex;
            }
        } while( true );


    }

}