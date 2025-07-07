package org.bsc.langgraph4j.aiservices.lc4j;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfiguration {
    @Bean(name = "chatModel")
    public ChatModel chatModel() {
        return ChatModels.OLLAMA_QWEN2_5_7B.model.get();
    }

    @Bean(name = "streamingChatModel")
    public StreamingChatModel streamingChatModel() {
        return StreamingChatModels.OLLAMA_QWEN2_5_7B.model.get();
    }

}
