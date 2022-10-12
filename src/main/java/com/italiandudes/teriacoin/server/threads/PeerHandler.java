package com.italiandudes.teriacoin.server.threads;

import com.italianDudes.idl.common.*;
import com.italiandudes.teriacoin.common.exception.socket.InvalidProtocolException;
import com.italiandudes.teriacoin.TeriaCoin.Defs;
import com.italiandudes.teriacoin.server.lists.BalanceListHandler;
import com.italiandudes.teriacoin.server.lists.PeerList;

public class PeerHandler implements Runnable {

    //Attributes
    private final Peer peer;

    //Constructors
    public PeerHandler(Peer peer){
        this.peer = peer;
    }

    //Methods
    @Override
    public void run() {

        String request = null;

        boolean connectionClosed = false;
        boolean hasError = false;
        Throwable throwable = null;

        while(!connectionClosed && !hasError){

            try {
                request = Serializer.receiveString(peer);
                Logger.log("["+peer.getCredential().getUsername()+"] "+request);
            }catch (Exception e){
                hasError = true;
                throwable = e;
            }

            if(!hasError) {
                switch (request) {

                    case Defs.TeriaProtocols.TERIA_BALANCE:
                        try {
                            Serializer.sendDouble(peer, BalanceListHandler.getBalance(peer.getCredential()).getBalance());
                        }catch (Exception e){
                            hasError = true;
                            throwable = e;
                        }
                        break;

                    case Defs.TeriaProtocols.TERIA_LOGOUT:
                        connectionClosed = true;
                        break;

                    default:
                        hasError = true;
                        throwable = new InvalidProtocolException("Protocol non respected!");
                        break;

                }
            }

        }

        if(hasError){
            Logger.log("Connection with peer terminated:\n"+ StringHandler.getStackTrace(throwable), new InfoFlags(true,false));
        }

        PeerList.removePeer(peer);

    }
}