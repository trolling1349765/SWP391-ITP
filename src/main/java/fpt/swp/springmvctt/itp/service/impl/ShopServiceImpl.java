package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Override
    public Page<Shop> findByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        return shopRepository.findByStatus(status, pageable);
    }

    @Override
    public Shop findById(Long id) {
        return shopRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Shop> filterInactiveShops(String shopName, String username, LocalDate fromDate,
                                          LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if( shopName == "" ) shopName = null;
        if( username == "" ) username = null;
        if( fromDate == null ) fromDate = null;
        if( toDate == null ) toDate = null;
        return shopRepository.filterShops("inactive", shopName, username, fromDate, toDate, pageable);
    }

}
