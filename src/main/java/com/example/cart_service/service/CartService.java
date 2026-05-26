package com.example.cart_service.service;

import com.example.cart_service.client.ProductClient;
import com.example.cart_service.customexceptions.*;
import com.example.cart_service.dto.*;
import com.example.cart_service.model.*;
import com.example.cart_service.repository.CartRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;
//import java.util.List;
//import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    private final ProductClient productServiceClient;

    public CartResponseDTO mapToDTO(Cart cart) {

        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                        .map(item -> CartItemDTO.builder()
                                .productId(item.getProductId())
                                .name(item.getName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .totalPrice(
                                        item.getPrice() *
                                                item.getQuantity()
                                )
                                .build())
                        .toList();

        int totalItems = itemDTOs.stream()
                        .mapToInt(CartItemDTO::getQuantity)
                        .sum();

        return CartResponseDTO.builder()
                .userId(cart.getUserId())
                .items(itemDTOs)
                .totalItems(totalItems)
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    public Cart getOrCreateCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() ->
                        cartRepository.save(
                                Cart.builder()
                                        .userId(userId)
                                        .items(new ArrayList<>())
                                        .totalPrice(0.0)
                                        .build()
                        )
                );
    }

    @Transactional
    public CartResponseDTO addToCart(Long userId, String productId, int quantity){
        if(quantity <= 0){
            throw new InvalidRequestException("Quantity must be greater than 0");
        }

        ProductResponseDTO product = productServiceClient.getProductById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found");
        }

        if(product.getStock() <= 0){
            throw new InvalidRequestException("Product out of stock");
        }

        if (quantity > product.getStock()) {
            throw new InvalidRequestException("Insufficient stock");
        }
        Cart cart = getOrCreateCart(userId);
        Optional<CartItem> existingItem = cart.getItems()
                        .stream()
                        .filter(item ->
                                item.getProductId().equals(productId))
                        .findFirst();

        if(existingItem.isPresent()){
            CartItem item = existingItem.get();
            int updatedQuantity = item.getQuantity() + quantity;

            if (updatedQuantity > product.getStock()) {
                throw new InvalidRequestException("Exceeds stock limit");
            }
            item.setQuantity(updatedQuantity);
        } else {
            CartItem item = CartItem.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build();

            cart.getItems().add(item);
        }

        recalculateTotal(cart);
        return mapToDTO(cartRepository.save(cart));
    }

    public CartResponseDTO viewCart(Long userId) {
        return mapToDTO(getOrCreateCart(userId));
    }

    public void recalculateTotal(Cart cart) {
        double total = 0.0;
        for (CartItem item : cart.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }
        cart.setTotalPrice(total);
    }

    @Transactional
    public CartResponseDTO updateQuantity(Long userId, String productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        CartItem item = cart.getItems()
                .stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Item not found"));

        ProductResponseDTO product = productServiceClient.getProductById(productId);

        if (quantity > product.getStock()) {
            quantity = product.getStock();
        }
        item.setQuantity(quantity);
        recalculateTotal(cart);

        return mapToDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDTO removeItem(Long userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new CartItemNotFoundException("Item not found");
        }

        recalculateTotal(cart);
        return mapToDTO(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

    public int getCartItemCount(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cart.getItems()
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}