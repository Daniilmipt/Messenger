package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;

public class ConnectToDataBase implements Command{
    private final ServerSomthing serverSomthing;

    public ConnectToDataBase(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }
    @Override
    public void execute() {
        String url = "jdbc:postgresql://localhost:5432/public";
        String user = "postgres";
        String password = "SA!()+_!SA";
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            serverSomthing.setConnection(connection);
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }
}
