package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.dto.FavoriteProductDTO;
import java.util.List;

public interface FavoriteProductService {
    void addFavorite(String username, Long productId);
    void removeFavorite(String username, Long productId);
    List<FavoriteProductDTO> getFavorites(String username);
}
