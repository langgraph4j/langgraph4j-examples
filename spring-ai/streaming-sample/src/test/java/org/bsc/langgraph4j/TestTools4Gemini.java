package org.bsc.langgraph4j;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static java.lang.String.format;

public class TestTools4Gemini {

    @Tool( description="tool for test AI agent executor")
    String execTest(@ToolParam( description = "test message") String message) {
        return format( "test tool ('%s') executed with result 'OK'", message);
    }

    @Tool( description="return current number of system thread allocated by application")
    String threadCount() {
        // FIX for GEMINI MODEL
        return format("'%d'", Thread.getAllStackTraces().size());
    }

}
