package org.bsc.langgraph4j.example;

import io.github.a2ap.core.model.Message;
import io.github.a2ap.core.model.Part;
import io.github.a2ap.core.model.RequestContext;
import io.github.a2ap.core.model.TextPart;
import io.github.a2ap.core.server.AgentExecutor;
import io.github.a2ap.core.server.EventQueue;
import org.springframework.ai.content.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * @author lambochen
 */
@Component
public class A2AHandler implements AgentExecutor {

    @Autowired
    private Agent agent;

    @Override
    public Mono<Void> execute(RequestContext context, EventQueue eventQueue) {
        List<Part> parts = context.getRequest().getMessage().getParts();
        TextPart textPart = (TextPart) parts.get(0);

        // agent execute
        var response = agent.execute(textPart.getText());
        var responseMessage = response.lastMessage().map(Content::getText).orElse(null);

        return Mono.fromRunnable(() -> {
            // build message to return
            Message message = Message.builder().role("assistant")
                    .messageId(UUID.randomUUID().toString())
                    .kind("message")
                    .parts(List.of(TextPart.builder().text(responseMessage).build()))
                    .build();

            // push message
            eventQueue.enqueueEvent(message);
            eventQueue.close();
        }).then();
    }

    @Override
    public Mono<Void> cancel(String taskId) {
        // TODO do something
        return Mono.empty();
    }
}
