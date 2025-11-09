package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.*;
import fpt.swp.springmvctt.itp.repository.*;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteProductServiceImpl implements FavoriteProductService {

    private final FavoriteProductRepository favoriteRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    @Transactional
    @Override
    public void addFavorite(String username, Long productId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean exists = favoriteRepo.findByUserAndProduct(user, product).isPresent();
        if (!exists) {
            FavoriteProduct fav = FavoriteProduct.builder()
                    .user(user)
                    .product(product)
                    .build();
            favoriteRepo.save(fav);
        }
    }

    @Transactional
    @Override
    public void removeFavorite(String username, Long productId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        favoriteRepo.deleteByUserAndProduct(user, product);
    }

    @Override
    public List<FavoriteProductDTO> getFavorites(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepo.findByUser(user).stream()
                .map(fav -> FavoriteProductDTO.builder()
                        .id(fav.getId())
                        .productId(fav.getProduct().getId())
                        .productName(fav.getProduct().getName())
                        .productImage(fav.getProduct().getImageUrl()) // field tuỳ bạn đặt
                        .createdAt(fav.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
