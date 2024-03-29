package com.italiandudes.teriacoin.server.threads;

import com.italianDudes.idl.common.Credential;
import com.italianDudes.idl.common.Logger;
import com.italianDudes.idl.common.Peer;
import com.italianDudes.idl.common.Serializer;
import com.italiandudes.teriacoin.TeriaCoin.Defs.TeriaProtocols;
import com.italiandudes.teriacoin.common.exception.socket.InvalidProtocolException;
import com.italiandudes.teriacoin.server.TeriaCoinServer;
import com.italiandudes.teriacoin.server.lists.BalanceListHandler;
import com.italiandudes.teriacoin.server.lists.PeerList;

import java.io.IOException;
import java.net.Socket;

public class Authenticator implements Runnable {

    //Attributes
    private final Socket connection;

    //Constructors
    public Authenticator(Socket connection){
        this.connection = connection;
    }

    //Methods
    @Override
    public void run() {
        if(connection!=null) {

            Peer tempPeer = new Peer(connection);

            try {
                String protocol = Serializer.receiveString(tempPeer);
                String username = Serializer.receiveString(tempPeer);
                String password = Serializer.receiveString(tempPeer);
                String clientVersion = Serializer.receiveString(tempPeer);

                if (!TeriaCoinServer.getConfig().getValue(TeriaCoinServer.ServerDefs.KEY_CLIENT_VERSION).equals(clientVersion)) {
                    Serializer.sendInt(tempPeer, TeriaProtocols.OUTDATED);
                    Logger.log("["+tempPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+tempPeer.getPeerSocket().getPort()+"] User that attempted to join as \"" + username + "\" has an outdated client, connection rejected!");
                    tempPeer.getPeerSocket().close();
                    return;
                }

                Credential credential = new Credential(username, password, false);
                tempPeer = new Peer(connection, credential);

                boolean disconnect = false;

                switch (protocol){

                    case TeriaProtocols.TERIA_LOGIN:
                        if(BalanceListHandler.contains(credential)) {
                            Peer authenticatedPeer = new Peer(connection, credential);
                            if (PeerList.addPeer(authenticatedPeer)) {
                                Logger.log("["+authenticatedPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+authenticatedPeer.getPeerSocket().getPort()+"] Logged in with \""+ authenticatedPeer.getCredential().getUsername() +"\"!");
                                Serializer.sendInt(authenticatedPeer, TeriaProtocols.OK);
                                new Thread(new PeerHandler(authenticatedPeer)).start();
                            } else {
                                Serializer.sendInt(tempPeer, TeriaProtocols.TeriaLoginCodes.ALREADY_LOGGED_IN);
                                Logger.log("["+authenticatedPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+authenticatedPeer.getPeerSocket().getPort()+"] User \"" + authenticatedPeer.getCredential().getUsername() + "\" already connected!");
                                disconnect = true;
                            }
                        }else{
                            Serializer.sendInt(tempPeer, TeriaProtocols.TeriaLoginCodes.INVALID_CREDENTIALS);
                            Logger.log("["+tempPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+tempPeer.getPeerSocket().getPort()+"] User \"" + credential.getUsername() + "\" isn't registered or combination username/password is wrong!");
                            disconnect = true;
                        }
                        break;

                    case TeriaProtocols.TERIA_REGISTER:
                        if(BalanceListHandler.contains(credential)){
                            Logger.log("["+"["+tempPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+tempPeer.getPeerSocket().getPort()+"] Username \""+tempPeer.getCredential().getUsername()+"\" already exist!");
                            Serializer.sendInt(tempPeer, TeriaProtocols.TeriaRegisterCodes.ALREADY_EXIST);
                        }else{
                            //TODO: Test vari su username
                            Logger.log("["+tempPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+tempPeer.getPeerSocket().getPort()+"] User \""+tempPeer.getCredential().getUsername()+"\" registered! Closing connection");
                            Serializer.sendInt(tempPeer, TeriaProtocols.OK);
                            BalanceListHandler.registerBalance(tempPeer.getCredential());
                        }
                        disconnect = true;
                        break;

                    default:
                        try {
                            Serializer.sendInt(tempPeer, TeriaProtocols.INVALID_PROTOCOL);
                        }catch (Exception ignored){}
                        throw new InvalidProtocolException("["+ tempPeer.getPeerSocket().getInetAddress().getHostAddress()+":"+tempPeer.getPeerSocket().getPort() +"] Protocol non respected by \""+tempPeer.getCredential().getUsername()+"\"!");
                }

                if(disconnect){
                    try{
                        connection.close();
                    }catch (IOException ignored){}
                }

            }catch (IOException e){
                Logger.log(e);
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }

        }
    }
}