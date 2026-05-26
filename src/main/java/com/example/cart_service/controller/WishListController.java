package com.example.cart_service.controller;

import com.example.cart_service.dto.*;
//import com.Revature.Ecommerce.Platform.dto.WishlistResponseDTO;
//import com.Revature.Ecommerce.Platform.service.WishListService;
import com.example.cart_service.service.WishListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist Controller", description = "APIs for managing wishlist")
public class WishListController {

    @Autowired
    private WishListService service;

    @PostMapping("/add")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<WishlistResponseDTO> add(
            @RequestBody WishlistRequestDTO dto) {

        return ResponseEntity.ok(service.addToWishlist(dto));
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<WishlistResponseDTO> remove(
            @RequestBody WishlistRequestDTO dto) {

        return ResponseEntity.ok(service.removeFromWishlist(dto));
    }

    @GetMapping
    @Operation(summary = "Get wishlist")
    public ResponseEntity<WishlistResponseDTO> getWishlist(
            @RequestParam Long userId) {

        return ResponseEntity.ok(service.getWishlist(userId));
    }

    @PostMapping("/move-to-cart")
    @Operation(summary = "Move product from wishlist to cart")
    public ResponseEntity<String> moveToCart(
            @RequestBody WishlistRequestDTO dto) {

        service.moveToCart(dto);
        return ResponseEntity.ok("Product moved to cart successfully");
    }
}