package com.example.cart_service.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private String productId;
    private String name;
    private Double price;
    private Integer quantity;
}