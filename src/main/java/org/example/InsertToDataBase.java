package org.example;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class InsertToDataBase{
    private final ServerSomthing serverSomthing;
    public InsertToDataBase(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }

    public void execute(String word, String chatName) {
        String dateTime = word.substring(1,20);
        String[] arr = word.substring(21).split(" ");
        String message = String.join(" ", Arrays.copyOfRange(arr, 2, arr.length));
        try {
            String sql_chat = "insert into chat values(?, ?) ON CONFLICT DO NOTHING";
            PreparedStatement ps = serverSomthing.getConnection().prepareStatement(sql_chat);
            int chatHashCode = Objects.hashCode(chatName);
            ps.setInt(1, chatHashCode);
            ps.setString(2, chatName);
            ps.executeUpdate();

            String sql_sender = "insert into sender values(?, ?) ON CONFLICT DO NOTHING";
            ps = serverSomthing.getConnection().prepareStatement(sql_sender);
            int nickNameHashCode = Objects.hashCode(serverSomthing.getNickName());
            ps.setInt(1, nickNameHashCode);
            ps.setString(2, serverSomthing.getNickName());
            ps.executeUpdate();


            String sql_message = "insert into chat_message values(?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
            ps = serverSomthing.getConnection().prepareStatement(sql_message);
            ps.setInt(1, Objects.hash(chatName, serverSomthing.getNickName(), message, dateTime));
            ps.setInt(2, chatHashCode);
            ps.setInt(3, nickNameHashCode);
            ps.setString(4, message);
            ps.setString(5, dateTime);
            ps.executeUpdate();
            ps.close();
//            statement.executeUpdate("drop table if exists chat_info");
//            statement.executeUpdate("create table chat_info (chat_name string, datetime string, sender string, message string)");
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }
}
