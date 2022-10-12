package com.italiandudes.teriacoin.common.exception.socket;

import java.io.IOException;

@SuppressWarnings("unused")
public class InvalidProtocolException extends IOException {

    //Constructors
    public InvalidProtocolException(String message){
        super(message);
    }

}