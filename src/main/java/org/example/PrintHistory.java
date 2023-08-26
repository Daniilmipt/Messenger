package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;

public class PrintHistory implements Command{
    private final ServerSomthing serverSomthing;
    public PrintHistory(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }

    @Override
    public void execute() {
        try {
            PreparedStatement ps = createPreparedStatement(serverSomthing.getConnection(), serverSomthing.getChatName());
            ResultSet resultSet = ps.executeQuery();
            printHistory(serverSomthing.getOut(), resultSet);
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

    private PreparedStatement createPreparedStatement(Connection connection, String chatName) throws SQLException {
        String sql =
                "select cm.context_message, cm.datetime, s.nick_name " +
                "from chat_message as cm " +
                "inner join chat as c " +
                        "on cm.chat_id = c.chat_id " +
                        "inner join sender as s " +
                        "on s.party_id = cm.party_id " +
                        "where c.chat_name = ? limit 10";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, chatName);
        return ps;
    }

    private void printHistory(BufferedWriter writer, ResultSet resultSet) throws SQLException{
        try {
            writer.write("History messages" + "\n");
            while (resultSet.next()) {
                writer.write(("(" + resultSet.getString("datetime") + "): ")
                        + (resultSet.getString("nick_name") + " -- ")
                        + resultSet.getString("context_message") + "\n");
            }
            writer.write("/...." + "\n");
            writer.flush();
        }
        catch (IOException ioException) {System.err.println(ioException.getMessage());}
    }
}
