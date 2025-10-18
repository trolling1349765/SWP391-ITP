package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;

    @Override
    public Optional<Shop> findById(long id) {
        return shopRepository.findById(id);
    }
}
