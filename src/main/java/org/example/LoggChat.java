package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoggChat {

    private final ServerSomthing serverSomthing;
    public LoggChat(ServerSomthing serverSomthing) {
        this.serverSomthing = serverSomthing;
    }
    public void execute(LogStatus logError){
        String json = getJsonLog(logError);
        Server.getWriter().println(json);
    }

    private String getJsonLog(LogStatus logError){
        String dateTime = ServerSomthing.getCurrentDateTime();
        Map<String, String> map = new HashMap<>();
        map.put("time", dateTime);

        if (logError.equals(LogStatus.NickNameError))
        {
            map.put("status", LogStatus.NickNameError.descr());
        }
        else {
            map.put("nickName", serverSomthing.getNickName());
            if (logError.equals(LogStatus.Authorization)) {
                map.put("status", LogStatus.Authorization.descr());
            } else if (logError.equals(LogStatus.Chat)) {
                map.put("chatName", serverSomthing.getChatName());
                map.put("status", LogStatus.Chat.descr());
            } else if (logError.equals(LogStatus.ChatError)) {
                map.put("status", LogStatus.ChatError.descr());
            } else if (logError.equals(LogStatus.ReadError)) {
                map.put("chatName", serverSomthing.getChatName());
                map.put("status", LogStatus.ReadError.descr());
            } else if (logError.equals(LogStatus.Stop)) {
                map.put("chatName", serverSomthing.getChatName());
                map.put("status", LogStatus.Stop.descr());
            }
        }
        try {
            return ServerSomthing.getObjectMapper().writeValueAsString(map);
        }
        catch (JsonProcessingException ignored){
            return "";
        }
    }
}
