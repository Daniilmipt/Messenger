package org.example;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class DownService implements Command{
    private final ServerSomthing serverSomthing;
    public DownService(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }

    @Override
    public void execute() {
        try {
            serverSomthing.getLoggChat().execute(LogStatus.Stop);
            if(!serverSomthing.getSocket().isClosed()) {
                serverSomthing.getSocket().close();
                serverSomthing.getIn().close();
                serverSomthing.getOut().close();
                Server.getWriter().flush();
//                Server.getWriter().close();
                if (Server.serverMap.containsKey(Optional.ofNullable(serverSomthing.getChatName()).orElse(""))) {
                    for (ServerSomthing vr : Server.serverMap.get(serverSomthing.getChatName())) {
                        if (vr.equals(serverSomthing)) vr.interrupt();
                        Server.serverMap.get(serverSomthing.getChatName()).remove(serverSomthing);
                    }
                }
            }
            try
            {
                if(serverSomthing.getConnection() != null)
                    serverSomthing.getConnection().close();
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
