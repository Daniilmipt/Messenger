package org.example;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {

    public static final int PORT = 8080;
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<ServerSomthing>> serverMap = new ConcurrentHashMap<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента
    public static Story story; // история переписки

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            story = new Story();
            System.out.println("Server Started");
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    ServerSomthing serverSomthing = new ServerSomthing(socket);
                    if(!serverMap.containsKey(serverSomthing.chatName())){
                        serverMap.put(serverSomthing.chatName(), new ConcurrentLinkedQueue<>(List.of(serverSomthing)));
                    }
                    else {
                        serverMap.get(serverSomthing.chatName()).add(serverSomthing);
                    }
                } catch (IOException e) {
                    socket.close();
                }
            }
        }
    }
}