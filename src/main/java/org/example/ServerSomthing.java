package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Project realize console multi - user chat.
 * Run program - run Server.main()
 */

@Getter
public class ServerSomthing extends Thread {

//    private Queue<Command> commandList;
    private final Command authorizationChat = new AuthorizationChat(this);
    private final Command chooseChat = new ChooseChat(this);
    private final Command connectToDataBase = new ConnectToDataBase(this);
    private final Command printHistory = new PrintHistory(this);
    private final Command changeChat = new ChangeChat(this);
    private final SendMessageForClient sendMessageForClient = new SendMessageForClient(this);
    private final Command downService = new DownService(this);
    private final LoggChat loggChat = new LoggChat(this);
    private final InsertToDataBase insertToDataBase = new InsertToDataBase(this);
    private final Command listenChat = new ListenChat(this);


    private final Socket socket;

    // read from socket
    private final BufferedReader in;

    // write to socket
    private final BufferedWriter out;
    @Setter
    private String chatName;
    @Setter
    private String nickName;

    // connection with database
    @Setter
    private Connection connection;

    // write to log.txt file
    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.start();
    }

    public static String getCurrentDateTime(){
        Date time = new Date();
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(time);
    }

    public void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {
            System.out.println("Failed to send message to client");
        }

    }


    /**
     * Save and print user messages in chat
     */
    @Override
    public void run() {
        authorizationChat.execute();
        chooseChat.execute();
        connectToDataBase.execute();
        printHistory.execute();
        listenChat.execute();
    }
}