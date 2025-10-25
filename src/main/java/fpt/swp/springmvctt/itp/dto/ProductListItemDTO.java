package fpt.swp.springmvctt.itp.dto;

import java.math.BigDecimal;

public record ProductListItemDTO(
        Long id,
        String productName,
        BigDecimal price,
        String img,
        Long shopId,
        String shopName
) {}
