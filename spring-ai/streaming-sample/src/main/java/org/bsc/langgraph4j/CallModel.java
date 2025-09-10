package org.bsc.langgraph4j;

import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.action.AsyncNodeActionWithConfig;
import org.bsc.langgraph4j.action.NodeActionWithConfig;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.spring.ai.generators.StreamingChatGenerator;
import org.springframework.ai.chat.messages.Message;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeActionWithConfig.node_async;

class CallModel<State extends MessagesState<Message>> implements NodeActionWithConfig<State> {

    public static <State extends MessagesState<Message>> AsyncNodeActionWithConfig<State> of(ChatService chatService, boolean streaming ) {
        return node_async(new CallModel<>(chatService, streaming));
    }

    private final ChatService chatService;
    private final boolean streaming;

    protected CallModel(ChatService chatService, boolean streaming) {
        this.chatService = chatService;
        this.streaming = streaming;
    }

    /**
     * Calls a model with the given workflow state.
     *
     * @param state The current state containing input and intermediate steps.
     * @return A map containing the outcome of the agent call, either an action or a finish.
     */
    @Override
    public Map<String, Object> apply(State state, RunnableConfig config) throws Exception {

        var messages = state.messages();

        if (messages.isEmpty()) {
            throw new IllegalArgumentException("no input provided!");
        }

        if (streaming) {
            var flux = chatService.streamingExecute(messages);

            var generator = StreamingChatGenerator.builder()
                    .startingNode("agent")
                    .startingState(state)
                    .mapResult(response -> Map.of("messages", response.getResult().getOutput()))
                    .build(flux);

            return Map.of("messages", generator);
        } else {
            var response = chatService.execute(messages);

            var output = response.getResult().getOutput();

            return Map.of("messages", output);
        }

    }

}
