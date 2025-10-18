package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.Shop;

import java.util.Optional;

public interface ShopService {
    Optional<Shop> findById(long id);
}
