package org.bsc.langgraph4j.agent.langgraph;


import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.Map;

public final class QAState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "question", Channels.base(() -> ""),
            "country", Channels.base(() -> ""),
            "city", Channels.base(() -> ""),
            "messages", Channels.base(() -> "")
    );

/*
    public QAState() {
        super(Map.of(
                "question", "",
                "country", "",
                "city", "",
                "messages", ""
        ));
    }
*/

    public QAState(Map<String, Object> data) {
        super(data);
    }

    public String question() {
        return this.<String>value("question").orElse("");
    }

    public String country() {
        return this.<String>value("country").orElse("");
    }

    public String city() {
        return this.<String>value("city").orElse("");
    }

    public String messages() {
        return this.<String>value("messages").orElse("");
    }
}