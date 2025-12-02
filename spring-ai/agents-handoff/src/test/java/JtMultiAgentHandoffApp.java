//DEPS org.bsc.langgraph4j:langgraph4j-springai-agent-handoff:1.0-SNAPSHOT
//DEPS org.springframework.ai:spring-ai-bom:1.1.0@pom
//DEPS org.springframework.ai:spring-ai-client-chat
//DEPS org.springframework.ai:spring-ai-openai
//DEPS org.springframework.ai:spring-ai-ollama
//SOURCES org/bsc/langgraph4j/multi_agent/springai/AiModel.java
//SOURCES org/bsc/langgraph4j/multi_agent/springai/AgentMarketplace.java
//SOURCES org/bsc/langgraph4j/multi_agent/springai/AgentPayment.java

import io.javelit.core.Jt;
import io.javelit.core.JtComponent;
import org.bsc.langgraph4j.multi_agent.springai.AgentHandoff;
import org.bsc.langgraph4j.multi_agent.springai.AgentMarketplace;
import org.bsc.langgraph4j.multi_agent.springai.AgentPayment;
import org.bsc.langgraph4j.multi_agent.springai.AiModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Content;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Javelite App
 *
 *
 */
public class JtMultiAgentHandoffApp {


    public static void main(String[] args) {

        var app = new JtMultiAgentHandoffApp();

        app.view();
    }

    public void view() {

        Jt.title("LangGraph4J Multi Agent Handoff").use();
        Jt.markdown("### Powered by LangGraph4j and SpringAI").use();

        var selectModelCols = Jt.columns(2).key("select-model-cols").use();

        boolean cloud = Jt.toggle("Select Cloud/Local Model").use(selectModelCols.col(0));
        Jt.markdown(cloud ? "*Cloud*" : "*Local*").use(selectModelCols.col(1));

        ChatModel chatModel = null;
        if (cloud) {
            var cloudModelCols = Jt.columns(2).key("cloud-model-cols").use();
            var model = Jt.radio("Available models",
                    List.of("gpt-4o-mini")).use(cloudModelCols.col(0));
            var apikey = Jt.textInput("API KEY:")
                    .type("password")
                    .labelVisibility(JtComponent.LabelVisibility.HIDDEN)
                    .placeholder("api key")
                    .width(600)
                    .use(cloudModelCols.col(1));
            if (apikey == null) {
                Jt.error("API KEY cannot be null").use();
            } else {
                System.setProperty("OPENAI_API_KEY", apikey);
            }
            chatModel = AiModel.OPENAI.chtModel(model);
        } else {
            var model = Jt.radio("Available models",
                    List.of("qwen2.5:7b", "qwen3:8b", "gpt-oss:20b")).use();
            chatModel = AiModel.OLLAMA.chtModel(model);
        }

        Jt.divider("hr1").use();

        if (chatModel != null) {

            var input = Jt.textArea("input")
                    .value("search for product 'X' and purchase it with IBAN US82WEST1234567890123456")
                    .placeholder("input")
                    .labelVisibility(JtComponent.LabelVisibility.HIDDEN)
                    .use();
            if( Jt.button("Start").disabled(input.isEmpty()).use() ) {

                var outputComponent = Jt.empty().key("step").use();

                try {
                    var agentMarketPlace = AgentMarketplace.builder()
                            .chatModel(chatModel)
                            .build();

                    var agentPayment = AgentPayment.builder()
                            .chatModel(chatModel)
                            .build();

                    var handoffExecutor = AgentHandoff.builder()
                            .chatModel(chatModel)
                            .agent(agentMarketPlace)
                            .agent(agentPayment)
                            .build()
                            .compile();

                    var generator = handoffExecutor.stream( Map.of("messages", new UserMessage(input) ) );

                    Jt.info( "starting handoff agentic workflow").use( outputComponent );

                    for( var step : generator ) {

                        Jt.info("""
                                step [%s]
                                
                                %s
                                """.formatted( step.node(),
                                step.state().messages().stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining("\n\n")))
                        ).use( outputComponent );

                        if( step.isEND() ) {
                            var response = step.state().lastMessage()
                                    .map(Content::getText)
                                    .orElse("No response found");
                            Jt.success(response).use();
                        }
                    }


                } catch (Throwable ex) {
                    Jt.error(ex.getMessage()).use();
                }
            }
        }
    }
}