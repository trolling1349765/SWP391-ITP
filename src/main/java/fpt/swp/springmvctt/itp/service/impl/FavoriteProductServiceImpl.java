package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.*;
import fpt.swp.springmvctt.itp.repository.*;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // ✅ thêm import này
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
    public void addFavorite(String email, Long productId) {
        // ✅ vì findByEmail trả về User, nên check null
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean exists = favoriteRepo.findByUserAndProduct(user, product).isPresent();
        if (!exists) {
            FavoriteProduct fav = FavoriteProduct.builder()
                    .user(user)
                    .product(product)
                    .createdAt(LocalDateTime.now()) // ✅ Thêm dòng này
                    .build();
            favoriteRepo.save(fav);
        }
    }

    @Transactional
    @Override
    public void removeFavorite(String email, Long productId) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        favoriteRepo.deleteByUserAndProduct(user, product);
    }

    @Override
    public List<FavoriteProductDTO> getFavorites(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return favoriteRepo.findByUser(user).stream()
                .map(fav -> FavoriteProductDTO.builder()
                        .id(fav.getId())
                        .productId(fav.getProduct().getId())
                        .productName(fav.getProduct().getProductName())
                        .productImage(fav.getProduct().getImage())
                        .createdAt(fav.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
