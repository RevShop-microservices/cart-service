package com.example.cart_service.dto;

import lombok.Data;

@Data
public class UpdateCartItemDTO {
    private String productId;
    private int quantity;
}