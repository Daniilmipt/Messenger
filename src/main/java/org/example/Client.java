package org.example;

import java.net.*;
import java.io.*;

/**
 * Run CLient - run CLient.main()
 */

class ClientSomthing {

    private Socket socket;

    // read from socket
    private BufferedReader in;

    // write to socket
    private BufferedWriter out;

    // read from console
    private BufferedReader inputUser;
    private String nickname;


    public ClientSomthing(String addr, int port) {
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // ask nickname from console
            this.pressNickname();

            // ask chatname from console
            this.chooseChat();

            // Thread reading messages from socket
            new ReadMsg().start();

            // Thread write messages to socket
            new WriteMsg().start();
        } catch (IOException e) {
            // turn off
            this.downService();
        }
    }

    /**
     * Read nickname from console
     */

    private void pressNickname() {
        System.out.print("Press your nick: \n");
        try {
            nickname = inputUser.readLine();
            out.write("Hello " + nickname + "\n");
            out.flush();
        } catch (IOException ignored) {
        }

    }

    /**
     * Read chan name from console
     */
    private void chooseChat() {
        System.out.print("Choose chat: \n");
        try {
            String chatName = inputUser.readLine();
//            out.write("You are in chat: " + chatName + "\n");
            out.write("(" + ServerSomthing.getCurrentDateTime() + ") " + nickname + ": " + chatName + "\n");
            out.flush();
        } catch (IOException ignored) {
        }

    }

    /**
     * close socket
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {}
    }

    /**
     * Read message from server and imagine it on console
     */
    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    // wait message
                    str = in.readLine();
                    if (str.equals("stop")) {
                        ClientSomthing.this.downService();
                        break;
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                ClientSomthing.this.downService();
            }
        }
    }

    /**
     * Read message from console and imagine it on console
     */
    public class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    String dtime = ServerSomthing.getCurrentDateTime();
                    userWord = inputUser.readLine();
                    if (userWord.equals("stop")) {
                        out.write("stop");
                        break;
                    }

                    if(userWord.equals("back")){
                        out.write("back\n");
                    }
                    else{
                        // send to server
                        out.write("(" + dtime + ") " + nickname + ": " + userWord + "\n");
                    }
                    out.flush();
                } catch (IOException e) {
                    ClientSomthing.this.downService();
                }
            }
            ClientSomthing.this.downService();
        }
    }
}

public class Client {

    public static String ipAddr = "localhost";
    public static int port = 8080;

    /**
     * Create client-connection with server
     */
    public static void main(String[] args) {
        new ClientSomthing(ipAddr, port);
    }
}