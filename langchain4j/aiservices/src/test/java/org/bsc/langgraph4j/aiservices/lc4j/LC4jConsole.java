package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Demonstrates the use of Spring Boot CLI to execute a task using an agent executor.
 */
@Component
public class LC4jConsole implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LC4jConsole.class);

    private final StateGraph<State> agentWorkflow;

    public LC4jConsole(StateGraph<State> agentWorkflow) {
        this.agentWorkflow = agentWorkflow;
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
        System.out.println("RUN");

        var agent = agentWorkflow.compile();

        var userMessage = UserMessage.from("what are the result of 1 + 2 ?");
        var finalState = agent.invoke(Map.of("messages", userMessage));

        System.out.println(finalState);

    }
}
