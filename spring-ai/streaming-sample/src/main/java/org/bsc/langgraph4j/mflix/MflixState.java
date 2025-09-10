package org.bsc.langgraph4j.mflix;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.Map;
import java.util.ArrayList;

public class MflixState extends AgentState {

    public static final String USER_QUERY_KEY = "user_query";
    public static final String RESPONDER = "responder";
    public static final String RESPONSE_STREAM = "response_stream";
    public static final String MESSAGES = "messages";

    private static Object ArrayList;
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            USER_QUERY_KEY, Channels.<String>base((arg0, query) -> query),
            RESPONDER, Channels.<String>base((arg0, responder) -> responder),
            RESPONSE_STREAM, Channels.base((arg0, obj) -> obj),
            MESSAGES, Channels.<String>appender(ArrayList::new)
    );

    public MflixState(Map<String, Object> initData) {
        super(initData);
    }
}