package org.example;

import java.io.IOException;

public class AuthorizationChat implements Command{
    private final ServerSomthing serverSomthing;
    public AuthorizationChat(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }

    @Override
    public void execute() {
        LogStatus logError = LogStatus.Authorization;
        try {
            String word = serverSomthing.getIn().readLine();
            serverSomthing.getOut().write(word + "\n");
            serverSomthing.setNickName(word.substring(6));
            serverSomthing.getOut().flush();
        } catch (IOException ioException) {
            logError = LogStatus.NickNameError;
            System.err.println(ioException.getMessage());
        }
        finally {
            serverSomthing.getLoggChat().execute(logError);
        }
    }
}
