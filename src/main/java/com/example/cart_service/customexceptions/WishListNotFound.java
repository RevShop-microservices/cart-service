package com.example.cart_service.customexceptions;

public class WishListNotFound extends RuntimeException{
    public WishListNotFound(String message){
        super(message);
    }
}
