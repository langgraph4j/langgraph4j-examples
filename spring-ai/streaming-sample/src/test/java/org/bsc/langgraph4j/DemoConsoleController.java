package org.bsc.langgraph4j;

import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.action.InterruptionMetadata;
import org.bsc.langgraph4j.agent.AgentEx;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.streaming.StreamingOutput;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

/**
 * Demonstrates the use of Spring Boot CLI to execute a task using an agent executor.
 */
@Controller
public class DemoConsoleController implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DemoConsoleController.class);

    private final ChatModel chatModel;

    public DemoConsoleController( ChatModel chatModel ) {

        this.chatModel = chatModel;
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

        log.info("Welcome to the Spring Boot CLI application!");

        var console = System.console();

        var userMessage = """
        perform test twice with message 'this is a test' and reports their results and also number of current active threads
        """;

        var streaming = false;

        runAgent( userMessage, streaming, console  );

        // runAgentWithApproval( userMessage, streaming, console  );
    }

    public void runAgentWithApproval( String userMessage, boolean streaming, java.io.Console console ) throws Exception {

        var saver = new MemorySaver();

        var compileConfig = CompileConfig.builder()
                .checkpointSaver(saver)
                .build();

        var agent = AgentExecutorEx.builder()
                .chatModel(chatModel, streaming)
                .toolsFromObject( new TestTools()) // Support without providing tools
                .approvalOn( "execTest", ( nodeId, state ) ->
                        InterruptionMetadata.builder( nodeId, state )
                                .addMetadata( "label", "confirm execution of test?")
                                .build())
                .build()
                .compile(compileConfig);

        log.info( "{}", agent.getGraph( GraphRepresentation.Type.MERMAID, "ReAct Agent", false));

        Map<String,Object> input = Map.of("messages", new UserMessage(userMessage) );

        var runnableConfig = RunnableConfig.builder().build();

        while( true ) {
            var result = agent.stream(input, runnableConfig );

            var output = result.stream()
                    .peek(s -> {
                        if (s instanceof StreamingOutput<?> out) {
                            System.out.printf("%s: (%s)\n", out.node(), out.chunk());
                        } else {
                            System.out.println(s.node());
                        }
                    })
                    .reduce((a, b) -> b)
                    .orElseThrow();

            if (output.isEND()) {
                console.format( "result: %s\n",
                        output.state().lastMessage()
                                .map(AssistantMessage.class::cast)
                                .map(AssistantMessage::getText)
                                .orElseThrow());
                break;

            } else {

                var returnValue = AsyncGenerator.resultValue(result);

                if( returnValue.isPresent() ) {

                    log.info("interrupted: {}", returnValue.orElse("NO RESULT FOUND!"));

                    if (returnValue.get() instanceof InterruptionMetadata<?> interruption) {

                        var answer = console.readLine(format("%s : (N\\y) \t\n", interruption.metadata("label").orElse("Approve action ?")));

                        if (Objects.equals(answer, "Y") || Objects.equals(answer, "y")) {
                            runnableConfig = agent.updateState(runnableConfig, Map.of(AgentEx.APPROVAL_RESULT_PROPERTY, AgentEx.ApprovalState.APPROVED.name()));
                        } else {
                            runnableConfig = agent.updateState(runnableConfig, Map.of(AgentEx.APPROVAL_RESULT_PROPERTY, AgentEx.ApprovalState.REJECTED.name()));
                        }
                    }
                    input = null;
                }

            }

        }
    }

    public void runAgent( String userMessage, boolean streaming, java.io.Console console ) throws Exception {

        var saver = new MemorySaver();

        var compileConfig = CompileConfig.builder()
                .checkpointSaver(saver)
                .build();

        var agentBuilder =  AgentExecutor.builder()
                .chatModel(chatModel, streaming);

        // FIX for GEMINI MODEL
        if( chatModel instanceof VertexAiGeminiChatModel ) {
            agentBuilder
//                .defaultSystem( """
//                When call tools, You must only output the function or tool to call, using strict JSON.
//                Do not output commentary or internal thoughts.
//                """)
                .toolsFromObject( new TestTools4Gemini());
        }
        else {
            agentBuilder.toolsFromObject( new TestTools());
        }

        var agent = agentBuilder.build().compile(compileConfig);

        log.info( "{}", agent.getGraph( GraphRepresentation.Type.MERMAID, "ReAct Agent", false));

        Map<String,Object> input = Map.of("messages", new UserMessage(userMessage) );
        var runnableConfig = RunnableConfig.builder().build();

        var result = agent.stream(input, runnableConfig );

        var output = result.stream()
                .peek(s -> {
                    if (s instanceof StreamingOutput<?> out) {
                        System.out.printf("%s: (%s)\n", out.node(), out.chunk());
                    } else {
                        System.out.println(s.node());
                    }
                })
                .reduce((a, b) -> b)
                .orElseThrow();

        console.format( "result: %s\n",
                output.state().lastMessage()
                        .map(AssistantMessage.class::cast)
                        .map(AssistantMessage::getText)
                        .orElseThrow());

    }

}