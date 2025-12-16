package dev.langchain4j.image_to_diagram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.regex.Pattern;

/**
 * This class provides functionality to parse a JSON formatted string and convert it into a `Diagram.Element` object.
 */
public interface  DiagramOutputParser {

    /**
     * Parses a JSON string and converts it into a Diagram.Element object.
     *
     * @param s The JSON string to be parsed.
     * @return A Diagram.Element object representing the parsed JSON data.
     * @throws IllegalArgumentException If no valid JSON is found in the input string.
     */
    static Diagram.Element parse(String s) {
        // String pattern = "```json\n(.*?)\n```";
        String pattern = "```json\n(.*?)\n```";

        // Create a Pattern object
        Pattern jsonPattern = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE);

        // Create a Matcher object
        java.util.regex.Matcher matcher = jsonPattern.matcher(s);

        // Check if a match is found
        if (!matcher.find()) {
            throw new IllegalArgumentException("no diagram provided in text\n" + s);
        }

        var jsonMapper = JsonMapper.builder().build();

        try {
            return  jsonMapper.readValue(matcher.group(1), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}