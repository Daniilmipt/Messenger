package org.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChooseChat implements Command{
    private final ServerSomthing serverSomthing;
    public ChooseChat(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }
    @Override
    public void execute() {
        LogStatus logError = LogStatus.Chat;
        try {
            String word = serverSomthing.getIn().readLine();
            String[] arr = word.substring(21).split(" ");
            serverSomthing.setChatName(String.join(" ", Arrays.copyOfRange(arr, 2, arr.length)));
            serverSomthing.getOut().write("You are in chat: " + serverSomthing.getChatName() + "\n");
            serverSomthing.getOut().flush();
            System.out.println(serverSomthing.getNickName() + " joined to " + serverSomthing.getChatName() + " in "
                    + ServerSomthing.getCurrentDateTime());

            if (!Server.serverMap.containsKey(serverSomthing.getChatName())) {
                Server.serverMap.put(serverSomthing.getChatName(), new ConcurrentLinkedQueue<>(List.of(serverSomthing)));
            } else {
                Server.serverMap.get(serverSomthing.getChatName()).add(serverSomthing);
            }
        } catch (IOException ioException) {
            logError = LogStatus.ChatError;
            System.err.println(ioException.getMessage());
        }
        finally {
            serverSomthing.getLoggChat().execute(logError);
        }
    }
}
