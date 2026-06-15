package org.bsc.langgraph4j.lc4j.skills;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.skills.ClassPathSkillLoader;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.agentexecutor.AgentExecutor;
import org.bsc.langgraph4j.agentexecutor.AgentExecutorEx;
import org.bsc.langgraph4j.checkpoint.FileSystemSaver;

import java.nio.file.Path;
import java.util.Map;

public class PlantUMLSkill {

    public static void main( String[] args ) throws Exception {

        final var skills = ClassPathSkillLoader.loadSkills("skills");

        final var model = AiStreamingModel.OLLAMA.chatModel( "qwen3.5" );

        final var saver = new FileSystemSaver(
                Path.of("target", "PlantUMLSkill"),
                AgentExecutorEx.Serializers.JSON.object() );

        final var graph = AgentExecutor.builder()
                .chatModel(model, true)
                .systemMessage(SystemMessage.from("You are my developer assistant" ))
                //.toolsFromObject(tools)
                .skills(skills)
                .build()
                .compile(CompileConfig.builder()
                        .checkpointSaver(saver)
                        .releaseThread(true)
                        .build());

        final var config = RunnableConfig.builder().threadId("T1").build();

        final var userMessage = UserMessage.from("""
        Generate a simple use case diagram using markdown format
        list your available kills
        """);

        final var result = graph.stream(
                        GraphInput.args( Map.of("messages", userMessage ) ), config)
                .stream()
                .reduce( (a,b) -> b);

        System.out.printf("Final result: %s%n", result);
    }
}
