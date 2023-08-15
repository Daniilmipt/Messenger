package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Project realize console multi - user chat.
 * Run program - run Server.main()
 */

class ServerSomthing extends Thread {

    private final Socket socket;

    // read from socket
    private final BufferedReader in;

    // write to socket
    private final BufferedWriter out;
    private String chatName;
    private String nickName;

    // connection with database
    private Statement statement;

    // write to log.txt file
    private static final BufferedWriter writer;

    static {
        try {
            writer = new BufferedWriter(new FileWriter("/home/daniil/IdeaProjects/Messenger/log.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.getNickName();
        this.getChatName();
        this.getConnect();
        this.printHistory(out, chatName, statement);
        this.start();
    }

    /**
     * Connection to sqllite database
     */
    private void getConnect(){
        Connection connection;
        try
        {
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(5);
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void insertToDataBase(String word, String chatName){
        String dateTime = word.substring(1,20);
        String[] arr = word.substring(21).split(" ");
        String sender = nickName;
        String message = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));
        try {
//            statement.executeUpdate("drop table if exists chat_info");
//            statement.executeUpdate("create table chat_info (chat_name string, datetime string, sender string, message string)");
            statement.executeUpdate(String.format("insert into chat_info values('%1$s', '%2$s', '%3$s', '%4$s')", chatName, dateTime, sender, message));
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }

    public static String getCurrentDateTime(){
        Date time = new Date();
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(time);
    }


    /**
     * Save and print user messages in chat
     */
    @Override
    public void run() {
        String word;
        try {
            // First and second messages were nickname and chatname
            // Now we are waiting user messages in chat
            while (true) {
                word = in.readLine();
                if(word == null) {
                    this.downService();
                    break;
                }

                // stop chat
                if (word.equals("stop")){
                    Map<String, String> map = new HashMap<>();
                    map.put("nickName", nickName);
                    map.put("chatName", chatName);
                    map.put("time", ServerSomthing.getCurrentDateTime());
                    map.put("status", LogStatus.Stop.descr());
                    objectMapper.writeValueAsString(map);

                } else if (word.equals("back")) {
                    // go on menu with choosing chat
                    Server.serverMap.get(chatName).remove(this);
                    this.send("Choose chat: ");
                    this.getChatName();
                    if(Server.serverMap.containsKey(chatName)){
                        Server.serverMap.get(chatName).add(this);
                    }
                    else{
                        Server.serverMap.put(chatName, new ConcurrentLinkedQueue<>(List.of(this)));
                    }
                    this.printHistory(out, chatName, statement);
                } else {
                    // chat messages
                    if (Server.serverMap.containsKey(chatName)) {
                        System.out.println("Echoing: " + chatName + " -- " + word);
                    }

                    this.insertToDataBase(word, chatName);
                    for (ServerSomthing vr : Server.serverMap.get(chatName)) {
                        vr.send(word);
                    }
                }
            }
        } catch (IOException e) {
            this.writeJsonLog(LogStatus.ReadError);
            this.downService();
        }
    }

    /**
     * Get Json with logging user actions
     */
    private String getJsonLog(LogStatus logError){
        String dateTime = ServerSomthing.getCurrentDateTime();
        Map<String, String> map = new HashMap<>();
        map.put("time", dateTime);

        if (logError.equals(LogStatus.NickNameError))
        {
            map.put("status", LogStatus.NickNameError.descr());
        }
        else {
            map.put("nickName", nickName);
            if (logError.equals(LogStatus.Authorization)) {
                map.put("status", LogStatus.Authorization.descr());
            } else if (logError.equals(LogStatus.Chat)) {
                map.put("chatName", chatName);
                map.put("status", LogStatus.Chat.descr());
            } else if (logError.equals(LogStatus.ChatError)) {
                map.put("status", LogStatus.ChatError.descr());
            } else if (logError.equals(LogStatus.ReadError)) {
                map.put("chatName", chatName);
                map.put("status", LogStatus.ReadError.descr());
            } else if (logError.equals(LogStatus.Stop)) {
                map.put("chatName", chatName);
                map.put("status", LogStatus.Stop.descr());
            }
        }
        try {
            System.out.println(map);
            return objectMapper.writeValueAsString(map);
        }
        catch (JsonProcessingException ignored){
            return "";
        }
    }

    /**
     * Write Json logs in log.txt
     */
    private void writeJsonLog(LogStatus logError){
        String json = this.getJsonLog(logError);
        try {
            writer.write(json);
            writer.write("\n");
            writer.flush();
        }
        catch (IOException ioException){
            System.err.println("Can not log error in file log.txt");
        }
    }


    /**
     * Waiting until the client enters the name in console
     */
    private void getNickName(){
        LogStatus logError = LogStatus.Authorization;
        try {
            String word = in.readLine();
            out.write(word + "\n");
            nickName = word.substring(6);
            out.flush();
        } catch (IOException ioException) {
            logError = LogStatus.NickNameError;
            System.err.println(ioException.getMessage());
        }
        finally {
            this.writeJsonLog(logError);
        }
    }

    /**
     * Waiting until the client enters the chat in console
     */
    private void getChatName(){
        LogStatus logError = LogStatus.Chat;
        try {
            String word = in.readLine();
            String[] arr = word.substring(21).split(" ");
            chatName = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));
            out.write("You are in chat: " + chatName + "\n");
            out.flush();
            System.out.println(nickName + " joined to " + chatName + " in "
                    + ServerSomthing.getCurrentDateTime());
        } catch (IOException ioException) {
            logError = LogStatus.ChatError;
            System.err.println(ioException.getMessage());
        }
        finally {
            this.writeJsonLog(logError);
        }
    }

    /**
     * Print chat history to user
     */
    public void printHistory(BufferedWriter writer, String chatName, Statement statement){
        try {
            ResultSet rs = statement.executeQuery(String.format("select * from chat_info where chat_name = '%1$s' limit 10", chatName));
            try {
                writer.write("History messages" + "\n");
                while (rs.next()) {
                    writer.write(("(" + rs.getString("datetime") + "): ")
                            + (rs.getString("sender") + " -- ")
                            + rs.getString("message") + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            }
            catch (IOException ioException) {System.err.println(ioException.getMessage());}
        }
        catch (SQLException sqlException) {System.err.println(sqlException.getMessage());}
    }

    public String chatName(){
        return chatName;
    }

    /**
     * Write user message for all chat members
     */
    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {
            System.out.println("Failed to send message to client");
        }

    }

    /**
     * Close server
     */
    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                writer.close();
                for (ServerSomthing vr : Server.serverMap.get(chatName)) {
                    if(vr.equals(this)) vr.interrupt();
                    Server.serverMap.get(chatName).remove(this);
                }
            }

            try
            {
                if(statement.getConnection() != null)
                    statement.getConnection().close();
            }
            catch(SQLException e)
            {
                System.err.println(e.getMessage());
            }
        } catch (IOException ignored) {
            System.out.println("Failed to close users");
        }
    }
}