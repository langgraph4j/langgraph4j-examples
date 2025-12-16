package dev.langchain4j.image_to_diagram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// diagram schema
/*
const participantSchema = z.object({
    name: z.string().describe('participant name'),
    shape: z.string().describe('participant shape'), // can be an enum ?
    description: z.string().describe('participant description')
});

const relationSchema = z.object({
    source: z.string().describe('source participant'),
    target: z.string().describe('target participant'),
    description: z.string().describe('relation description')
});

const containerSchema = z.object({
    name: z.string().describe('container name'),
    children: z.array(z.string()).describe('list of contained elements name'), // can be an enum ?
    description: z.string().describe('container description')
});

export const diagramSchema = z.object({
    type: z.string().describe('Diagram tipology (one word). Eg. "sequence", "class", "process", etc."'),
    title: z.string().describe("Diagram summary (max one line) or title (if any)"),
    participants: z.array(participantSchema).describe("list of participants in the diagram "),
    relations: z.array(relationSchema).describe("list of relations in the diagram"),
    containers: z.array(containerSchema).describe("list of participants that contain other ones in the diagram"),
    description: z.array(z.string()).describe("Step by step description of the diagram with clear indication of participants and actions between them."),
});

*/

/**
 * Represents a diagram which contains various elements such as participants, relations, and containers.
 */
public interface Diagram {

    /**
     * Represents a participant in an event.
     *
     * @param name      the name of the participant
     * @param shape     the shape associated with the participant
     * @param description a brief description of the participant
     */
    record Participant (
            @JsonProperty("name") String name,
            @JsonProperty("shape") String shape,
            @JsonProperty("description") String description) {
    }

    /**
     * Represents a relation between two entities with an optional description.
     *
     * @param source     the identifier of the source entity
     * @param target     the identifier of the target entity
     * @param description  an optional description of the relation
     */
    record Relation (
            @JsonProperty("source") String source,
            @JsonProperty("target") String target,
            @JsonProperty("description") String description) {
    }

    /**
     * Represents a container with a name, children, and a description.
     * 
     * @param name         the name of the container
     * @param children     a list of child containers or items
     * @param description  a brief description of the container
     */
    record Container(
            @JsonProperty("name") String name,
            @JsonProperty("children") List<String> children,
            @JsonProperty("description") String description) {
    }

    /**
     * Represents an element in a structured format.
     *
     * @param type        the type of the element
     * @param title       the title of the element
     * @param participants  a list of participants associated with the element
     * @param relations     a list of relations associated with the element
     * @param containers    a list of containers associated with the element
     * @param description a list of descriptions for the element
     */
    record Element(
            @JsonProperty("type") String type,
            @JsonProperty("title") String title,
            @JsonProperty("participants") List<Participant> participants,
            @JsonProperty("relations") List<Relation> relations,
            @JsonProperty("containers") List<Container> containers,
            @JsonProperty("description") List<String> description
    ) {}

}