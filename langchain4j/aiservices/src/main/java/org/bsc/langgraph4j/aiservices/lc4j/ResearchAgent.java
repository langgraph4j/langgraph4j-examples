package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ResearchAgent implements NodeAction<State> {
    static class Tools {

        @Tool("""
        Use this to perform a research over internet
        """)
        String search(@P("internet query") String query) {
//            log.info( "search query: '{}'", query );
            return """
            the games will be in Italy at Cortina '2026
            """;
        }
    }

    interface Service {
        String search(@dev.langchain4j.service.UserMessage  String query);
    }

    final Service service;

    public ResearchAgent(@Qualifier("chatModel") ChatModel model ) {
        service = AiServices.builder( Service.class )
                .chatModel(model)
                .tools( new Tools() )
                .build();
    }
    @Override
    public Map<String, Object> apply(State state) throws Exception {
        var message = state.lastMessage().orElseThrow();
        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };
        var result = service.search( text );
        return Map.of( "messages", AiMessage.from(result) );

    }
}