package org.bsc.langgraph4j.agent.controller;


public record ChatResponse(String sessionId, String agentMessage, boolean waitingForUser) {
}