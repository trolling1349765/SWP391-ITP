package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    public Shop createForUser(Long userId, String shopName) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (shopRepository.existsByUserId(userId) || u.getShopId() != null) {
            throw new IllegalStateException("User #" + userId + " đã có shop");
        }

        Shop s = new Shop();
        s.setUserId(userId);
        s.setShopName(shopName);
        s.setStatus("ACTIVE"); // hoặc HIDDEN tuỳ policy của bạn
        s = shopRepository.save(s);

        // link ngược: user.shop_id = shop.id
        u.setShopId(s.getId());
        userRepository.save(u);

        return s;
    }

    @Override
    public Shop updateBasic(Long shopId, String shopName, String description) {
        Shop s = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));
        if (shopName != null && !shopName.isBlank()) s.setShopName(shopName);
        if (description != null) s.setDescription(description);
        return shopRepository.save(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shop> getByUser(Long userId) {
        return shopRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Shop get(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + id));
    }
}
