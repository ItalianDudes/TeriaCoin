package com.italiandudes.teriacoin;

import com.italianDudes.idl.common.InfoFlags;
import com.italianDudes.idl.common.Logger;
import com.italianDudes.idl.common.State;
import com.italiandudes.teriacoin.server.TeriaCoinServer;

import java.io.IOException;

public class TeriaCoin {

    public static void main(String[] args) {

        try {
            Logger.init();
        }catch (IOException e){
            System.err.println("Can't init Logger");
            System.exit(-1);
        }

        State outState = TeriaCoinServer.startServer();

        if(outState.getCode() != 0){
            Logger.log("Server terminated with an error code ["+outState.getCode()+"]: "+outState.getMessage(), new InfoFlags(true,false));
        }

        Logger.close();

    }

    public static final class Defs {

        //Commands
        public static final String COMMAND_STOP_SERVER = "stop";
        public static final String COMMAND_DISCONNECT_PEER = "disconnect";
        public static final String COMMAND_CONFIG = "config";
        public static final String COMMAND_CONFIG_GET = "get";
        public static final String COMMAND_CONFIG_SET = "set";
        public static final String[] COMMAND_CONFIG_SUB_COLLECTION = {COMMAND_CONFIG_GET, COMMAND_CONFIG_SET};
        public static final String COMMAND_CONFIG_KEY_PORT = "port";
        public static final String[] COMMAND_CONFIG_KEY_COLLECTION = {COMMAND_CONFIG_KEY_PORT};
        public static final String COMMAND_HELP = "help";
        public static final String COMMAND_CHECK_CONDITION = "check";
        public static final String COMMAND_CHECK_CONDITION_CONNECTED = "connected";
        public static final String[] COMMAND_CHECK_CONDITION_KEY_COLLECTION = {COMMAND_CHECK_CONDITION_CONNECTED};

        //Protocols
        public static final class TeriaProtocols {

            public static final int OK = 0;
            public static final int INVALID_PROTOCOL = -100;
            public static final String TERIA_REGISTER = "REGISTER";

            public static final class TeriaRegisterCodes {
                public static final int INVALID_USER = -1;
                public static final int INVALID_PASSWORD = -2;
                public static final int UNSAFE_PASSWORD= -3;
                public static final int ALREADY_EXIST = -4;
            }

            public static final String TERIA_LOGIN = "LOGIN";

            public static final class TeriaLoginCodes {
                public static final int INVALID_CREDENTIALS = -1;
                public static final int ALREADY_LOGGED_IN = -2;
            }
            public static final String TERIA_LOGOUT = "LOGOUT";
            public static final String TERIA_BALANCE = "BALANCE";
            public static final String TERIA_SEND = "SEND";

            public static final class TeriaSendCodes {
                public static final int INSUFFICIENT_TC_AVAILABLE = -1;
                public static final int USERNAME_DOES_NOT_EXIST = -2;
                public static final int INVALID_TC_AMOUNT = -3;
            }

        }

    }

}