package dev.langchain4j.adaptiverag;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Router for user queries to the most relevant datasource.
 */
public record QuestionRouter( ChatModel chatModel ) implements Function<String, QuestionRouter.Type> {

    public QuestionRouter {
        requireNonNull(chatModel, "chatModel must not be null");
    }

    public enum Type {
        vectorstore,
        web_search
    }
    /**
    * Route a user query to the most relevant datasource.
    */
    static class Result {

        @Description("Given a user question choose to route it to web search or a vectorstore.")
        Type datasource;
    }


    /**
     * Provides a service interface for processing user questions.
     * 
     * @SystemMessage "You are an expert at routing a user question to a vectorstore or web search. The vectorstore contains documents related to agents, prompt engineering, and adversarial attacks. Use the vectorstore for questions on these topics. Otherwise, use web-search."
     */
    interface Service {

        /**
         * Invokes the system with a user's question.
         * 
         * This method is designed to route a user's query to either a vectorstore or a web search,
         * based on the content of the query. The vectorstore houses documents related to agents, prompt
         * engineering, and adversarial attacks. If the query pertains to any of these topics, it will be
         * directed to the vectorstore. For other types of questions, a web search will be initiated.
         * 
         * @param question The user's input query that needs to be routed or searched for.
         * @return A Result object containing the response from either the vectorstore or web search.
         */
        @SystemMessage("You are an expert at routing a user question to a vectorstore or web search.\n" +
                "The vectorstore contains documents related to agents, prompt engineering, and adversarial attacks.\n" +
                "Use the vectorstore for questions on these topics. Otherwise, use web-search.")
        Result invoke(String question);
    }

    /**
     * Applies a given question to an AI model and extracts the data source from the response.
     * @param question The question to be applied to the AI model.
     * @return The extracted data source from the model's response.
     */
    @Override
    public Type apply(String question) {

        Service extractor = AiServices.create(Service.class, chatModel);

        Result ds = extractor.invoke(question);
        return ds.datasource;

    }

}