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
                        .orElseThrow(() -> new WishListNotFound("Wishlist not found"));

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
        List<ProductResponseDTO> products =
                wishlist.getProductIds()
                        .stream()
                        .map(productServiceClient::getProductById)
                        .toList();

        return WishlistResponseDTO.builder().userId(wishlist.getUserId())
                .products(products)
                .totalItems(products.size())
                .build();
    }
}