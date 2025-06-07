package org.bsc.langgraph4j.mcp;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Demonstrates the use of Spring Boot CLI to execute a task using an agent executor.
 */
@Component
@Profile("console")
public class LG4jMCPClientConsole implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LG4jMCPClientConsole.class);

    private final LG4jMCPClientService agentService;

    public LG4jMCPClientConsole(LG4jMCPClientService agentService) {
        this.agentService = agentService;
    }

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
        System.out.println( "RUN" );

        var agentWorkflow = agentService.getAgentWorkflow();

        var agent =  agentWorkflow.compile();

        var userMessage = UserMessage.builder()
                            .text("get all issues names and project")
                            .build();
        var finalState = agent.invoke( Map.of( "messages", userMessage));

        System.out.println( finalState );

    }
}