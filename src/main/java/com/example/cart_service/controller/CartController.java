package com.example.cart_service.controller;

import com.example.cart_service.dto.*;
//import com.Revature.Ecommerce.Platform.dto.CartResponseDTO;
//import com.Revature.Ecommerce.Platform.dto.UpdateCartItemDTO;
import com.example.cart_service.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart Controller", description = "APIs for managing user cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService service;

    @PostMapping("/add")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<CartResponseDTO> addToCart(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @RequestBody AddToCartRequestDTO dto) {

        log.info("API: Add to cart | userId={} productId={} quantity={}",
                userId, dto.getProductId(), dto.getQuantity());
        return ResponseEntity.ok(service.addToCart(userId, dto.getProductId(), dto.getQuantity())
        );
    }

    @GetMapping
    @Operation(summary = "View user's cart")
    public ResponseEntity<CartResponseDTO> viewCart(
            @Parameter(description = "User ID") @RequestParam Long userId) {

        return ResponseEntity.ok(service.viewCart(userId));
    }

    @PutMapping("/update")
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @RequestBody UpdateCartItemDTO dto) {

        return ResponseEntity.ok(
                service.updateQuantity(userId, dto.getProductId(), dto.getQuantity())
        );
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponseDTO> removeItem(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Product ID") @RequestParam String productId) {

        return ResponseEntity.ok(service.removeItem(userId, productId));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<String> clearCart(
            @Parameter(description = "User ID") @RequestParam Long userId) {

        service.clearCart(userId);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}