package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Sample
{
    public static void main(String[] args)
    {
        Connection connection = null;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//            statement.executeUpdate("drop table if exists chat_info");
//            statement.executeUpdate("create table chat_info (chat_name string, datetime string, sender string, message string)");
            ResultSet rs = statement.executeQuery("select * from chat_info");
            while(rs.next())
            {
                // read the result set
                System.out.println("chat_name = " + rs.getString("chat_name"));
                System.out.println("datetime = " + rs.getString("datetime"));
                System.out.println("sender = " + rs.getString("sender"));
                System.out.println("message = " + rs.getString("message"));
                System.out.println();
            }
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}