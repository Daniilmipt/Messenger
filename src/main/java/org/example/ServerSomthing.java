package org.example;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * проект реализует консольный многопользовательский чат.
 * вход в программу запуска сервера - в классе Server.
 * @author izotopraspadov, the tech
 * @version 2.0
 */

class ServerSomthing extends Thread {

    private final Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private final BufferedReader in; // поток чтения из сокета
    private final BufferedWriter out; // поток завписи в сокет
    private String chatName;
    private Statement statement;

    /**
     * для общения с клиентом необходим сокет (адресные данные)
     */

    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        // если потоку ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.getNickName();
        this.getChatName();
        Server.story.printStory(out, chatName); // поток вывода передаётся для передачи истории последних 10
        // сооюбщений новому поключению
        this.getConnect();
        this.start(); // вызываем run()
    }

    private void getConnect(){
        Connection connection;
        try
        {
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(5);  // set timeout to 30 sec.
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void insertToDataBase(String word, String chatName){
        String[] arr = word.split(" ");
        String dateTime = arr[0].replaceAll("[()]","");
        String sender = arr[1].replaceAll(":", "");
        String message = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));
        try {
            statement.executeUpdate("drop table if exists chat_info");
            statement.executeUpdate("create table chat_info (chat_name string, datetime string, sender string, message string)");
            statement.executeUpdate(String.format("insert into chat_info values('%1$s', '%2$s', '%3$s', '%4$s')", chatName, dateTime, sender, message));
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        String word;
        try {
            // первое сообщение отправленное сюда - это никнейм
            while (true) {
                word = in.readLine();
                if(word == null) {
                    this.downService(); // харакири
                    break; // если пришла пустая строка - выходим из цикла прослушки
                }
                if(Server.serverMap.containsKey(chatName)) {
                    System.out.println("Echoing: " + "chat: " + chatName + " -- " + word);
                }

                this.insertToDataBase(word, chatName);
                Server.story.addStoryEl(word, chatName);
                for (ServerSomthing vr : Server.serverMap.get(chatName)) {
                    vr.send(word);
                }
            }
        } catch (IOException e) {
            this.downService();
        }
    }

    private void getNickName() throws IOException {
        String word = in.readLine();
        try {
            out.write(word + "\n");
            out.flush(); // flush() нужен для выталкивания оставшихся данных
            // если такие есть, и очистки потока для дьнейших нужд
        } catch (IOException ignored) {}
    }

    private void getChatName() throws IOException {
        String message = in.readLine();
        chatName = message.split(" ")[4];
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    public String chatName(){
        return chatName;
    }

    /**
     * отсылка одного сообщения клиенту по указанному потоку
     * @param msg
     */
    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}

    }

    /**
     * закрытие сервера
     * прерывание себя как нити и удаление из списка нитей
     */
    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
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
        } catch (IOException ignored) {}
    }
}

/**
 * класс хранящий в ссылочном приватном
 * списке информацию о последних 10 (или меньше) сообщениях
 */

class Story {

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> story = new ConcurrentHashMap<>();

    /**
     * добавить новый элемент в список
     * @param el
     */

    public void addStoryEl(String el, String chatName) {
        // если сообщений больше 10, удаляем первое и добавляем новое
        // иначе просто добавить
        if(!story.containsKey(chatName)){
            story.put(chatName, new ConcurrentLinkedQueue<>(List.of(el)));
        }
        else {
            if (story.get(chatName).size() >= 10) {
                story.get(chatName).remove();
                story.get(chatName).add(el);
            } else {
                story.get(chatName).add(el);
            }
        }
    }

    /**
     * отсылаем последовательно каждое сообщение из списка
     * в поток вывода данному клиенту (новому подключению)
     * @param writer
     */

    public void printStory(BufferedWriter writer, String chatName) {
        if(story.containsKey(chatName) && story.get(chatName).size() > 0) {
            try {
                writer.write("History messages" + "\n");
                for (String vr : story.get(chatName)) {
                    writer.write(vr + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            } catch (IOException ignored) {}

        }
    }
}