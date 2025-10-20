package dev.langchain4j.studio;

import dev.langchain4j.DotEnvConfig;
import dev.langchain4j.adaptiverag.AdaptiveRag;
import jakarta.servlet.DispatcherType;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.jetty.LangGraphStudioServer4Jetty;

import java.util.EnumSet;

public class AdaptiveRAGStudioServer {

    public static void main(String[] args) throws Exception {

        DotEnvConfig.load();

        var openApiKey = DotEnvConfig.valueOf("OPENAI_API_KEY")
                .orElseThrow( () -> new IllegalArgumentException("no OPENAI API KEY provided!"));
        var tavilyApiKey = DotEnvConfig.valueOf("TAVILY_API_KEY")
                .orElseThrow( () -> new IllegalArgumentException("no TAVILY API KEY provided!"));

        var adaptiveRagTest = new AdaptiveRag( openApiKey, tavilyApiKey);

        var app = adaptiveRagTest.buildGraph();

        // [Serializing with Jackson (JSON) - getting "No serializer found"?](https://stackoverflow.com/a/8395924/521197)
        // ObjectMapper objectMapper = new ObjectMapper();
        // objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        System.out.println (
                app.getGraph(GraphRepresentation.Type.MERMAID, "ADAPTIVE RAG EXECUTOR", false)
                        .content()
        );

        var adaptiveRAG = LangGraphStudioServer.Instance.builder()
                .title("ADAPTIVE RAG EXECUTOR")
                .addInputStringArg("question")
                .graph(app)
                .compileConfig(CompileConfig.builder()
                        .releaseThread(true)
                        .build())
                .build();


        LangGraphStudioServer4Jetty.builder()
                .port(8080)
                .instance( "adaptive_rag", adaptiveRAG )
                .build()
                .start()
                .join();


    }

}
