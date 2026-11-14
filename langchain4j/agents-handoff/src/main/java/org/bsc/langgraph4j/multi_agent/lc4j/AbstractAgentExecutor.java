package org.bsc.langgraph4j.multi_agent.lc4j;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.tool.ToolExecutor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.agentexecutor.AgentExecutor;

import java.util.Map;

public abstract class AbstractAgentExecutor<B extends AbstractAgentExecutor.Builder<B>> extends AbstractAgent<B> {

    public static abstract class Builder<B extends AbstractAgentExecutor.Builder<B>> extends AbstractAgent.Builder<B> {

        final AgentExecutor.Builder delegate = AgentExecutor.builder();

        public B chatModel(ChatModel model) {
            delegate.chatModel(model);
            return result();
        }

        public B tool(Map.Entry<ToolSpecification, ToolExecutor> entry) {
            delegate.tool(entry);
            return result();
        }

        public B toolFromObject( Object objectWithTools ) {
            delegate.toolsFromObject(objectWithTools);
            return result();
        }

        public B systemMessage(SystemMessage message) {
            delegate.systemMessage(message);
            return result();
        }
    }

    private final CompiledGraph<AgentExecutor.State> agentExecutor;

    public AbstractAgentExecutor( Builder<B> builder ) throws GraphStateException {
        super( builder );

        agentExecutor = builder.delegate.build().compile();
    }

    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object o) {

        final var userMessage = UserMessage.from( toolExecutionRequest.arguments() );

        final var result = agentExecutor.invokeSync(
                GraphInput.args(Map.of( "messages", userMessage )),
                RunnableConfig.empty() );

        return result.<AgentExecutor.State>asState().finalResponse()
                .orElseThrow( () -> new Error("No final response found in the state!"));
    }

}
