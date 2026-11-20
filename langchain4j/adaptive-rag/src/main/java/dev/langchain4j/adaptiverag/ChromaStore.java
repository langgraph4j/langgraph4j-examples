package dev.langchain4j.adaptiverag;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import java.time.Duration;

/**
 * The ChromaStore class provides a singleton interface for interacting with the Chroma Embedding Store.
 * This class is designed to be used in conjunction with an OpenAI API key to perform search operations on text segments.
 * It abstracts the embedding model and the storage layer, offering a simple method to retrieve relevant data based on queries.
 */
public final class ChromaStore {

    private final ChromaEmbeddingStore chroma;
    private final DimensionAwareEmbeddingModel embeddingModel;

    /**
     * This constructor initializes a new instance of the ChromaStore.
     */
    public ChromaStore() {

        this.chroma = ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8000")
                .collectionName("rag-chroma")
                .timeout(Duration.ofMinutes(2))
                .build();

        this.embeddingModel = OllamaEmbeddingModel.builder()
                .modelName("qwen-3.5")
                .build();
    }

    /**
     * Searches for text segments in the embedding index that match a given query.
     *
     * @param query The search query to be embedded and used for searching.
     * @return An EmbeddingSearchResult containing up to one TextSegment that best matches the query.
     */
    public EmbeddingSearchResult<TextSegment> search(String query) {

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding( queryEmbedding )
                .maxResults( 1 )
                .minScore( 0.0 )
                .build();
        return chroma.search( searchRequest );

    }
}