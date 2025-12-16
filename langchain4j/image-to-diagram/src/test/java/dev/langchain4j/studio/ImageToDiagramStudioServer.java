package dev.langchain4j.studio;

import dev.langchain4j.DotEnvConfig;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.image_to_diagram.ImageToDiagramWorkflow;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.jetty.LangGraphStudioServer4Jetty;

public class ImageToDiagramStudioServer {


    public static void main(String[] args) throws Exception {

        DotEnvConfig.load();

        var agentExecutor = new ImageToDiagramWorkflow();

        //var workflow = agentExecutor.workflowWithCorrection();
        var workflow = agentExecutor.workflow();

        System.out.println (
                workflow.getGraph(GraphRepresentation.Type.MERMAID, "Image To Diagram", false)
                        .content()
        );

        var imageToDiagram = LangGraphStudioServer.Instance.builder()
                .title("LangGraph4j Studio - Image To Diagram")
                .addInputImageArg( "imageData", true, ( base64value ) ->
                        ImageContent.from( (String)base64value, "image/png", ImageContent.DetailLevel.AUTO) )
                .graph(workflow)
                .compileConfig(CompileConfig.builder()
                        .checkpointSaver( new MemorySaver() )
                        .releaseThread(true)
                        .build())
                .build();

        var server = LangGraphStudioServer4Jetty.builder()
                .port(8080)
                .instance("image_to_diagram", imageToDiagram)
                .build();

        server.start().join();

    }

}
