package com.italiandudes.teriacoin.server.threads;

import com.italianDudes.idl.common.*;
import com.italiandudes.teriacoin.common.exception.socket.InvalidProtocolException;
import com.italiandudes.teriacoin.TeriaCoin.Defs;
import com.italiandudes.teriacoin.server.lists.BalanceListHandler;
import com.italiandudes.teriacoin.server.lists.PeerList;

import java.io.IOException;

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

                    case Defs.TeriaProtocols.TERIA_SEND:
                        try {

                            String destinationUser = Serializer.receiveString(peer);
                            double amountTC = Serializer.receiveDouble(peer);
                            if(amountTC<=0){
                                Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaSendCodes.INVALID_TC_AMOUNT);
                            }else {
                                if(BalanceListHandler.getBalance(peer.getCredential()).getBalance() < amountTC){
                                    Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaSendCodes.INSUFFICIENT_TC_AVAILABLE);
                                }else {
                                    Credential destinationCredential = BalanceListHandler.getCredentialByUser(destinationUser);
                                    if (destinationCredential == null) {
                                        Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaSendCodes.USERNAME_DOES_NOT_EXIST);
                                    } else {
                                        BalanceListHandler.getBalance(destinationCredential).setBalance(BalanceListHandler.getBalance(destinationCredential).getBalance()+amountTC);
                                        BalanceListHandler.getBalance(peer.getCredential()).setBalance(BalanceListHandler.getBalance(peer.getCredential()).getBalance()-amountTC);
                                        Serializer.sendInt(peer, Defs.TeriaProtocols.OK);
                                    }
                                }
                            }

                        }catch (IOException e){
                            hasError = true;
                            throwable = e;
                        }
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