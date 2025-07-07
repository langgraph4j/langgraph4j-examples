package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CoderAgent implements AsyncNodeAction<State> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoderAgent.class);

    static class Tools {

        @Tool("""
            Use this to execute java code and do math. If you want to see the output of a value,
            you should print it out with `System.out.println(...);`. This is visible to the user.""")
        String search(@P("coder request") String request) {
//            log.info( "CoderTool request: '{}'", request );
            return """
            3
            """;
        }
    }

    interface Service {
        TokenStream evaluate(@dev.langchain4j.service.UserMessage String code);
    }

    final Service service;

    public CoderAgent(@Qualifier("streamingChatModel") StreamingChatModel model){
        service = AiServices.builder( Service.class )
                .streamingChatModel(model)
                .tools( new Tools() )
                .build();
    }
    @Override
    public CompletableFuture<Map<String, Object>> apply(State state)  {
        var message = state.lastMessage().orElseThrow();
        var text = switch( message.type() ) {
            case USER -> ((UserMessage)message).singleText();
            case AI -> ((AiMessage)message).text();
            default -> throw new IllegalStateException("unexpected message type: " + message.type() );
        };

        var result = new CompletableFuture<Map<String, Object>>();

        TokenStream tokenStream = service.evaluate( text );
        StringBuilder stringBuilder = new StringBuilder();
        tokenStream
            .onPartialResponse((String partialResponse) -> {
                log.info( "{}", partialResponse );
            }).onToolExecuted((ToolExecution toolExecution) -> {
                log.info("""
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                {}
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                {}
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """,
                toolExecution.request().name(), toolExecution.result());
            }).onCompleteResponse((ChatResponse r) -> {
                log.info( "{}\n{} ", r.metadata().toString(), r.toString());
                stringBuilder.append(r.aiMessage().text());
                result.complete( Map.of( "messages", AiMessage.from(stringBuilder.toString()) ) );
            }).onError((Throwable error) -> {
                log.error( "{}", error.getMessage(), error );
                result.completeExceptionally(error);
            }).start();

        return result;
    }
}