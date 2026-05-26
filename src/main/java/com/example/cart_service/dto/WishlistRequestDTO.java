package com.example.cart_service.dto;

import lombok.*;

@Data
public class WishlistRequestDTO {
    private Long userId;
    private String productId;
}
