package com.italiandudes.teriacoin.server.lists;

import com.italianDudes.idl.common.Credential;
import com.italianDudes.idl.common.Logger;
import com.italiandudes.teriacoin.common.Balance;
import com.italiandudes.teriacoin.server.TeriaCoinServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

@SuppressWarnings("unused")
public final class BalanceListHandler {

    //Attributes
    private static HashMap<Credential, Balance> balanceMap;

    //Constructors
    private BalanceListHandler() {
        throw new UnsupportedOperationException("Can't instantiate this class!");
    }

    //Methods
    public static void init(){
        balanceMap = new HashMap<>();
        readRegisteredBalances();
    }
    public static void readRegisteredBalances(){

        File serverDir = new File(TeriaCoinServer.ServerDefs.SERVER_DIRECTORY_PATH);
        if(!serverDir.exists() || !serverDir.isDirectory()){
            //noinspection ResultOfMethodCallIgnored
            serverDir.mkdirs();
        }

        File registeredBalancesFile = new File(TeriaCoinServer.ServerDefs.SERVER_REGISTERED_BALANCES_FILEPATH);

        if(registeredBalancesFile.exists() && registeredBalancesFile.isFile()) {

            Scanner inFile;
            try {
                inFile = new Scanner(registeredBalancesFile);
            }catch (FileNotFoundException ioException){
                inFile = null;
                Logger.log("Cannot read registered balances!");
                Logger.log(ioException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_READ_SERVER_REGISTERED_BALANCES_FILE);
            }

            int numBalances = 0;
            try{
                numBalances = Integer.parseInt(inFile.nextLine());
            }catch (NumberFormatException ignored){}

            String username;
            String password;
            double balance;

            for(int i=0;i<numBalances;i++){
                username = inFile.nextLine();
                password = inFile.nextLine();
                balance = Double.parseDouble(inFile.nextLine());
                BalanceListHandler.registerBalance(new Credential(username,password,false), balance);
            }

            inFile.close();

        }else{

            try {
                //noinspection ResultOfMethodCallIgnored
                registeredBalancesFile.createNewFile();
            }catch (IOException e){
                Logger.log("Cannot create registered user list file!");
                Logger.log(e);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CREATE_SERVER_REGISTERED_BALANCES_FILE);
            }

            FileWriter outFile;
            try {
                outFile = new FileWriter(registeredBalancesFile);
            }catch (IOException ioException){
                outFile = null;
                Logger.log("Cannot write registered balances file!");
                Logger.log(ioException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_REGISTERED_BALANCES_FILE);
            }
            try {
                outFile.write("0");
                outFile.flush();
            }catch (IOException ioException){
                System.err.println("Error during writing registered balances file!");
                try {
                    outFile.close();
                }catch (IOException closeIOException){
                    Logger.log("Error during writing registered balances file!");
                    Logger.log(closeIOException);
                    Logger.close();
                    System.exit(TeriaCoinServer.ServerDefs.CANNOT_CLOSE_SERVER_REGISTERED_BALANCES_FILE);
                }
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_REGISTERED_BALANCES_FILE);
            }
            try {
                outFile.close();
            }catch (IOException closeIOException){
                Logger.log("Cannot close registered balances file!");
                Logger.log(closeIOException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CLOSE_SERVER_REGISTERED_BALANCES_FILE);
            }
        }

    }
    public static void writeRegisteredBalances(){

        File serverDir = new File(TeriaCoinServer.ServerDefs.SERVER_DIRECTORY_PATH);
        if(!serverDir.exists() || !serverDir.isDirectory()){
            //noinspection ResultOfMethodCallIgnored
            serverDir.mkdirs();
        }

        File registeredBalancesFile = new File(TeriaCoinServer.ServerDefs.SERVER_REGISTERED_BALANCES_FILEPATH);
        if(!registeredBalancesFile.exists() || !registeredBalancesFile.isFile()){
            try {
                //noinspection ResultOfMethodCallIgnored
                registeredBalancesFile.createNewFile();
            }catch (IOException e){
                Logger.log("Cannot create registered user list file!");
                Logger.log(e);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CREATE_SERVER_REGISTERED_BALANCES_FILE);
            }
        }

        FileWriter outFile;
        try {
            outFile = new FileWriter(registeredBalancesFile);
        }catch (IOException ioException){
            outFile = null;
            Logger.log("Cannot write registered user list file!");
            Logger.log(ioException);
            Logger.close();
            System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_REGISTERED_BALANCES_FILE);
        }
        try {
            outFile.write(balanceMap.size()+"\n");
            Set<Credential> credentials = balanceMap.keySet();
            for (Credential credential : credentials) {
                outFile.write(credential.getUsername() + "\n");
                outFile.write(credential.getPassword() + "\n");
                outFile.write(balanceMap.get(credential).getBalance() + "\n");
            }
            outFile.flush();
        }catch (IOException ioException){
            Logger.log("Error during writing registered balances file!");
            Logger.log(ioException);
            try {
                outFile.close();
            }catch (IOException closeIOException){
                Logger.log("Error during writing registered balances file!");
                Logger.log(closeIOException);
                Logger.close();
                System.exit(TeriaCoinServer.ServerDefs.CANNOT_CLOSE_SERVER_REGISTERED_BALANCES_FILE);
            }
            Logger.close();
            System.exit(TeriaCoinServer.ServerDefs.CANNOT_WRITE_SERVER_REGISTERED_BALANCES_FILE);
        }

    }
    public static boolean unregisterBalance(Credential credential){
        Balance balance = balanceMap.get(credential);
        if(balance == null){
            return false;
        }
        balanceMap.remove(credential);
        return true;
    }
    @SuppressWarnings("UnusedReturnValue")
    public static boolean registerBalance(Credential credential){
        Balance balanceExist = balanceMap.get(credential);
        if(balanceExist != null){
            return false;
        }
        balanceMap.put(credential, new Balance());
        return true;
    }
    public static boolean registerBalance(Credential credential, Balance balance){
        Balance balanceExist = balanceMap.get(credential);
        if(balanceExist != null){
            return false;
        }
        balanceMap.put(credential, balance);
        return true;
    }
    @SuppressWarnings("UnusedReturnValue")
    public static boolean registerBalance(Credential credential, double balance){
        Balance balanceExist = balanceMap.get(credential);
        if(balanceExist != null){
            return false;
        }
        balanceMap.put(credential, new Balance(balance));
        return true;
    }
    public static Set<Credential> getKeySet(){
        return balanceMap.keySet();
    }
    public static Credential getCredentialByUser(String user){
        Set<Credential> credentials = balanceMap.keySet();
        for(Credential credential : credentials){
            if(credential.getUsername().equals(user)){
                return credential;
            }
        }
        return null;
    }
    public static boolean contains(Credential credential){
        return balanceMap.get(credential)!=null;
    }
    public synchronized static void clearList(){
        balanceMap.clear();
    }
    public static boolean isEmpty(){
        return balanceMap.isEmpty();
    }
    public static Balance getBalance(Credential credential){
        return balanceMap.get(credential);
    }
    public static int size(){
        return balanceMap.size();
    }
}
