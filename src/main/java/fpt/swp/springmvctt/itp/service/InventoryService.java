package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    Product addOrUpdateStock(StockForm form); // serial mới -> HIDDEN
    Product setSerialQuantity(Long productId, String serialCode, Long absoluteQty);
    Product rebuildProductQuantity(Long productId);

    ProductStore changeSerialStatus(Long productStoreId, ProductStatus status);
    List<ProductStore> listSerials(Long productId);

    int availableStockForProduct(Long shopId, Long productId);
    Map<Long, Integer> availableStockByProductForShop(Long shopId);
    void deleteByProductId(Long productId);
}
