package org.example;

import java.io.BufferedReader;
import java.io.IOException;

public class ListenChat implements Command{
    private final ServerSomthing serverSomthing;
    public ListenChat(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }
    @Override
    public void execute() {
        String word;
        try {
            // First and second messages were nickname and chatname
            // Now we are waiting user messages in chat
            while (true) {
                word = serverSomthing.getIn().readLine();
                if(word == null) {
                    serverSomthing.getDownService().execute();
                    break;
                }

                if (word.equals("stop")){
                    serverSomthing.getDownService().execute();
                } else if (word.equals("back")) {
                    serverSomthing.getChangeChat().execute();
                } else {
                    serverSomthing.getSendMessageForClient().execute(word);
                }
            }
        } catch (IOException e) {
            serverSomthing.getLoggChat().execute(LogStatus.ReadError);
            serverSomthing.getDownService().execute();
        }
    }
}
