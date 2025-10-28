package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import java.util.Optional;

public interface ShopService {
    Shop createForUser(Long userId, String shopName);
    Shop updateBasic(Long shopId, String shopName, String description);
    Optional<Shop> getByUser(Long userId);
    Shop get(Long id);
    public Page<Shop> findByStatus(String status, int  page, int size);
    public Shop findById(Long id);
    Page<Shop> filterInactiveShops(String shopName, String username, LocalDate fromDate, LocalDate toDate, int page, int size);
//    public Page<Shop> findByFilter(String status, int page, int size, Pageable pageable);
}
