package com.italiandudes.teriacoin.server;

import com.italianDudes.idl.common.InfoFlags;
import com.italianDudes.idl.common.Logger;
import com.italianDudes.idl.common.StringHandler;
import com.italiandudes.teriacoin.TeriaCoin.Defs;
import com.italiandudes.teriacoin.common.ItemDescriptor;
import com.italiandudes.teriacoin.server.lists.ItemIndexListHandler;
import com.italiandudes.teriacoin.server.lists.PeerList;

import java.util.Arrays;

public final class ServerUtils {

    //Constructors
    private ServerUtils(){
        throw new UnsupportedOperationException("Can't instantiate this class!");
    }

    //Methods
    public static boolean parseKeyboardInput(String input) {

        if (input.equals(Defs.COMMAND_STOP_SERVER)) {
            return true;
        } else {

            String[] parsedCommand = StringHandler.parseString(input);

            if (parsedCommand != null) {

                switch (parsedCommand[0]) {

                    case Defs.COMMAND_DISCONNECT_PEER:
                        if(parsedCommand.length<2){
                            printErrorMessage(parsedCommand[0]);
                        }else{

                            for(int i=1;i< parsedCommand.length;i++){
                                if(PeerList.removePeer(parsedCommand[i])){
                                    Logger.log("User \""+parsedCommand[i]+"\" disconnected!");
                                }else{
                                    Logger.log("Can't disconnect \""+parsedCommand[i]+"\": this user isn't connected");
                                }
                            }

                        }
                        break;

                    case Defs.COMMAND_CONFIG:
                        if(parsedCommand.length<3){
                            printErrorMessage(parsedCommand[0]);
                        }else {

                            String configValue;

                            switch (parsedCommand[1]){

                                case Defs.COMMAND_CONFIG_GET:

                                    for(int i = 2; i < parsedCommand.length; i++){
                                        configValue = TeriaCoinServer.getConfig().getValue(parsedCommand[i]);
                                        if(configValue==null){
                                            Logger.log("Key \""+parsedCommand[i]+"\" doesn't exist");
                                        }else{
                                            Logger.log("Value at key \""+parsedCommand[i]+"\": "+configValue);
                                        }
                                    }
                                    break;

                                case Defs.COMMAND_CONFIG_SET:

                                    String key;
                                    String newValue;
                                    boolean commandFinished = false;
                                    boolean isSomethingChanged = false;

                                    for(int i=2;i< parsedCommand.length && !commandFinished;i++){

                                        key = parsedCommand[i];
                                        configValue = TeriaCoinServer.getConfig().getValue(parsedCommand[i]);
                                        i++;

                                        try {
                                            newValue = parsedCommand[i];
                                            if(TeriaCoinServer.getConfig().setValue(key,newValue)){
                                                Logger.log("Value for key \""+key+"\" updated from \""+configValue+"\" to \""+newValue+"\"");
                                                isSomethingChanged = true;
                                            }else{
                                                Logger.log("Updating value failed for key \""+key+"\", the value will remain \""+configValue+"\"");
                                            }
                                        }catch (ArrayIndexOutOfBoundsException e){
                                            Logger.log("Value for key \""+parsedCommand[i]+"\" not provided");
                                            commandFinished = true;
                                        }
                                    }

                                    if(isSomethingChanged){
                                        Logger.log("NOTE: Some of this changes may be applied to the next reboot to avoid conflicts. A reboot is highly suggested");
                                    }
                                    break;

                                default:
                                    printErrorMessage(parsedCommand[0]+" "+parsedCommand[1]);
                                    break;

                            }

                        }
                        break;

                    case Defs.COMMAND_CHECK_CONDITION:
                        if(parsedCommand.length<2){
                            printErrorMessage(parsedCommand[0]);
                        }else{

                            switch (parsedCommand[1]){

                                case Defs.COMMAND_CHECK_CONDITION_CONNECTED:
                                    if(parsedCommand.length<3){
                                        printErrorMessage(parsedCommand[0]+" "+parsedCommand[1]);
                                    }else {
                                        for (int i = 2; i < parsedCommand.length; i++){
                                            Logger.log(parsedCommand[i]+": "+PeerList.isConnected(parsedCommand[i]));
                                        }
                                    }
                                    break;

                                default:
                                    Logger.log("Invalid check type: \""+parsedCommand[1]+"\" doesn't exist");
                                    break;

                            }

                        }
                        break;

                    case Defs.COMMAND_ADD_ITEM:
                        if(parsedCommand.length<5){
                            printErrorMessage(parsedCommand[0]);
                        }else{
                            int index = Integer.parseInt(parsedCommand[1]);
                            String itemID = parsedCommand[2];
                            String itemName = parsedCommand[3];
                            double itemValue = Double.parseDouble(parsedCommand[4]);
                            if(ItemIndexListHandler.contains(index)){
                                Logger.log("Can't add item: index already registered");
                            }else{
                                ItemIndexListHandler.registerItem(index, new ItemDescriptor(itemID, itemName, itemValue));
                                Logger.log("Item registered successfully!");
                            }
                        }
                        break;

                    case Defs.COMMAND_REMOVE_ITEM:
                        if(parsedCommand.length<2){
                            printErrorMessage(parsedCommand[0]);
                        }else{
                            for(int i=1;i<parsedCommand.length;i++){
                                if(ItemIndexListHandler.contains(Integer.parseInt(parsedCommand[i]))){
                                    ItemIndexListHandler.unregisterItem(Integer.parseInt(parsedCommand[i]));
                                    Logger.log("Unregistered "+parsedCommand[i]);
                                }else{
                                    Logger.log("Index "+parsedCommand[i]+" it's not registered");
                                }
                            }
                        }
                        break;

                    case Defs.COMMAND_HELP:
                        showHelpMessage();
                        break;

                    default:
                        Logger.log("Invalid keyboard input: " + Arrays.toString(parsedCommand));
                        Logger.log("Type \""+Defs.COMMAND_HELP+"\" for the command list");
                        break;

                }

            } else {
                Logger.log("Input command recognizing failed!", new InfoFlags(true, false));
            }

        }

        return false;

    }
    private static void printErrorMessage(String errorCommand){
        Logger.log("Wrong use of \""+errorCommand+"\", type \""+Defs.COMMAND_HELP+"\" for the command list");
    }
    public static void showHelpMessage(){

        System.out.println("COMMAND LIST:");
            System.out.println("\t-"+Defs.COMMAND_STOP_SERVER+":");
                System.out.println("\t\t-Shut the server down disconnecting all connected users");
                System.out.println("\t\t-Syntax: "+Defs.COMMAND_STOP_SERVER);
            System.out.println("\t-"+Defs.COMMAND_DISCONNECT_PEER+":");
                System.out.println("\t\t-Disconnect user(s) from the server");
                System.out.println("\t\t-Syntax: "+Defs.COMMAND_DISCONNECT_PEER+" <user1> [user2] ... [userN]");
            System.out.println("\t-"+Defs.COMMAND_CHECK_CONDITION +":");
                System.out.println("\t\t-Diagnostic tool that verify a condition showing a boolean message");
                System.out.println("\t\t-Conditions:");
                for(String subtype : Defs.COMMAND_CHECK_CONDITION_KEY_COLLECTION){
                    System.out.println("\t\t\t-"+subtype);
                }
                System.out.println("\t\tSyntax: "+Defs.COMMAND_CHECK_CONDITION+" <condition> [parameter1] [parameter2] ... [parameterN]");
            System.out.println("\t-"+Defs.COMMAND_CONFIG+":");
                System.out.println("\t\t-Tool for view or change config values");
                System.out.println("\t\t-Available subcommands:");
                for(String subtype : Defs.COMMAND_CONFIG_SUB_COLLECTION){
                    System.out.println("\t\t\t-"+subtype);
                }
                System.out.println("\t\t-Available keys:");
                for(String key : Defs.COMMAND_CONFIG_KEY_COLLECTION){
                    System.out.println("\t\t\t-"+key);
                }
                System.out.println("\t\tSyntax: "+Defs.COMMAND_CONFIG+" <subcommand> <key> [newValue]");
        System.out.println("\t-"+Defs.COMMAND_ADD_ITEM +":");
            System.out.println("\t\t-Register a new Item");
            System.out.println("\t\tSyntax: "+Defs.COMMAND_ADD_ITEM +" <index> <itemID> <itemName> <itemValue>");
        System.out.println("\t-"+Defs.COMMAND_REMOVE_ITEM +":");
            System.out.println("\t\t-Unregister an existing Item providing the index");
            System.out.println("\t\tSyntax: "+Defs.COMMAND_REMOVE_ITEM +" <index> [index1] ... [indexN]");

    }

}