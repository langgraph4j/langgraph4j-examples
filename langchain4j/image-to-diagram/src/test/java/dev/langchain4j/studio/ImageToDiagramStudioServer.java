package dev.langchain4j.studio;

import dev.langchain4j.DotEnvConfig;
import dev.langchain4j.image_to_diagram.ImageToDiagramWorkflow;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.jetty.LangGraphStudioServer4Jetty;

public class ImageToDiagramStudioServer {


    public static void main(String[] args) throws Exception {

        DotEnvConfig.load();

        // var openApiKey = DotEnvConfig.valueOf("OPENAI_API_KEY")
        //        .orElseThrow( () -> new IllegalArgumentException("no OPENAI API KEY provided!"));

        // var imageData = ImageLoader.loadImageAsBase64( "LangChainAgents.png" );

        var agentExecutor = new ImageToDiagramWorkflow();

        var workflow = agentExecutor.workflowWithCorrection();

        System.out.println (
                workflow.getGraph(GraphRepresentation.Type.MERMAID, "Image To Diagram", false)
                        .content()
        );

        var imageToDiagram = LangGraphStudioServer.Instance.builder()
                .title("LangGraph4j Studio - Image To Diagram")
                .addInputImageArg( "imageData" )
                .graph(workflow)
                .compileConfig(CompileConfig.builder()
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
