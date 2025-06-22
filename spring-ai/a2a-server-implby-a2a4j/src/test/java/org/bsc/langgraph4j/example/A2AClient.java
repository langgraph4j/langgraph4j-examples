package org.bsc.langgraph4j.example;

import io.github.a2ap.core.client.impl.DefaultA2AClient;
import io.github.a2ap.core.client.impl.HttpCardResolver;
import io.github.a2ap.core.model.Message;
import io.github.a2ap.core.model.MessageSendParams;
import io.github.a2ap.core.model.TextPart;

import java.util.List;

/**
 * @author lambochen
 */
public class A2AClient {

    private final io.github.a2ap.core.client.A2AClient remoteAgent = new DefaultA2AClient(
            new HttpCardResolver("http://127.0.0.1:8080")
    );

    public static void main(String[] args) {
        A2AClient a2AClient = new A2AClient();

        Message messageParam = Message.builder().role("user")
                .parts(List.of(TextPart.builder().text(
                        "introduce LangGraph4j"
                ).build()))
                .build();

        var resp = a2AClient.remoteAgent.sendMessage(MessageSendParams.builder().message(messageParam).build());
        System.out.println(resp);
    }
}
