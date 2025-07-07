package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.Set;
import java.util.function.Supplier;

public enum ChatModels {

    OPENAI_GPT_4O_MINI( () -> OpenAiChatModel.builder()
            .apiKey( System.getenv("OPENAI_API_KEY") )
            .modelName( "gpt-4o-mini" )
            .supportedCapabilities(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA))
            .logRequests(false)
            .logResponses(false)
            .maxRetries(2)
            .temperature(0.0)
            .build() ),
    OLLAMA_QWEN2_5_7B( () -> OllamaChatModel.builder()
            .modelName( "qwen2.5:7b" )
            .baseUrl("http://localhost:11434")
            .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
            .logRequests(false)
            .logResponses(false)
            .maxRetries(2)
            .temperature(0.0)
            .build() )
    ;

    public final Supplier<ChatModel> model;

    ChatModels(Supplier<ChatModel> model ) {
        this.model = model;
    }
}
