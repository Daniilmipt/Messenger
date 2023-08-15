package org.example;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Server for communication with users
 */
public class Server {

    public static final int PORT = 8080;

    /**
     * serverMap - map with chat name and list of client connections
     * in the future, we need change list to tree structure
     */
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<ServerSomthing>> serverMap = new ConcurrentHashMap<>();
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server Started");
            while (true) {
                Socket socket = server.accept();
                try {
                    ServerSomthing serverSomthing = new ServerSomthing(socket);
//                    if this chat exists, we will add this connection to chat
//                    else we will create new chat with this name
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