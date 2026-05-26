package com.example.cart_service.dto;

import lombok.*;
import java.util.*;

@Data
@Builder
public class CartResponseDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private int totalItems;
    private Double totalPrice;
}