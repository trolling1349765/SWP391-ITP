package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.request.StockForm;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

    void addOrUpdateStock(StockForm form);
}
