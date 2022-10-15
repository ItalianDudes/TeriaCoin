package com.italiandudes.teriacoin.server.threads;

import com.italianDudes.idl.common.*;
import com.italiandudes.teriacoin.TeriaCoin.Defs;
import com.italiandudes.teriacoin.common.Balance;
import com.italiandudes.teriacoin.common.ItemDescriptor;
import com.italiandudes.teriacoin.common.exception.socket.InvalidProtocolException;
import com.italiandudes.teriacoin.server.lists.BalanceListHandler;
import com.italiandudes.teriacoin.server.lists.ItemIndexListHandler;
import com.italiandudes.teriacoin.server.lists.PeerList;

import java.io.IOException;
import java.util.Set;

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
                Logger.log("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] "+peer.getCredential().getUsername()+": "+request);
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

                    case Defs.TeriaProtocols.TERIA_EXCHANGE_TC:
                        try {
                            int index = Serializer.receiveInt(peer);
                            int amount = Serializer.receiveInt(peer);

                            if(!ItemIndexListHandler.contains(index)){
                                Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaExchangeTCCodes.ITEM_INDEX_NOT_FOUND);
                            }else{
                                double itemValue = ItemIndexListHandler.getItemID(index).getValueTC();
                                double finalCost = itemValue*amount;
                                if(BalanceListHandler.getBalance(peer.getCredential()).getBalance() < finalCost){
                                    Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaExchangeTCCodes.INSUFFICIENT_TC_AVAILABLE);
                                }else{
                                    BalanceListHandler.getBalance(peer.getCredential()).setBalance(BalanceListHandler.getBalance(peer.getCredential()).getBalance()-finalCost);
                                    Serializer.sendInt(peer, Defs.TeriaProtocols.OK);
                                    Serializer.sendString(peer, ItemIndexListHandler.getItemID(index).getItemID());
                                    Serializer.sendInt(peer, amount);
                                    Logger.log("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] Decreased \""+peer.getCredential().getUsername()+"\" balance of "+finalCost+"TC");
                                }
                            }
                        }catch (IOException e){
                            hasError = true;
                            throwable = e;
                        }
                        break;

                    case Defs.TeriaProtocols.TERIA_EXCHANGE_ITEM:
                        try {
                            int index = Serializer.receiveInt(peer);

                            if(!ItemIndexListHandler.contains(index)){
                                Serializer.sendBoolean(peer, false);
                            }else{
                                Serializer.sendBoolean(peer, true);
                                Serializer.sendString(peer, ItemIndexListHandler.getItemID(index).getItemID());

                                int result = Serializer.receiveInt(peer);

                                switch (result){

                                    case Defs.TeriaProtocols.OK:
                                        double amount = Serializer.receiveInt(peer);
                                        double itemValue = ItemIndexListHandler.getItemID(index).getValueTC();
                                        double finalValue = itemValue*amount;
                                        BalanceListHandler.getBalance(peer.getCredential()).setBalance(BalanceListHandler.getBalance(peer.getCredential()).getBalance()+finalValue);
                                        Logger.log("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] Increased \""+peer.getCredential().getUsername()+"\" balance of "+finalValue+"TC");
                                        break;

                                    case Defs.TeriaProtocols.TeriaExchangeItemCodes.MISSING_REQUESTED_ITEM_AMOUT:
                                        break;

                                    case Defs.TeriaProtocols.TeriaExchangeItemCodes.ITEM_INDEX_NOT_FOUND:
                                        break;

                                    default:
                                        throw new InvalidProtocolException("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] Protocol non respected by \""+peer.getCredential().getUsername()+"\"!");

                                }

                            }
                        }catch (IOException e){
                            hasError = true;
                            throwable = e;
                        }
                        break;

                    case Defs.TeriaProtocols.TERIA_EXCHANGE_LIST:
                        try {
                            Serializer.sendInt(peer, ItemIndexListHandler.size());
                            Set<Integer> keySet = ItemIndexListHandler.getKeySet();

                            for(Integer integer : keySet){
                                ItemDescriptor buffer = ItemIndexListHandler.getItemID(integer);
                                Serializer.sendInt(peer, integer);
                                Serializer.sendString(peer, buffer.getItemName());
                                Serializer.sendDouble(peer, buffer.getValueTC());
                            }

                        }catch (IOException e){
                            hasError = true;
                            throwable = e;
                        }
                        break;

                    case Defs.TeriaProtocols.TERIA_UNREGISTER:
                        try {
                            String password = Serializer.receiveString(peer);
                            if(!password.equals(peer.getCredential().getPassword())){
                                Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaUnregisterCodes.PASSWORD_MISMATCH);
                            }else{
                                BalanceListHandler.unregisterBalance(peer.getCredential());
                                Serializer.sendInt(peer, Defs.TeriaProtocols.OK);
                                connectionClosed = true;
                                Logger.log("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] Unregistered \""+peer.getCredential().getUsername()+"\"");
                            }

                        }catch (IOException e){
                            hasError = true;
                            throwable = e;
                        }
                        break;

                    case Defs.TeriaProtocols.TERIA_CHANGE_PASSWORD:
                        try{
                            String password = Serializer.receiveString(peer);
                            String newPassword = Serializer.receiveString(peer);
                            String confirmNewPassword = Serializer.receiveString(peer);

                            if(!password.equals(peer.getCredential().getPassword())){
                                Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaChangePasswordCodes.PASSWORD_MISMATCH);
                            }else if(!newPassword.equals(confirmNewPassword)){
                                Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaChangePasswordCodes.NEW_PASSWORD_AND_CONFIRM_MISMATCH);
                            }else if(!password.equals(newPassword)){
                                Serializer.sendInt(peer, Defs.TeriaProtocols.TeriaChangePasswordCodes.OLD_AND_NEW_PASSWORD_ARE_EQUALS);
                            }else{
                                Balance userBalance = BalanceListHandler.getBalance(peer.getCredential());
                                BalanceListHandler.unregisterBalance(peer.getCredential());
                                BalanceListHandler.registerBalance(new Credential(peer.getCredential().getUsername(), newPassword, false), userBalance);
                                Serializer.sendInt(peer, Defs.TeriaProtocols.OK);
                                connectionClosed = true;
                                Logger.log("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] \""+peer.getCredential().getUsername()+"\" has changed password, disconnecting...");
                            }

                        }catch (IOException e){
                            hasError = true;
                            throwable = e;
                        }
                        break;

                    default:
                        hasError = true;
                        throwable = new InvalidProtocolException("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] Protocol non respected by \""+peer.getCredential().getUsername()+"\"!");
                        break;

                }
            }

        }

        if(hasError){
            Logger.log("["+ peer.getPeerSocket().getInetAddress().getHostAddress()+":"+peer.getPeerSocket().getPort() +"] Connection with peer \""+peer.getCredential().getUsername()+"\" terminated:\n"+ StringHandler.getStackTrace(throwable), new InfoFlags(true,false));
        }

        PeerList.removePeer(peer);

    }
}