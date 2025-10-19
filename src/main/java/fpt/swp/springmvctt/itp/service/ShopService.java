package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.Shop;

import java.util.Optional;

public interface ShopService {
    Shop createForUser(Long userId, String shopName);
    Shop updateBasic(Long shopId, String shopName, String description);
    Optional<Shop> getByUser(Long userId);
    Shop get(Long id);
}
