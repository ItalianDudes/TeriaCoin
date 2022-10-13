package com.italiandudes.teriacoin.server;

import com.italianDudes.idl.common.InfoFlags;
import com.italianDudes.idl.common.Logger;
import com.italianDudes.idl.common.State;
import com.italiandudes.teriacoin.common.Config;
import com.italiandudes.teriacoin.server.lists.BalanceListHandler;
import com.italiandudes.teriacoin.server.lists.ItemIndexListHandler;
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
        if(!serverConfig.readConfigs(ServerDefs.SERVER_CONFIG_FILEPATH)){
            Logger.log("[SERVER START ROUTINE] Config file not found! Firing up with default configs...");
        }

        BalanceListHandler.init();
        ItemIndexListHandler.init();

        Logger.log("[SERVER START ROUTINE] Server started successfully at port: "+serverConfig.getValue(ServerDefs.KEY_PORT));

        OnlineServer onlineServer = OnlineServer.getInstance(Integer.parseInt(serverConfig.getValue(ServerDefs.KEY_PORT)));

        Thread onlineServerThread = new Thread(onlineServer);
        onlineServerThread.start();

        Scanner scan = new Scanner(System.in);
        String keyboardBuffer;

        do{
            keyboardBuffer = scan.nextLine();
        }while (!ServerUtils.parseKeyboardInput(keyboardBuffer));

        onlineServer.sendInterrupt();
        onlineServerThread.interrupt();

        BalanceListHandler.writeRegisteredBalances();
        ItemIndexListHandler.writeItemIndexMap();

        if(serverConfig.writeConfigs(ServerDefs.SERVER_CONFIG_FILEPATH)){
            Logger.log("Configs saved successfully!");
        }else{
            Logger.log("Configs save failed!");
        }

        Logger.log("--SERVER CLOSED--", new InfoFlags());

        return new State(0);
    }

    public static final class ServerDefs {

        //PATHS
        public static final String SERVER_DIRECTORY_PATH = "server/";
        public static final String SERVER_REGISTERED_BALANCES_FILEPATH = SERVER_DIRECTORY_PATH+"registeredBalances.txt";
        public static final String SERVER_ITEM_INDEX_FILEPATH = SERVER_DIRECTORY_PATH+"itemIndexes.txt";
        //Config
            public static final String SERVER_CONFIG_FILEPATH = SERVER_DIRECTORY_PATH+"config.cfg";
            //Keys
            public static final String KEY_PORT = "port";
            //Values
            public static final String VALUE_PORT = "25000";

        //Return Values
        public static final int CANNOT_READ_SERVER_REGISTERED_BALANCES_FILE = 4514;
        public static final int CANNOT_WRITE_SERVER_REGISTERED_BALANCES_FILE = 4516;
        public static final int CANNOT_CLOSE_SERVER_REGISTERED_BALANCES_FILE = 4515;
        public static final int CANNOT_CREATE_SERVER_REGISTERED_BALANCES_FILE = 4513;
        public static final int CANNOT_READ_SERVER_ITEM_INDEX_FILE = 4615;
        public static final int CANNOT_CREATE_SERVER_ITEM_INDEX_FILE = 4616;
        public static final int CANNOT_WRITE_SERVER_ITEM_INDEX_FILE = 4617;
        public static final int CANNOT_CLOSE_SERVER_ITEM_INDEX_FILE = 4618;
    }

}
