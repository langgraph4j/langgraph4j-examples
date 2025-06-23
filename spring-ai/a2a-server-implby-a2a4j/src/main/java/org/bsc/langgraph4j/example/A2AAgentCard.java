package org.bsc.langgraph4j.example;

import io.github.a2ap.core.model.AgentCapabilities;
import io.github.a2ap.core.model.AgentCard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lambochen
 */
@Configuration
public class A2AAgentCard {

    @Bean
    public AgentCard agentCard() {
        AgentCard agentCard = new AgentCard();
        agentCard.setId("a2a-server");
        agentCard.setName("A2A Server");
        agentCard.setDescription("A sample A2A agent implemented by LangGraph4j");
        agentCard.setUrl("http://127.0.0.1:8080/a2a/server");
        agentCard.setVersion("1.0.0");

        AgentCapabilities agentCapabilities = new AgentCapabilities();
        agentCapabilities.setStreaming(true);
        agentCapabilities.setPushNotifications(false);
        agentCapabilities.setStateTransitionHistory(true);
        agentCard.setCapabilities(agentCapabilities);
        return agentCard;
    }

}
