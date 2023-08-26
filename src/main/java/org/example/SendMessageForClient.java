package org.example;

import java.io.BufferedWriter;
import java.io.IOException;

public class SendMessageForClient{
    private final ServerSomthing serverSomthing;
    public SendMessageForClient(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }
    public void execute(String message) {
        if (Server.serverMap.containsKey(serverSomthing.getChatName()))
            System.out.println("Echoing: " + serverSomthing.getChatName() + " -- " + message);

        serverSomthing.getInsertToDataBase().execute(message, serverSomthing.getChatName());
        Server.serverMap.get(serverSomthing.getChatName()).forEach(vr -> vr.send(message));
    }
}
