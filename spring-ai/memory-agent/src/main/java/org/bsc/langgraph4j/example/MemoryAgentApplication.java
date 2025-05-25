package org.bsc.langgraph4j.example;


import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Content;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@SpringBootApplication
public class MemoryAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoryAgentApplication.class, args);
    }

    @Bean
    CommandLineRunner memoryChatbot(ChatModel llm) {
        return args -> {
            var workflow = new MemoryAgent(llm).workflow();

            var conversationId = "conversation-1";

            // conversation-1: step 1
            var runnableConfig =  RunnableConfig.builder().threadId(conversationId).build();
            var state = workflow.invoke(
                    Map.of("messages", new UserMessage("Hi，I'm LangGraph4j!")),
                    runnableConfig
            ).orElseThrow();

            System.out.println(
                    state.lastMessage().map(Content::getText).orElse("UNKNOWN")
            ); // It can be seen here that the historical messages have been stored

            // conversation-1: step 2
            state = workflow.invoke(
                    Map.of("messages", new UserMessage("Who am I？")),
                    runnableConfig
            ).orElseThrow();

            System.out.println(
                    state.lastMessage().map(Content::getText).orElse("UNKNOWN")
            ); // It can be seen here that the historical messages have been stored

            // other conversation
            conversationId = "conversation-2";
            runnableConfig =  RunnableConfig.builder().threadId(conversationId).build();
            state = workflow.invoke(
                    Map.of("messages", new UserMessage("Do you know my name?")),
                    runnableConfig
            ).orElseThrow();

            System.out.println(
                    state.lastMessage().map(Content::getText).orElse("UNKNOWN")
            ); // As can be seen here, since it is a new conversation, there are no historical messages
        };
    }

    /**
     * conversation-1:
     * {messages=[UserMessage{content='Hi，I'm LangGraph4j!', properties={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=Hello, LangGraph4j! How can I assist you today?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, annotations=[], index=0, id=chatcmpl-BadnH9kFrk8LWxG17bgfqjXHzf7MX}]]}
     * {messages=[UserMessage{content='Hi，I'm LangGraph4j!', properties={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=Hello, LangGraph4j! How can I assist you today?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, annotations=[], index=0, id=chatcmpl-BadnH9kFrk8LWxG17bgfqjXHzf7MX}], UserMessage{content='Who am I？', properties={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=You referred to yourself as LangGraph4j, which suggests you might be related to a graph-based framework or library, possibly for programming or data analysis. However, without more context, it’s hard to say exactly who you are. Can you provide more details?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, annotations=[], index=0, id=chatcmpl-BadnH1LyTJNzfCcuNPDQLW8aKqHem}]]}
     *
     * conversation-2:
     * {messages=[UserMessage{content='Do you know my name?', properties={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=No, I don't know your name. If you'd like to share it or have any other questions, feel free to let me know!, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, annotations=[], index=0, id=chatcmpl-BadnIw55YUhuyAkW5I2KMs5XqCey7}]]}
     */
}