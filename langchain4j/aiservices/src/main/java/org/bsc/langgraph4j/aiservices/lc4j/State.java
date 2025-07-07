package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.data.message.ChatMessage;
import org.bsc.langgraph4j.langchain4j.serializer.std.LC4jStateSerializer;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.serializer.StateSerializer;

import java.util.Map;
import java.util.Optional;

public class State extends MessagesState<ChatMessage> {
    public Optional<String> next() {
        return this.value("next");
    }

    public State(Map<String, Object> initData) {
        super( initData  );
    }

    public static StateSerializer<State> serializer() {
        return new LC4jStateSerializer<>( State::new );
    }
}