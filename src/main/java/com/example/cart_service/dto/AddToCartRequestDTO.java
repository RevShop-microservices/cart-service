package com.example.cart_service.dto;

import lombok.Data;

@Data
public class AddToCartRequestDTO {
    private String productId;
    private int quantity;
}