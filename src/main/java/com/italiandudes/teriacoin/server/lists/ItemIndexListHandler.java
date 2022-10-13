package com.italiandudes.teriacoin.server.lists;

import com.italianDudes.idl.common.Logger;
import com.italiandudes.teriacoin.common.ItemDescriptor;
import com.italiandudes.teriacoin.server.TeriaCoinServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public final class ItemIndexListHandler {

    //Attributes
    private static HashMap<Integer, ItemDescriptor> itemMap;

    //Constructors
    private ItemIndexListHandler() {
        throw new UnsupportedOperationException("Can't instantiate this class!");
    }

    //Methods
    public static void init(){
        itemMap = new HashMap<>();
        readItemIndexMap();
    }
    public static void readItemIndexMap(){

        File serverDir = new File(TeriaCoinServer.ServerDefs.SERVER_DIRECTORY_PATH);
        if(!serverDir.exists() || !serverDir.isDirectory()){
            //noinspection ResultOfMethodCallIgnored
            serverDir.mkdirs();
        }

        File itemIndexFile = new File(TeriaCoinServer.ServerDefs.SERVER_ITEM_INDEX_FILEPATH);

        if(itemIndexFile.exists() && itemIndexFile.isFile()) {

            Scanner inFile;
            try {
                inFile = new Scanner(itemIndexFile);
            }catch (FileNotFoundException ioException){
                inFile = null;
                Logger.log("Cannot read item indexes file!");
                Logger.log(ioException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_READ_SERVER_ITEM_INDEX_FILE);
            }

            int index;
            String itemID, itemName;
            double valueTC;

            while(inFile.hasNext()){
                index = Integer.parseInt(inFile.nextLine());
                itemID = inFile.nextLine();
                itemName = inFile.nextLine();
                valueTC = Double.parseDouble(inFile.nextLine());
                ItemIndexListHandler.registerItem(index, new ItemDescriptor(itemID, itemName, valueTC));
            }

            inFile.close();

        }else{

            try {
                //noinspection ResultOfMethodCallIgnored
                itemIndexFile.createNewFile();
            }catch (IOException e){
                Logger.log("Cannot create item indexes file!");
                Logger.log(e);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CREATE_SERVER_ITEM_INDEX_FILE);
            }

            FileWriter outFile;
            try {
                outFile = new FileWriter(itemIndexFile);
            }catch (IOException ioException){
                outFile = null;
                Logger.log("Cannot write item indexes file!");
                Logger.log(ioException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_ITEM_INDEX_FILE);
            }
            try {
                outFile.write("");
                outFile.flush();
            }catch (IOException ioException){
                System.err.println("Error during writing item indexes file!");
                try {
                    outFile.close();
                }catch (IOException closeIOException){
                    Logger.log("Error during writing item indexes file!");
                    Logger.log(closeIOException);
                    Logger.close();
                    System.exit(TeriaCoinServer.ServerDefs.CANNOT_CLOSE_SERVER_ITEM_INDEX_FILE);
                }
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_ITEM_INDEX_FILE);
            }
            try {
                outFile.close();
            }catch (IOException closeIOException){
                Logger.log("Cannot close item indexes file!");
                Logger.log(closeIOException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CLOSE_SERVER_REGISTERED_BALANCES_FILE);
            }
        }

    }
    public static void writeItemIndexMap(){

        File serverDir = new File(TeriaCoinServer.ServerDefs.SERVER_DIRECTORY_PATH);
        if(!serverDir.exists() || !serverDir.isDirectory()){
            //noinspection ResultOfMethodCallIgnored
            serverDir.mkdirs();
        }

        File registeredBalancesFile = new File(TeriaCoinServer.ServerDefs.SERVER_ITEM_INDEX_FILEPATH);
        if(!registeredBalancesFile.exists() || !registeredBalancesFile.isFile()){
            try {
                //noinspection ResultOfMethodCallIgnored
                registeredBalancesFile.createNewFile();
            }catch (IOException e){
                Logger.log("Cannot create item indexes file!");
                Logger.log(e);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CREATE_SERVER_ITEM_INDEX_FILE);
            }
        }

        FileWriter outFile;
        try {
            outFile = new FileWriter(registeredBalancesFile);
        }catch (IOException ioException){
            outFile = null;
            Logger.log("Cannot write item indexes file!");
            Logger.log(ioException);
            Logger.close();
            System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_ITEM_INDEX_FILE);
        }
        try {
            Set<Integer> indexes = itemMap.keySet();
            ItemDescriptor buffer;
            for(Integer integer : indexes){
                outFile.write(integer + "\n");
                buffer = itemMap.get(integer);
                outFile.write(buffer.getItemID() + "\n");
                outFile.write(buffer.getItemName() + "\n");
                outFile.write(buffer.getValueTC() + "\n");
            }
            outFile.flush();
        }catch (IOException ioException){
            Logger.log("Error during writing item indexes file!");
            Logger.log(ioException);
            try {
                outFile.close();
            }catch (IOException closeIOException){
                Logger.log("Error during writing item indexes file!");
                Logger.log(closeIOException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CLOSE_SERVER_ITEM_INDEX_FILE);
            }
            Logger.close();
            System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_ITEM_INDEX_FILE);
        }

    }
    @SuppressWarnings("UnusedReturnValue")
    public static boolean unregisterItem(Integer index){
        if(itemMap.get(index) == null){
            return false;
        }
        itemMap.remove(index);
        return true;
    }
    @SuppressWarnings("UnusedReturnValue")
    public static boolean registerItem(Integer index, ItemDescriptor item){
        if(itemMap.get(index) != null){
            return false;
        }
        itemMap.put(index, item);
        return true;
    }
    public static boolean contains(Integer index){
        return itemMap.get(index)!=null;
    }
    public synchronized static void clearList(){
        itemMap.clear();
    }
    public static boolean isEmpty(){
        return itemMap.isEmpty();
    }
    public static ItemDescriptor getItemID(Integer index){
        return itemMap.get(index);
    }
    public static Set<Integer> getKeySet(){
        return itemMap.keySet();
    }
    public static int size(){
        return itemMap.size();
    }

}
