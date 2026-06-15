package org.bsc.langgraph4j.multi_agent.springai;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientImpl;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.function.Function;

public enum AiModel {

    OPENAI( model ->
            OpenAiChatModel.builder()
                    .openAiClient( new OpenAIClientImpl(com.openai.core.ClientOptions.builder()
                            .apiKey(System.getProperty("OPENAI_API_KEY"))
                            .build()))
                    .options(OpenAiChatOptions.builder()
                            .model(model)
                            .logprobs(false)
                            .temperature(0.1)
                            .build())
                    .build()),
    OLLAMA( model ->
            OllamaChatModel.builder()
                    .ollamaApi(OllamaApi.builder().baseUrl("http://localhost:11434").build())
                    .options(OllamaChatOptions.builder()
                            .model(model)
                            .temperature(0.1)
                            .build())
                    .build())
    ;

    private final Function<String,ChatModel> model;

    public ChatModel chtModel( String model ) {
        return this.model.apply(model);
    }

    AiModel(Function<String,ChatModel> model) {
        this.model = model;
    }

}

