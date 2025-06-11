package org.bsc.langgraph4j.agent.controller;

import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.agent.langgraph.QAAssistant;
import org.bsc.langgraph4j.agent.langgraph.QAState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/chat")
public class ChatController {

    /**
     * sessionId → local in memory agent state. (next step moving to redis)
     **/
    private final Map<String, QAAssistant> sessions = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req) throws Exception {
        // if sessionId is null, agent starts with from scratch
        String sessionId = req.sessionId() != null
                ? req.sessionId()
                : UUID.randomUUID().toString();

        // get latest agent output from memory
        QAAssistant assistant = sessions.computeIfAbsent(sessionId, id -> {
            try {
                return new QAAssistant();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        System.out.println("assistant: " + assistant.toString());

        NodeOutput<QAState> output;
        if (req.sessionId() == null) {
            output = assistant.startConversation(req.message());
        } else {
            output = assistant.provideFeedback(req.message());
        }

        String agentMsg = output.state().messages();
        boolean waitingForUser = !output.isEND();
        return ResponseEntity.ok(new ChatResponse(sessionId, agentMsg, waitingForUser));
    }
}