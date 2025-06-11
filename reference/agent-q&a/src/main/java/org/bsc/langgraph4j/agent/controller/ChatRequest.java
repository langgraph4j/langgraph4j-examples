package org.bsc.langgraph4j.agent.controller;

public record ChatRequest(String sessionId, String message) {
}