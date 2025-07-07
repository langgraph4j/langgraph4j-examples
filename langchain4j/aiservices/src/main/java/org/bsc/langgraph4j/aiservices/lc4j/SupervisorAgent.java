package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Service
public class SupervisorAgent implements NodeAction<State> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SupervisorAgent.class);

    static class Router {
        @Description("Worker to route to next. If no workers needed, route to FINISH.")
        String next;

        @Override
        public String toString() {
            return format( "Router[next: %s]",next);
        }
    }

    interface Service {
        @SystemMessage( """
                You are a supervisor tasked with managing a conversation between the following workers: {{members}}.
                Given the following user request, respond with the worker to act next.
                Each worker will perform a task and respond with their results and status.
                When finished, respond with FINISH.
                """)
        Router evaluate(@V("members") String members, @dev.langchain4j.service.UserMessage String userMessage);
    }

    final Service service;
    public final String[] members = {"researcher", "coder", "FINISH" };

    public SupervisorAgent(@Qualifier("chatModel") ChatModel model ) {

        service = AiServices.create( Service.class, model );
    }

    @Override
    public Map<String, Object> apply(State state) throws Exception {

        var message = state.lastMessage().orElseThrow();

        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };

        var m = String.join(",", members);

        var result = service.evaluate( m, text );

        log.info("Supervisor Result: {}", result );

        return Map.of( "next", result.next );
    }
}