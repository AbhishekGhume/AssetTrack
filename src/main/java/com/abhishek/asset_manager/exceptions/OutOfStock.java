package com.abhishek.asset_manager.exceptions;

public class OutOfStock extends RuntimeException {
    public OutOfStock(String message) {
        super(message);
    }
}
