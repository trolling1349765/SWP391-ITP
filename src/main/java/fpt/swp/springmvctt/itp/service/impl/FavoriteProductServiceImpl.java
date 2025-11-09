package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import fpt.swp.springmvctt.itp.entity.*;
import fpt.swp.springmvctt.itp.repository.*;
import fpt.swp.springmvctt.itp.service.FavoriteProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteProductServiceImpl implements FavoriteProductService {

    private final FavoriteProductRepository favoriteRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    /** ✅ Thêm sản phẩm yêu thích */
    @Transactional
    @Override
    public void addFavorite(String email, Long productId) {
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
                    .createdAt(LocalDateTime.now()) // ✅ set thời gian tạo
                    .build();
            favoriteRepo.save(fav);
        }
    }

    /** ✅ Xóa sản phẩm khỏi yêu thích */
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

    /** ✅ Lấy danh sách yêu thích của user */
    @Override
    public List<FavoriteProductDTO> getFavorites(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return favoriteRepo.findByUser(user).stream()
                .map(fav -> {
                    Product p = fav.getProduct();
                    return FavoriteProductDTO.builder()
                            .id(fav.getId())
                            .productId(p.getId())
                            .productName(p.getProductName())
                            .productImage(p.getImage())
                            .price(p.getPrice())
                            .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                            .shopName(p.getShop() != null ? p.getShop().getShopName() : null)
                            .createdAt(fav.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
