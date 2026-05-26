package com.example.cart_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*;
//SQL instead of mongoDB
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;
    private Long userId;
    private List<CartItem> items=new ArrayList<>();
    private Double totalPrice=0.0;
}