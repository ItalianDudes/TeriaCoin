package com.italiandudes.teriacoin.common;

import com.italianDudes.idl.common.Logger;
import com.italiandudes.teriacoin.server.TeriaCoinServer;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class Config {

    //Attributes
    private final ArrayList<ConfigData> configMap;

    //Class Definition
    private static class ConfigData {

        //Attributes
        private String key;
        private String value;

        //Constructors
        public ConfigData(String key, String value){
            this.key = key;
            this.value = value;
        }

        //Methods
        public String getKey() {
            return key;
        }
        public void setKey(String key){
            this.key = key;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value){
            this.value = value;
        }
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof ConfigData))
                return false;
            ConfigData configData = (ConfigData) obj;
            return configData.value.equals(this.value) && configData.key.equals(this.key);
        }
        @Override
        public String toString() {
            return key+"="+value;
        }
    }

    //Constructors
    public Config(){
        configMap = new ArrayList<>();
    }
    public Config(ArrayList<ConfigData> configMap){
        this.configMap = configMap;
    }

    //Methods
    public String getValue(String key){
        for(ConfigData configData : configMap){
            if(configData.key.equals(key))
                return configData.value;
        }
        return null;
    }
    public boolean put(String key, String value){
        ConfigData configData = new ConfigData(key, value);
        for(ConfigData configDataElementList : configMap){
            if(configDataElementList.equals(configData))
                return false;
        }
        return configMap.add(configData);
    }
    public boolean containsValue(String value){
        for(ConfigData configData : configMap){
            if(configData.value.equals(value))
                return true;
        }
        return false;
    }
    public boolean containsKey(String key){
        for(ConfigData configData :  configMap){
            if(configData.key.equals(key))
                return true;
        }
        return false;
    }
    public void setDefault(){
        configMap.add(new ConfigData(TeriaCoinServer.ServerDefs.KEY_PORT, TeriaCoinServer.ServerDefs.VALUE_PORT));
        configMap.add(new ConfigData(TeriaCoinServer.ServerDefs.KEY_CLIENT_VERSION, TeriaCoinServer.ServerDefs.VALUE_CLIENT_VERSION));
    }
    public void dumpConfigs(){
        configMap.clear();
    }
    public boolean readConfigs(String filePath){
        return readConfigs(new File(filePath));
    }
    public boolean setValue(String key, String value){
        for(ConfigData config : configMap){
            if(config.key.equals(key)){
                config.setValue(value);
                return true;
            }
        }
        return false;
    }
    public boolean readConfigs(File filePointer){

        Scanner inFile;

        try{
            inFile = new Scanner(filePointer);
        }catch (FileNotFoundException e){
            this.setDefault();
            return false;
        }

        try {

            String key, value;

            do {

                key = inFile.nextLine();
                if (inFile.hasNext()) {
                    value = inFile.nextLine();
                    configMap.add(new ConfigData(key, value));
                }

            } while (inFile.hasNext());

            inFile.close();
            return true;

        }catch (Exception e){
            Logger.log(e);
            inFile.close();
            configMap.clear();
            return false;
        }

    }
    public boolean writeConfigs(String filePath){
        return writeConfigs(new File(filePath));
    }
    public boolean writeConfigs(File filePointer){

        try {
            if(filePointer.createNewFile()){
                Logger.log("Creating the config file for incoming writing...");
            }
        }catch (IOException e){
            Logger.log(e);
            return false;
        }
        BufferedWriter outFile;
        try {
            outFile = new BufferedWriter(new FileWriter(filePointer));
        }catch (IOException e){
            Logger.log(e);
            return false;
        }

        try{

            for(ConfigData configData : configMap){
                outFile.append(configData.key).append("\n").append(configData.value).append("\n");
            }

            outFile.flush();
            outFile.close();
            return true;

        }catch (Exception e){
            Logger.log(e);
            try {
                outFile.close();
            }catch (IOException ignored){}
            configMap.clear();
            return false;
        }

    }
}