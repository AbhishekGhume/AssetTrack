package com.abhishek.asset_manager.exceptions;

public class UserNotExistsException extends RuntimeException{

    public UserNotExistsException(String message) {
        super(message);
    }
}
