package org.example;

/**
 * Enumeration for logging user actions
 */
public enum LogStatus {
    NickNameError("Error when input nickname"),
    ChatError("Error when input chat"),
    ReadError("Error when read/write in chat"),
    Authorization("authorization"),
    Chat("chat"),
    Stop("finish"),
    ;
    private final String description;

    LogStatus(String s) {
        this.description = s;
    }

    public String descr(){
        return this.description;
    }
}
