package org.bsc.langgraph4j;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.agent.Agent;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.spring.ai.serializer.std.SpringAIStateSerializer;
import org.bsc.langgraph4j.spring.ai.tool.SpringAIToolService;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Represents the core component responsible for executing agent logic.
 * It includes methods for building and managing the execution graph,
 * as well as handling agent actions and state transitions.
 *
 * @author lambochen
 */
public interface AgentExecutor {

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AgentExecutor.class);

    /**
     * Class responsible for building a state graph.
     */
    class Builder extends AgentExecutorBuilder<Builder,State> {

        /**
         * Builds and returns a StateGraph with the specified configuration.
         * Initializes the stateSerializer if it's null. Then, constructs a new StateGraph object using the provided schema
         * and serializer, adds an initial edge from the START node to "agent", and then proceeds to add nodes for "agent" and
         * "action". It also sets up conditional edges from the "agent" node based on whether or not to continue.
         *
         * @return A configured StateGraph object.
         * @throws GraphStateException If there is an issue with building the graph state.
         */
        public StateGraph<State> build() throws GraphStateException {

            if (stateSerializer == null) {
                stateSerializer =  new SpringAIStateSerializer<>(AgentExecutor.State::new);
            }

            final var chatService = new ChatService(this);

            final var toolService = new SpringAIToolService(tools);

            return Agent.<Message,State>builder()
                    .stateSerializer(stateSerializer)
                    .schema( State.SCHEMA )
                    .callModelAction( CallModel.of( chatService, streaming ))
                    .executeToolsAction( (state, config ) ->
                            executeTools(state, toolService))
                    .shouldContinueEdge(AgentExecutor::shouldContinue)
                    .build();

        }
    }

    /**
     * Returns a new instance of {@link Builder}.
     *
     * @return a new {@link Builder} object
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Represents the state of an agent in a system.
     * This class extends {@link AgentState} and defines constants for keys related to input, agent outcome,
     * and intermediate steps. It includes a static map schema that specifies how these keys should be handled.
     */
    class State extends MessagesState<Message> {

        /**
         * Constructs a new State object using the initial data provided in the initData map.
         *
         * @param initData the map containing the initial settings for this state
         */
        public State(Map<String, Object> initData) {
            super(initData);
        }

    }

    /**
     * Executes tools based on the provided state.
     *
     * @param state The current state containing necessary information to execute tools.
     * @return A CompletableFuture containing a map with the intermediate steps, if successful. If there is no agent outcome or the tool service execution fails, an appropriate exception will be thrown.
     */
    private static CompletableFuture<Map<String, Object>> executeTools(State state, SpringAIToolService toolService) {
        log.trace("executeTools");

        var futureResult = new CompletableFuture<Map<String, Object>>();

        var message = state.lastMessage();

        if (message.isEmpty()) {
            futureResult.completeExceptionally(new IllegalArgumentException("no input provided!"));
        } else if (message.get() instanceof AssistantMessage assistantMessage) {
            if (assistantMessage.hasToolCalls()) {

                return toolService.executeFunctions(assistantMessage.getToolCalls())
                        .thenApply(result -> Map.of("messages", result));

            }
        } else {
            futureResult.completeExceptionally(new IllegalArgumentException("no AssistantMessage provided!"));
        }

        return futureResult;

    }

    /**
     * Determines whether the game should continue based on the current state.
     *
     * @param state The current state of the game.
     * @return "end" if the game should end, otherwise "continue".
     */
    private static CompletableFuture<Command> shouldContinue(State state, RunnableConfig config) {

        var message = state.lastMessage().orElseThrow();

        var finishReason = message.getMetadata().getOrDefault("finishReason", "");

        if (message instanceof AssistantMessage assistantMessage) {
            if (assistantMessage.hasToolCalls()) {
                return completedFuture(new Command(Agent.CONTINUE_LABEL ));
            }
        }

        if (Objects.equals( Objects.toString(finishReason), "STOP")) {
            return completedFuture(new Command(Agent.END_LABEL ));
        }

        return completedFuture(new Command(Agent.END_LABEL ));
    }
}