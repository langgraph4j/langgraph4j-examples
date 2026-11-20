package dev.langchain4j.adaptiverag;

import dev.langchain4j.DotEnvConfig;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.bsc.langgraph4j.GraphRepresentation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.List;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdaptiveRagTest {

    @BeforeAll
    public static void beforeAll() throws Exception {
        FileInputStream configFile = new FileInputStream("logging.properties");
        LogManager.getLogManager().readConfiguration(configFile);

        DotEnvConfig.load();
    }

    private final ChatModel chatModel = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName( "qwen3.5" )
            .timeout(Duration.ofMinutes(2))
            .logRequests(true)
                .logResponses(true)
                .maxRetries(2)
                .temperature(0.0)
                .build();

    String getTavilyApiKey() {
        return DotEnvConfig.valueOf("TAVILY_API_KEY")
                .orElseThrow( () -> new IllegalArgumentException("no TAVILY APIKEY provided!"));
    }

    @Test
    public void QuestionRewriterTest() {

        String result = (new QuestionRewriter(chatModel)).apply("agent memory");
        assertEquals("What is the role of memory in an agent's functioning?", result);
    }

    @Test
    public void RetrievalGraderTest() {

        String openApiKey = DotEnvConfig.valueOf("OPENAI_API_KEY")
                .orElseThrow( () -> new IllegalArgumentException("no APIKEY provided!"));

        RetrievalGrader grader = new RetrievalGrader(chatModel);

        ChromaEmbeddingStore chroma = ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8000")
                .collectionName("rag-chroma")
                .timeout(Duration.ofMinutes(2))
                .logRequests(true)
                .logResponses(true)
                .build();
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openApiKey)
                .build();

        String question = "agent memory";
        Embedding queryEmbedding = embeddingModel.embed(question).content();

        EmbeddingSearchRequest query = EmbeddingSearchRequest.builder()
                .queryEmbedding( queryEmbedding )
                .maxResults( 1 )
                .minScore( 0.0 )
                .build();
        EmbeddingSearchResult<TextSegment> relevant = chroma.search( query );

        List<EmbeddingMatch<TextSegment>> matches = relevant.matches();

        assertEquals( 1, matches.size() );

        RetrievalGrader.Score answer =
                grader.apply( new RetrievalGrader.Arguments(question, matches.get(0).embedded().text()));

        assertEquals( "no", answer.binaryScore);


    }

    @Test
    public void WebSearchTest() {

        WebSearchTool webSearchTool = new WebSearchTool(getTavilyApiKey());
        List<Content> webSearchResults = webSearchTool.apply("agent memory");

        String webSearchResultsText = webSearchResults.stream().map( content -> content.textSegment().text() )
                .collect(Collectors.joining("\n"));

        assertNotNull( webSearchResultsText );

        System.out.println( webSearchResultsText );

    }

    @Test
    public void questionRouterTest() {

        QuestionRouter qr = new QuestionRouter(chatModel);

        QuestionRouter.Type result = qr.apply( "What are the stock options?");

        assertEquals( QuestionRouter.Type.web_search, result );

        result = qr.apply( "agent memory?");

        assertEquals( QuestionRouter.Type.vectorstore, result );
    }


    @Test
    public void generationTest() {

        final var retriever = new ChromaStore();

        String question = "agent memory";
        EmbeddingSearchResult<TextSegment> relevantDocs = retriever.search(question);

        List<String> docs = relevantDocs.matches().stream()
                                .map( m -> m.embedded().text() )
                                .collect(Collectors.toList());
        Generation qr = new Generation(chatModel);

        String result = qr.apply( question, docs );

        System.out.println( result );
    }

    @Test
    public void getGraphTest() throws Exception {

        AdaptiveRag adaptiveRag = new AdaptiveRag(getTavilyApiKey());

        org.bsc.langgraph4j.StateGraph<AdaptiveRag.State> graph = adaptiveRag.buildGraph();

        GraphRepresentation plantUml = graph.getGraph( GraphRepresentation.Type.PLANTUML, "Adaptive RAG" );

        System.out.println( plantUml.content() );

        GraphRepresentation mermaid = graph.getGraph( GraphRepresentation.Type.MERMAID, "Adaptive RAG" );

        System.out.println( mermaid.content() );
    }
}
