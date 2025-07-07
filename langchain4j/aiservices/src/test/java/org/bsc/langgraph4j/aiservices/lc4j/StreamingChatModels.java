package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.function.Supplier;

public enum StreamingChatModels {

    OPENAI_GPT_4O_MINI( () -> OpenAiStreamingChatModel.builder()
            .apiKey( System.getenv("OPENAI_API_KEY") )
            .modelName( "gpt-4o-mini" )
            .logRequests(false)
            .logResponses(false)
            .temperature(0.0)
            .build() ),
    OLLAMA_QWEN2_5_7B( () -> OllamaStreamingChatModel.builder()
            .modelName( "qwen2.5:7b" )
            .baseUrl("http://localhost:11434")
            .logRequests(false)
            .logResponses(false)
            .temperature(0.0)
            .build() )
    ;

    public final Supplier<StreamingChatModel> model;

    StreamingChatModels(Supplier<StreamingChatModel> model ) {
        this.model = model;
    }
}
