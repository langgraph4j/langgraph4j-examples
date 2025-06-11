package org.bsc.langgraph4j.agent;

import org.bsc.langgraph4j.agent.langgraph.QAAssistant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LG4jQAAgentConsole implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        var console = System.console();

        var assistant = new QAAssistant();

        var question = console.readLine("\nQuestion: ");

        var output = assistant.startConversation( question );

        do {
            console.printf( "\nAgent:\n%s", output.state().messages());
            var message = console.readLine("\nAnswer: ");
            output = assistant.provideFeedback(message);

        } while( !output.isEND() );

        console.printf( "\nResult: \n%s", output.state().messages() );

    }
}
