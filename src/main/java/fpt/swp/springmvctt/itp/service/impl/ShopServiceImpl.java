package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public Shop createForUser(Long userId, String shopName) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (shopRepository.existsByUserId(userId) || u.getShop() != null) {
            throw new IllegalStateException("User #" + userId + " đã có shop");
        }

        Shop s = new Shop();
        s.setUser(u);  // Set relationship instead of userId
        s.setShopName(shopName);
        s.setStatus("ACTIVE"); // hoặc HIDDEN tuỳ policy của bạn
        s = shopRepository.save(s);

        // link ngược: user.shop = shop
        u.setShop(s);
        userRepository.save(u);

        return s;
    }

    @Override
    public Page<Shop> findByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        return shopRepository.findByStatus(status, pageable);
    }

    @Override
    public Shop updateBasic(Long shopId, String shopName, String description) {
        Shop s = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));
        if (shopName != null && !shopName.isBlank()) s.setShopName(shopName);
        if (description != null) s.setDescription(description);
        return shopRepository.save(s);
    }

    @Override
    public Shop findById(Long id) {
        return shopRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shop> getByUser(Long userId) {
        return shopRepository.findByUserId(userId);
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

    @Override
    public Page<Shop> findByFilter(
            String shopName,
            String createBy,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate fromUpdateDate,
            LocalDate toUpdateDate,
            Boolean deleted,
            String deleteBy,
            String status,
            int page,
            int size
    ) {
        if (shopName == null || shopName.isEmpty()) shopName = null;
        if (createBy == null || createBy.isEmpty()) createBy = null;
        if (deleteBy == null || deleteBy.isEmpty() ) deleteBy = null;
        if (status == null || status.isEmpty()) status = null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        return shopRepository.findByFilter(
                shopName,
                createBy,
                fromDate,
                toDate,
                fromUpdateDate,
                toUpdateDate,
                deleted,
                deleteBy,
                status,
                pageable
        );
    }

    @Override
    public void delete(Long id) {
        HttpSession session = httpServletRequest.getSession();
        User user = (User) session.getAttribute("user");
        shopRepository.findById(id).ifPresent((shop) ->{
            shop.setDeleteBy(user.getUsername());
            shop.setUpdateAt(LocalDate.now());
            shop.setIsDeleted(true);
            shopRepository.save(shop);
        });
    }

    @Override
    public void activateShop(Long id) {
        shopRepository.findById(id).ifPresent((shop) ->{
            shop.setStatus("ACTIVE");
            shopRepository.save(shop);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Shop get(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + id));
    }
}
