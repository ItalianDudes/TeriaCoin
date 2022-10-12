package com.italiandudes.teriacoin.server;

import com.italianDudes.idl.common.InfoFlags;
import com.italianDudes.idl.common.Logger;
import com.italianDudes.idl.common.State;
import com.italiandudes.teriacoin.TeriaCoin;
import com.italiandudes.teriacoin.common.Config;
import com.italiandudes.teriacoin.server.threads.OnlineServer;

import java.util.Scanner;

public final class TeriaCoinServer {

    //Attributes
    private static Config serverConfig = null;

    //Methods
    public static Config getConfig(){
        return serverConfig;
    }
    public static State startServer(){

        serverConfig = new Config();
        if(!serverConfig.readConfigs(TeriaCoin.Defs.SERVER_CONFIG_FILEPATH)){
            Logger.log("[SERVER START ROUTINE] Config file not found! Firing up with default configs...");
        }

        Logger.log("[SERVER START ROUTINE] Server started successfully at port: "+serverConfig.getValue(TeriaCoin.Defs.KEY_PORT));

        OnlineServer onlineServer = OnlineServer.getInstance(Integer.parseInt(serverConfig.getValue(TeriaCoin.Defs.KEY_PORT)));

        Thread onlineServerThread = new Thread(onlineServer);
        onlineServerThread.start();

        Scanner scan = new Scanner(System.in);
        String keyboardBuffer;

        do{
            keyboardBuffer = scan.nextLine();
        }while (!ServerUtils.parseKeyboardInput(keyboardBuffer));

        onlineServer.sendInterrupt();
        onlineServerThread.interrupt();

        if(serverConfig.writeConfigs(TeriaCoin.Defs.SERVER_CONFIG_FILEPATH)){
            Logger.log("Configs saved successfully!");
        }else{
            Logger.log("Configs save failed!");
        }

        Logger.log("--SERVER CLOSED--", new InfoFlags());

        return new State(0);
    }

    public static final class ServerDefs {
        //PATHS
        public static final String SERVER_DIRECTORY_PATH = TeriaCoin.Defs.BUILT_IN_PATH_RESOURCES +"server/";
        public static final String SERVER_REGISTERED_BALANCES_FILEPATH = SERVER_DIRECTORY_PATH+"registeredBalances.txt";

        //Return Values
        public static final int NO_ERR = 0;
        public static final int PORT_OUT_OF_BOUNDS = 2451; //Provided port in configs out of bounds (0-65535)
        public static final int IMPOSSIBLE_TO_BIND_PORT = 4154; //Cannot bind port, probably because is already bound
        public static final int CANNOT_CREATE_SERVER_DIRECTORY = 2111;
        public static final int CANNOT_READ_SERVER_REGISTERED_BALANCES_FILE = 4514;
        public static final int CANNOT_WRITE_SERVER_REGISTERED_BALANCES_FILE = 4516;
        public static final int CANNOT_CLOSE_SERVER_REGISTERED_BALANCES_FILE = 4515;
        public static final int CANNOT_READ_SERVER_PENDING_USERS_LIST_FILE = 4211;
        public static final int CANNOT_WRITE_SERVER_PENDING_USERS_LIST_FILE = 4212;
        public static final int CANNOT_CLOSE_SERVER_PENDING_USERS_LIST_FILE = 4213;
    }

}