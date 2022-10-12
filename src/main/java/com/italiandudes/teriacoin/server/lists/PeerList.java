package com.italiandudes.teriacoin.server.lists;

import com.italianDudes.idl.common.Peer;

import java.io.IOException;
import java.util.ArrayList;

public final class PeerList {

    //Attributes
    private final static ArrayList<Peer> clientList = new ArrayList<>();

    //Constructors
    private PeerList(){
        throw new UnsupportedOperationException("Can't instantiate this class!");
    }

    //Methods
    public static Peer getPeer(int index){
        return clientList.get(index);
    }
    public static boolean addPeer(Peer peer){
        if(peer==null)
            return false;
        for(Peer clientPeer : clientList){
            if(clientPeer.equals(peer))
                return false;
        }
        return clientList.add(peer);
    }
    public static boolean isConnected(String username){
        if(username == null)
            return false;
        for(Peer peer : clientList){
            if(peer.getCredential().getUsername().equals(username))
                return true;
        }
        return false;
    }
    public static boolean removePeer(String username){
        if(username == null)
            return false;
        for(Peer peer : clientList){
            if(peer.getCredential().getUsername().equals(username)){
                return removePeer(peer);
            }
        }
        return false;
    }
    public static boolean removePeer(Peer peer){
        if(peer == null)
            return false;
        for(int i=0;i<clientList.size();i++){
            if(clientList.get(i).equals(peer)){
                try {
                    clientList.get(i).getPeerSocket().close();
                }catch (IOException ignored){}
                clientList.remove(i);
                return true;
            }
        }
        return false;
    }
    public static void disconnectAll(){
        for (Peer peer : clientList) {
            try {
                peer.getPeerSocket().close();
            } catch (IOException ignored) {
            }
        }
        clientList.clear();
    }
    public static int getListSize(){
        return clientList.size();
    }
    public static int getActiveConnectionsNumber(){
        int activeConnections = 0;
        for (Peer peer : clientList) {
            if (peer.getPeerSocket().isConnected())
                activeConnections++;
        }
        return activeConnections;
    }

}