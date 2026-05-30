package com.example.cart_service.service;

import com.example.cart_service.client.ProductClient;
import com.example.cart_service.customexceptions.*;
import com.example.cart_service.dto.*;
import com.example.cart_service.model.Wishlist;
import com.example.cart_service.repository.WishlistRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishlistRepository wishlistRepository;

    private final CartService cartService;

    private final ProductClient productServiceClient;

    public WishlistResponseDTO addToWishlist(WishlistRequestDTO dto){
        ProductResponseDTO product = productServiceClient.getProductById(dto.getProductId());

        if (product == null) {
            throw new ProductNotFoundException("Product not found");
        }

        Wishlist wishlist = wishlistRepository.findByUserId(dto.getUserId())
                        .orElse(
                                Wishlist.builder()
                                        .userId(dto.getUserId())
                                        .productIds(new ArrayList<>())
                                        .build()
                        );

        if (!wishlist.getProductIds().contains(dto.getProductId())) {
            wishlist.getProductIds().add(dto.getProductId());
        }

        return mapToDTO(wishlistRepository.save(wishlist));
    }

    public WishlistResponseDTO removeFromWishlist(WishlistRequestDTO dto){
        Wishlist wishlist = wishlistRepository.findByUserId(dto.getUserId())
                        .orElseThrow(() -> new WishListNotFound("Wishlist not found"));

        wishlist.getProductIds().remove(dto.getProductId());
        return mapToDTO(wishlistRepository.save(wishlist));
    }

    public WishlistResponseDTO getWishlist(Long userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                        .orElse(
                                Wishlist.builder()
                                        .userId(userId)
                                        .productIds(new ArrayList<>())
                                        .build()
                        );

        return mapToDTO(wishlist);
    }

    @Transactional
    public void moveToCart(WishlistRequestDTO dto) {
        Wishlist wishlist = wishlistRepository.findByUserId(dto.getUserId())
                        .orElseThrow(() -> new WishListNotFound("Wishlist not found"));

        if (!wishlist.getProductIds().contains(dto.getProductId())) {
            throw new InvalidRequestException("Product not in wishlist");
        }

        cartService.addToCart(dto.getUserId(), dto.getProductId(), 1);

        wishlist.getProductIds().remove(dto.getProductId());
        wishlistRepository.save(wishlist);
    }

    public WishlistResponseDTO mapToDTO(Wishlist wishlist) {
        List<ProductResponseDTO> products = new ArrayList<>();
        if (wishlist.getProductIds() != null) {
            for (String productId : wishlist.getProductIds()) {
                try {
                    ProductResponseDTO product = productServiceClient.getProductById(productId);
                    if (product != null) {
                        products.add(product);
                    }
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(WishListService.class)
                            .warn("Product with ID {} could not be fetched or has been deleted from product-service: {}", productId, e.getMessage());
                }
            }
        }

        return WishlistResponseDTO.builder().userId(wishlist.getUserId())
                .products(products)
                .totalItems(products.size())
                .build();
    }
}