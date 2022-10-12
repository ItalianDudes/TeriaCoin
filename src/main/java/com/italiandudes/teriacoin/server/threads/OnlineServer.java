package com.italiandudes.teriacoin.server.threads;

import com.italianDudes.idl.common.Logger;
import com.italiandudes.teriacoin.server.lists.PeerList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class OnlineServer implements Runnable {

    //Attributes
    private static OnlineServer onlineServerInstance = null;
    private final ServerSocket serverSocket;
    private boolean isActive;

    //Constructors
    private OnlineServer(int port){
        ServerSocket temp = null;
        try{
            temp = new ServerSocket(port);
            isActive = true;
        }catch (IllegalArgumentException | IOException illegalArgumentException){
            Logger.log(illegalArgumentException);
            isActive = false;
        }
        serverSocket = temp;
    }

    //Singleton Getter
    public static OnlineServer getInstance(int port){
        if(onlineServerInstance==null){
            onlineServerInstance = new OnlineServer(port);
        }
        return onlineServerInstance;
    }

    //Methods
    public void sendInterrupt(){
        try {
            serverSocket.close();
        }catch (IOException ignored){}
        Thread.currentThread().interrupt();
    }
    @Override
    public void run() {

        if(isActive) {

            Socket incomingConnection = null;

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    incomingConnection = serverSocket.accept();
                } catch (IOException ioException) {
                    if(!serverSocket.isClosed())
                        Logger.log(ioException);
                }

                if(incomingConnection!=null && !incomingConnection.isClosed())
                    new Thread(new Authenticator(incomingConnection)).start();

            }

            if(!serverSocket.isClosed()){
                try{
                    serverSocket.close();
                }catch (IOException ignored){}
            }

            PeerList.disconnectAll();

        }

    }

}