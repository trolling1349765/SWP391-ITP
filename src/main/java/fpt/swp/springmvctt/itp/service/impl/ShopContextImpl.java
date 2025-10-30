package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.service.ShopContext;
import org.springframework.stereotype.Component;

@Component
public class ShopContextImpl implements ShopContext {

    @Override
    public long currentShopId() {

        return 1L;
    }
}
