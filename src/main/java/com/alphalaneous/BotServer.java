package com.alphalaneous;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class BotServer {
    private static ServerSocket serverSocket;
    public static int clients = 0;

    public static final ArrayList<ClientHandler> clientsList = new ArrayList<>();

    public static void start(){
        try {
            serverSocket = new ServerSocket(2963);
            while (true){
                ClientHandler handler = new ClientHandler(serverSocket.accept());
                clientsList.add(handler);
                handler.start();
                Thread.sleep(0);
            }
        }
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void restart(){

        for(ClientHandler client : clientsList){
            client.reconnect();
        }

    }

    public static void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
