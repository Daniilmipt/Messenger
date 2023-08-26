package org.example;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChangeChat implements Command{
    private final ServerSomthing serverSomthing;
    public ChangeChat(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }
    @Override
    public void execute() {
        Server.serverMap.get(serverSomthing.getChatName()).remove(serverSomthing);
        serverSomthing.getSendMessageForClient().execute("Choose chat: ");
        serverSomthing.getChooseChat().execute();
        if(Server.serverMap.containsKey(serverSomthing.getChatName())){
            Server.serverMap.get(serverSomthing.getChatName()).add(serverSomthing);
        }
        else{
            Server.serverMap.put(serverSomthing.getChatName(), new ConcurrentLinkedQueue<>(List.of(serverSomthing)));
        }
        serverSomthing.getPrintHistory().execute();
    }
}
