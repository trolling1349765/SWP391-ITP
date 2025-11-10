# README - Chức năng Xem Chi Tiết Sản Phẩm và Thông Tin Shop (Phía Người Mua)

## 📋 Tổng quan
Tài liệu này mô tả chi tiết chức năng cho phép người mua (customer) xem thông tin chi tiết sản phẩm và thông tin shop trên hệ thống.

---

## 🎯 Các chức năng chính

### 1. Xem Chi Tiết Sản Phẩm
- Hiển thị đầy đủ thông tin sản phẩm
- Xem thông tin shop bán sản phẩm
- Thêm/xóa sản phẩm vào danh sách yêu thích
- Mua sản phẩm trực tiếp
- Xem mô tả chi tiết

### 2. Xem Thông Tin Shop
- Hiển thị thông tin cơ bản của shop
- Xem danh sách sản phẩm của shop
- Thống kê sản phẩm (tổng, còn hàng, sắp hết, hết hàng)
- Phân trang và sắp xếp sản phẩm
- Liên hệ shop

---

## 🏗️ Kiến trúc và Luồng xử lý

### A. XEM CHI TIẾT SẢN PHẨM

#### 📍 Route
```
GET /product/{id}
```

#### 🎮 Controller: `ProductController.java`

**Vị trí**: `src/main/java/fpt/swp/springmvctt/itp/controller/ProductController.java`

```java
@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FavoriteProductService favoriteService;

    /**
     * Xem chi tiết sản phẩm (Main branch - có shop homepage links)
     * Route này dùng để xem thông tin sản phẩm với layout đẹp
     * Khi user click "Mua ngay" sẽ chuyển sang /orders/checkout/{productId}
     */
    @GetMapping("/product/{id}")
    public String viewProductDetail(
        @PathVariable Long id, 
        Model model, 
        HttpSession session, 
        HttpServletRequest request
    ) {
        // 1. Lấy thông tin sản phẩm (eager load shop information)
        Product product = productService.getProductById(id);

        if (product == null) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }

        // 2. Thêm thông tin sản phẩm vào model
        model.addAttribute("product", product);
        model.addAttribute("sessionUser", session.getAttribute("user"));
        model.addAttribute("requestURI", request.getRequestURI());

        // 3. Lấy danh sách sản phẩm yêu thích của user (nếu đã đăng nhập)
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            List<FavoriteProductDTO> favorites = favoriteService.getFavorites(user.getEmail());
            Set<Long> favoriteProductIds = favorites.stream()
                    .map(FavoriteProductDTO::getProductId)
                    .collect(Collectors.toSet());
            model.addAttribute("favoriteProductIds", favoriteProductIds);
        }

        return "user/product-detail";
    }
}
```

#### 💼 Service: `ProductServiceImpl.java`

**Vị trí**: `src/main/java/fpt/swp/springmvctt/itp/service/impl/ProductServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product getProductById(Long id) {
        // Sử dụng findByIdWithShop để eager load shop information
        // Tránh N+1 query problem
        return productRepository.findByIdWithShop(id).orElse(null);
    }
}
```

#### 🗄️ Repository: `ProductRepository.java`

**Vị trí**: `src/main/java/fpt/swp/springmvctt/itp/repository/ProductRepository.java`

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Eager load shop và category khi lấy product detail
    // Sử dụng JOIN FETCH để tránh lazy loading exception
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.shop " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithShop(@Param("id") Long id);
}
```

**Giải thích Query:**
- `LEFT JOIN FETCH p.shop`: Eager load thông tin shop cùng lúc với product
- `LEFT JOIN FETCH p.category`: Eager load thông tin category
- Tránh N+1 query problem và lazy loading exception

#### 🎨 View: `product-detail.html`

**Vị trí**: `src/main/resources/templates/user/product-detail.html`

**Cấu trúc HTML:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="vi">
<head>
    <title th:text="${product.productName} + ' | Chi tiết sản phẩm'">Chi tiết sản phẩm</title>
    <!-- Bootstrap 5.3.3, Font Awesome 6.4.0 -->
</head>
<body class="bg-light">
    <!-- Header -->
    <div th:replace="~{/Included/Header :: header}"></div>

    <div class="container py-4">
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a th:href="@{/}" class="text-decoration-none">Trang chủ</a>
                </li>
                <li class="breadcrumb-item">
                    <a th:href="@{/products}" class="text-decoration-none">Sản phẩm</a>
                </li>
                <li class="breadcrumb-item active" th:text="${product.productName}">Product Name</li>
            </ol>
        </nav>

        <div class="row g-4">
            <!-- Left Column - Product Image -->
            <div class="col-lg-5">
                <div class="text-center">
                    <img th:if="${product.image != null and !#strings.isEmpty(product.image)}"
                         th:src="${'/itp' + product.image}"
                         th:alt="${product.productName}"
                         class="product-image">
                    <img th:if="${product.image == null or #strings.isEmpty(product.image)}"
                         src="https://via.placeholder.com/500x400?text=No+Image"
                         class="product-image">
                </div>
            </div>

            <!-- Right Column - Product Info -->
            <div class="col-lg-7">
                <div class="sticky-buy">
                    <!-- Product Name -->
                    <h1 class="product-name" th:text="${product.productName}">Product Name</h1>

                    <!-- Shop Info (Clickable) -->
                    <div class="shop-info" th:if="${product.shop != null}">
                        <div class="d-flex align-items-center">
                            <i class="fas fa-store fa-2x me-3"></i>
                            <div>
                                <div class="small opacity-75">Được bán bởi</div>
                                <a th:href="@{/customer/shop/{id}(id=${product.shop.id})}"
                                   class="h5 mb-0"
                                   th:text="${product.shop.shopName}">
                                    Shop Name
                                </a>
                            </div>
                        </div>
                    </div>

                    <!-- Price -->
                    <div class="product-price mb-3">
                        <span th:text="${#numbers.formatDecimal(product.price, 0, 'COMMA', 0, 'POINT')}">0</span>₫
                    </div>

                    <!-- Stock Status -->
                    <div class="mb-4">
                        <span th:if="${product.availableStock > 10}"
                              class="badge bg-success stock-badge">
                            <i class="fas fa-check-circle me-1"></i>
                            Còn hàng (<span th:text="${product.availableStock}">0</span> sản phẩm)
                        </span>
                        <span th:if="${product.availableStock > 0 and product.availableStock <= 10}"
                              class="badge bg-warning stock-badge">
                            <i class="fas fa-exclamation-triangle me-1"></i>
                            Sắp hết hàng (còn <span th:text="${product.availableStock}">0</span>)
                        </span>
                        <span th:if="${product.availableStock == 0}"
                              class="badge bg-danger stock-badge">
                            <i class="fas fa-times-circle me-1"></i>
                            Hết hàng
                        </span>
                    </div>

                    <!-- Product Info Card -->
                    <div class="info-card">
                        <h5 class="mb-3">
                            <i class="fas fa-list text-primary me-2"></i>Thông tin sản phẩm
                        </h5>

                        <div class="info-row">
                            <div class="info-label">Mã sản phẩm:</div>
                            <div class="info-value" th:text="'#' + ${product.id}">ID</div>
                        </div>

                        <div class="info-row" th:if="${product.productType != null}">
                            <div class="info-label">Loại sản phẩm:</div>
                            <div class="info-value" th:text="${product.productType.displayName}">Type</div>
                        </div>

                        <div class="info-row">
                            <div class="info-label">Trạng thái:</div>
                            <div class="info-value">
                                <span class="badge"
                                      th:classappend="${product.status.name() == 'ACTIVE'} ? 'bg-success' : 'bg-secondary'"
                                      th:text="${product.status.name() == 'ACTIVE' ? 'Đang bán' : 'Không khả dụng'}">
                                    Status
                                </span>
                            </div>
                        </div>

                        <div class="info-row">
                            <div class="info-label">Kho còn:</div>
                            <div class="info-value fw-bold" th:text="${product.availableStock} + ' sản phẩm'">0</div>
                        </div>
                    </div>

                    <!-- Buy Button -->
                    <div class="d-grid gap-2 mb-3">
                        <a th:href="@{/orders/checkout/{productId}(productId=${product.id})}"
                           th:if="${product.availableStock > 0 and product.status.name() == 'ACTIVE'}"
                           class="btn btn-danger btn-buy">
                            <i class="fas fa-shopping-cart me-2"></i>Mua ngay
                        </a>
                        <button class="btn btn-danger btn-buy" disabled
                                th:if="${product.availableStock == 0 or (product.availableStock > 0 and product.status.name() != 'ACTIVE')}">
                            <i class="fas fa-shopping-cart me-2"></i>
                            <span th:if="${product.availableStock == 0}">Hết hàng</span>
                            <span th:if="${product.availableStock > 0 and product.status.name() != 'ACTIVE'}">Không khả dụng</span>
                        </button>
                    </div>

                    <!-- Favorite Button -->
                    <div class="row g-2">
                        <div class="col-6" th:if="${sessionUser != null}">
                            <!-- Nếu đã yêu thích -->
                            <form th:if="${favoriteProductIds != null and favoriteProductIds.contains(product.id)}"
                                  th:action="@{'/favorites/remove/' + ${product.id}}" 
                                  method="post" 
                                  class="w-100">
                                <button type="submit" class="btn btn-outline-danger w-100">
                                    <i class="fa-solid fa-heart-crack me-2"></i>Xóa khỏi yêu thích
                                </button>
                            </form>

                            <!-- Nếu chưa yêu thích -->
                            <form th:if="${favoriteProductIds == null or !favoriteProductIds.contains(product.id)}"
                                  th:action="@{'/favorites/add/' + ${product.id}}"
                                  method="post" 
                                  class="w-100">
                                <button type="submit" class="btn btn-outline-secondary w-100">
                                    <i class="fa-regular fa-heart me-2"></i>Thêm vào yêu thích
                                </button>
                            </form>
                        </div>

                        <!-- Nếu chưa đăng nhập -->
                        <div class="col-6" th:if="${sessionUser == null}">
                            <button class="btn btn-outline-secondary w-100" 
                                    onclick="window.location.href='/itp/login'">
                                <i class="far fa-heart me-2"></i>Đăng nhập để yêu thích
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Detailed Description Section -->
        <div class="row mt-5" 
             th:if="${product.detailedDescription != null and !#strings.isEmpty(product.detailedDescription)}">
            <div class="col-12">
                <div class="description-section">
                    <h3 class="mb-4">
                        <i class="fas fa-file-alt text-primary me-2"></i>Mô tả chi tiết
                    </h3>
                    <div th:utext="${product.detailedDescription}">
                        Detailed description here...
                    </div>
                </div>
            </div>
        </div>

        <!-- Navigation Buttons -->
        <div class="row mt-4 mb-5">
            <div class="col-12 text-center">
                <a th:href="@{/products}" class="btn btn-outline-secondary btn-lg">
                    <i class="fas fa-arrow-left me-2"></i>Quay lại danh sách sản phẩm
                </a>
                <a th:href="@{/customer/shop/{id}(id=${product.shop.id})}"
                   th:if="${product.shop != null}"
                   class="btn btn-outline-primary btn-lg ms-2">
                    <i class="fas fa-store me-2"></i>Xem thêm sản phẩm của shop
                </a>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <div th:replace="~{/Included/Footer :: footer}"></div>
</body>
</html>
```

---

### B. XEM THÔNG TIN SHOP

#### 📍 Routes
```
GET /customer/shop/list          - Danh sách tất cả shop
GET /customer/shop/{shopId}      - Chi tiết shop và sản phẩm
```

#### 🎮 Controller: `CustomerShopController.java`

**Vị trí**: `src/main/java/fpt/swp/springmvctt/itp/controller/CustomerShopController.java`

```java
@Controller
@RequestMapping("/customer/shop")
@RequiredArgsConstructor
public class CustomerShopController {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    /**
     * Xem chi tiết shop và các sản phẩm của shop đó
     */
    @GetMapping("/{shopId}")
    public String viewShopDetail(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort,
            Model model) {

        // 1. Tìm shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại!"));

        // 2. Chỉ cho xem shop ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(shop.getStatus())) {
            throw new RuntimeException("Shop này hiện không hoạt động!");
        }

        // 3. Xác định cách sắp xếp sản phẩm
        Pageable pageable;
        if ("priceAsc".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        } else if ("priceDesc".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("price").descending());
        } else {
            // Mặc định: sản phẩm mới nhất
            pageable = PageRequest.of(page, size, Sort.by("id").descending());
        }

        // 4. Lấy danh sách sản phẩm ACTIVE của shop với phân trang
        Page<Product> productsPage = productRepository.findByShopIdAndStatus(
            shopId, 
            ProductStatus.ACTIVE, 
            pageable
        );

        // 5. Thống kê sản phẩm
        List<Product> allProducts = productRepository.findByShopIdOrderByIdDesc(shopId);
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .count();
        long inStock = allProducts.stream()
                .filter(p -> p.getAvailableStock() > 10)
                .count();
        long lowStock = allProducts.stream()
                .filter(p -> p.getAvailableStock() > 0 && p.getAvailableStock() <= 10)
                .count();
        long outOfStock = allProducts.stream()
                .filter(p -> p.getAvailableStock() == 0)
                .count();

        // 6. Truyền dữ liệu vào model
        model.addAttribute("shop", shop);
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalItems", productsPage.getTotalElements());
        model.addAttribute("sort", sort);

        // Thống kê
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("inStock", inStock);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("outOfStock", outOfStock);

        return "user/shop-detail";
    }
}
```

**Logic xử lý:**

1. **Validate Shop**: Kiểm tra shop tồn tại và đang ACTIVE
2. **Sắp xếp sản phẩm**: 
   - `priceAsc`: Giá tăng dần
   - `priceDesc`: Giá giảm dần
   - Mặc định: Mới nhất (id DESC)
3. **Phân trang**: Sử dụng Spring Data JPA Pagination
4. **Thống kê**: 
   - Tổng sản phẩm
   - Sản phẩm đang bán (ACTIVE)
   - Còn hàng (stock > 10)
   - Sắp hết (0 < stock <= 10)
   - Hết hàng (stock = 0)

#### 🎨 View: `shop-detail.html`

**Vị trí**: `src/main/resources/templates/user/shop-detail.html`

**Cấu trúc HTML:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="vi">
<head>
    <title th:text="${shop.shopName} + ' | Shop Detail'">Shop Detail</title>
    <!-- Bootstrap 5.3.3, Font Awesome 6.4.0 -->
</head>
<body>
    <div th:replace="~{/Included/Header :: header}"></div>

    <!-- Shop Header with Banner -->
    <div class="shop-header">
        <!-- Banner Image (if available) -->
        <img th:if="${shop.image != null and !#strings.isEmpty(shop.image)}"
             th:src="@{${shop.image}}"
             alt="Shop Banner"
             class="shop-header-banner">
        
        <div class="shop-header-overlay"></div>
        
        <div class="container shop-header-content">
            <div class="row align-items-center">
                <!-- Shop Avatar -->
                <div class="col-auto mb-3 mb-md-0">
                    <img th:if="${shop.imageUrl != null and !#strings.isEmpty(shop.imageUrl)}"
                         th:src="@{${shop.imageUrl}}"
                         alt="Shop Avatar"
                         class="shop-avatar-large">
                    <div th:if="${shop.imageUrl == null or #strings.isEmpty(shop.imageUrl)}"
                         class="shop-avatar-large d-flex align-items-center justify-content-center">
                        <i class="fas fa-store"></i>
                    </div>
                </div>
                
                <!-- Shop Info -->
                <div class="col">
                    <h1 class="mb-2 fw-bold" th:text="${shop.shopName}">Shop Name</h1>
                    <p class="mb-2 opacity-90">
                        <i class="fas fa-tag me-2"></i>
                        <span th:text="${shop.category != null ? shop.category : 'Chưa phân loại'}">Category</span>
                    </p>
                    <div class="d-flex align-items-center gap-3 mb-2">
                        <div th:if="${shop.rating != null}">
                            <i class="fas fa-star text-warning"></i>
                            <span class="fw-bold" th:text="${shop.rating}">4.5</span>
                            <span class="opacity-75">/5</span>
                        </div>
                        <div>
                            <i class="fas fa-box me-1"></i>
                            <span th:text="${activeProducts}">0</span> sản phẩm đang bán
                        </div>
                    </div>
                    <p class="mt-2 mb-0 opacity-90" 
                       th:if="${shop.shortDescription != null}"
                       th:text="${shop.shortDescription}">Shop description</p>
                </div>
            </div>
        </div>
    </div>

    <!-- Statistics Cards -->
    <div class="container my-4">
        <div class="row g-3">
            <!-- Tổng sản phẩm -->
            <div class="col-md-3">
                <div class="card stat-card primary shadow-sm">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted mb-1">Tổng sản phẩm</h6>
                                <h3 class="mb-0 fw-bold" th:text="${totalProducts}">0</h3>
                            </div>
                            <i class="fas fa-box fa-2x text-primary opacity-25"></i>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Còn hàng -->
            <div class="col-md-3">
                <div class="card stat-card success shadow-sm">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted mb-1">Còn hàng</h6>
                                <h3 class="mb-0 fw-bold text-success" th:text="${inStock}">0</h3>
                            </div>
                            <i class="fas fa-check-circle fa-2x text-success opacity-25"></i>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Sắp hết -->
            <div class="col-md-3">
                <div class="card stat-card warning shadow-sm">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted mb-1">Sắp hết</h6>
                                <h3 class="mb-0 fw-bold text-warning" th:text="${lowStock}">0</h3>
                            </div>
                            <i class="fas fa-exclamation-triangle fa-2x text-warning opacity-25"></i>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Hết hàng -->
            <div class="col-md-3">
                <div class="card stat-card danger shadow-sm">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted mb-1">Hết hàng</h6>
                                <h3 class="mb-0 fw-bold text-danger" th:text="${outOfStock}">0</h3>
                            </div>
                            <i class="fas fa-times-circle fa-2x text-danger opacity-25"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Contact Information -->
    <div class="container mb-4" th:if="${shop.email != null or shop.phone != null}">
        <div class="card shadow-sm">
            <div class="card-body">
                <h5 class="card-title mb-3">
                    <i class="fas fa-phone text-primary me-2"></i>Thông tin liên hệ
                </h5>
                <div class="row">
                    <div class="col-md-6" th:if="${shop.phone != null}">
                        <p class="mb-0">
                            <i class="fas fa-mobile-alt text-muted me-2"></i>
                            <strong>Điện thoại:</strong>
                            <span th:text="${shop.phone}">Phone</span>
                        </p>
                    </div>
                    <div class="col-md-6" th:if="${shop.email != null}">
                        <p class="mb-0">
                            <i class="fas fa-envelope text-muted me-2"></i>
                            <strong>Email:</strong>
                            <span th:text="${shop.email}">Email</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Products Section -->
    <div class="container mb-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="mb-0">
                <i class="fas fa-shopping-bag text-primary me-2"></i>Sản phẩm của Shop
            </h4>

            <!-- Sort Dropdown -->
            <div class="dropdown">
                <button class="btn btn-outline-secondary dropdown-toggle" 
                        type="button" 
                        data-bs-toggle="dropdown">
                    <i class="fas fa-sort me-2"></i>Sắp xếp
                </button>
                <ul class="dropdown-menu">
                    <li>
                        <a class="dropdown-item" 
                           th:href="@{/customer/shop/{id}(id=${shop.id}, page=0)}">
                            <i class="fas fa-clock me-2"></i>Mới nhất
                        </a>
                    </li>
                    <li>
                        <a class="dropdown-item" 
                           th:href="@{/customer/shop/{id}(id=${shop.id}, page=0, sort='priceAsc')}">
                            <i class="fas fa-sort-amount-down me-2"></i>Giá tăng dần
                        </a>
                    </li>
                    <li>
                        <a class="dropdown-item" 
                           th:href="@{/customer/shop/{id}(id=${shop.id}, page=0, sort='priceDesc')}">
                            <i class="fas fa-sort-amount-up me-2"></i>Giá giảm dần
                        </a>
                    </li>
                </ul>
            </div>
        </div>

        <!-- Products Grid -->
        <div class="row g-4" th:if="${products != null and !products.isEmpty()}">
            <div class="col-md-3 col-sm-6" th:each="product : ${products}">
                <div class="card product-card border-0 shadow-sm position-relative">
                    <!-- Stock Badge -->
                    <span class="badge bg-success badge-stock" 
                          th:if="${product.availableStock > 10}">
                        Còn hàng
                    </span>
                    <span class="badge bg-warning badge-stock" 
                          th:if="${product.availableStock > 0 and product.availableStock <= 10}">
                        Sắp hết
                    </span>
                    <span class="badge bg-danger badge-stock" 
                          th:if="${product.availableStock == 0}">
                        Hết hàng
                    </span>

                    <!-- Product Image -->
                    <img th:if="${product.image != null and !#strings.isEmpty(product.image)}"
                         th:src="${'/itp' + product.image}"
                         class="card-img-top product-img"
                         alt="Product Image">
                    <img th:if="${product.image == null or #strings.isEmpty(product.image)}"
                         src="https://via.placeholder.com/300x200"
                         class="card-img-top product-img"
                         alt="Product Image">

                    <div class="card-body">
                        <h6 class="card-title mb-2" th:text="${product.productName}">Product Name</h6>

                        <p class="fw-bold text-primary mb-2" style="font-size: 1.2rem;">
                            <span th:text="${#numbers.formatDecimal(product.price, 0, 'COMMA', 0, 'POINT')}">0</span>₫
                        </p>

                        <p class="text-muted small mb-3">
                            <i class="fas fa-box me-1"></i>
                            Còn <span th:text="${product.availableStock}">0</span> sản phẩm
                        </p>

                        <a th:href="@{/product/{id}(id=${product.id})}" 
                           class="btn btn-outline-primary w-100">
                            <i class="fas fa-eye me-2"></i>Xem chi tiết
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div th:if="${products == null or products.isEmpty()}" 
             class="text-center py-5">
            <i class="fas fa-box-open fa-4x text-muted mb-3"></i>
            <h5 class="text-muted">Shop này chưa có sản phẩm nào</h5>
            <p class="text-muted">Hãy quay lại sau để khám phá sản phẩm mới!</p>
        </div>

        <!-- Pagination -->
        <nav th:if="${totalPages > 1}" class="mt-5">
            <ul class="pagination justify-content-center">
                <li class="page-item" th:classappend="${currentPage == 0} ? 'disabled'">
                    <a class="page-link" 
                       th:href="@{/customer/shop/{id}(id=${shop.id}, page=${currentPage - 1}, sort=${sort})}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>

                <li th:each="i : ${#numbers.sequence(0, totalPages - 1)}"
                    class="page-item"
                    th:classappend="${i == currentPage} ? 'active'">
                    <a class="page-link" 
                       th:href="@{/customer/shop/{id}(id=${shop.id}, page=${i}, sort=${sort})}" 
                       th:text="${i + 1}">1</a>
                </li>

                <li class="page-item" 
                    th:classappend="${currentPage >= totalPages - 1} ? 'disabled'">
                    <a class="page-link" 
                       th:href="@{/customer/shop/{id}(id=${shop.id}, page=${currentPage + 1}, sort=${sort})}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </ul>
        </nav>

        <!-- Pagination Info -->
        <div class="text-center mt-3" th:if="${products != null and !products.isEmpty()}">
            <small class="text-muted">
                Hiển thị <span th:text="${products.size()}">0</span> trong tổng số
                <span th:text="${totalItems}">0</span> sản phẩm
            </small>
        </div>
    </div>

    <div th:replace="~{/Included/Footer :: footer}"></div>
</body>
</html>
```

---

## 📊 Entities và Relationships

### Product Entity

**Vị trí**: `src/main/java/fpt/swp/springmvctt/itp/entity/Product.java`

```java
@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product extends BaseEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="shop_id", nullable=false)
    private Long shopId;

    @Column(name="product_name", nullable=false, length=150)
    private String productName;

    @Column(columnDefinition="TEXT")
    private String description;                // Mô tả ngắn

    @Column(name="detailed_description", columnDefinition="TEXT")
    private String detailedDescription;        // Mô tả chi tiết

    @Column(precision=15, scale=2, nullable=false)
    private BigDecimal price;

    @Column(name="category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name="product_type", nullable=false, length=20)
    private ProductType productType = ProductType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ProductStatus status = ProductStatus.HIDDEN;

    @Column(name="available_stock", nullable=false)
    private Integer availableStock = 0;

    @Column(name="image", length=255)
    private String image;

    // === Relations ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", insertable = false, updatable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteProduct> favoriteProducts;
}
```

**Các trường quan trọng:**
- `description`: Mô tả ngắn hiển thị trong card
- `detailedDescription`: Mô tả chi tiết hiển thị trong trang detail
- `availableStock`: Số lượng tồn kho (hiển thị trạng thái)
- `status`: ACTIVE/HIDDEN (chỉ hiển thị ACTIVE cho customer)
- `image`: Đường dẫn ảnh sản phẩm

### Shop Entity

**Vị trí**: `src/main/java/fpt/swp/springmvctt/itp/entity/Shop.java`

```java
@Entity
@Table(name="shops")
@Getter @Setter @NoArgsConstructor
public class Shop extends BaseEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="shop_name", length=100, nullable=false)
    private String shopName;

    @Column(name="short_description", length=500)
    private String shortDescription;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(precision=2, scale=1)
    private BigDecimal rating;

    @Column(length=20)
    private String status;              // "ACTIVE" hoặc "INACTIVE"

    @Column(length=255)
    private String category;

    @Column(length=255) 
    private String email;

    @Column(length=20)  
    private String phone;

    @Column(name="image_url", length=500) 
    private String imageUrl;            // Shop avatar

    @Column(name="image", length=255)     
    private String image;               // Shop banner

    // === Relations ===
    @OneToMany(mappedBy = "shopId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
```

**Các trường quan trọng:**
- `shopName`: Tên shop
- `shortDescription`: Mô tả ngắn
- `rating`: Đánh giá shop (0-5 sao)
- `status`: Trạng thái shop (ACTIVE/INACTIVE)
- `email`, `phone`: Thông tin liên hệ
- `imageUrl`: Avatar/logo shop
- `image`: Banner shop

---

## 🔄 Luồng dữ liệu (Data Flow)

### 1. Xem Chi Tiết Sản Phẩm

```
User click vào sản phẩm
    ↓
GET /product/{id}
    ↓
ProductController.viewProductDetail()
    ↓
ProductService.getProductById(id)
    ↓
ProductRepository.findByIdWithShop(id)
    ↓ [Eager Load Shop & Category]
Query: SELECT p FROM Product p 
       LEFT JOIN FETCH p.shop 
       LEFT JOIN FETCH p.category 
       WHERE p.id = :id
    ↓
Trả về Product (kèm Shop, Category)
    ↓
FavoriteProductService.getFavorites(email) [nếu đã login]
    ↓
Controller truyền dữ liệu vào Model:
  - product
  - sessionUser
  - favoriteProductIds (nếu đã login)
    ↓
Render: user/product-detail.html
    ↓
Hiển thị trang chi tiết sản phẩm
```

### 2. Xem Thông Tin Shop

```
User click vào Shop
    ↓
GET /customer/shop/{shopId}?page=0&sort=newest
    ↓
CustomerShopController.viewShopDetail()
    ↓
ShopRepository.findById(shopId)
    ↓
Kiểm tra shop.status == "ACTIVE"
    ↓
Xác định sort order:
  - priceAsc  → Sort.by("price").ascending()
  - priceDesc → Sort.by("price").descending()
  - newest    → Sort.by("id").descending()
    ↓
ProductRepository.findByShopIdAndStatus(shopId, ACTIVE, pageable)
    ↓
Query: SELECT * FROM products 
       WHERE shop_id = :shopId 
       AND status = 'ACTIVE' 
       ORDER BY [sort_field] 
       LIMIT :size OFFSET :offset
    ↓
Tính thống kê sản phẩm:
  - totalProducts
  - activeProducts
  - inStock (availableStock > 10)
  - lowStock (0 < availableStock <= 10)
  - outOfStock (availableStock == 0)
    ↓
Controller truyền dữ liệu vào Model:
  - shop
  - products (Page<Product>)
  - currentPage, totalPages, totalItems
  - sort
  - Statistics (totalProducts, inStock, lowStock, outOfStock)
    ↓
Render: user/shop-detail.html
    ↓
Hiển thị trang chi tiết shop với danh sách sản phẩm
```

---

## 🎨 UI/UX Features

### Trang Chi Tiết Sản Phẩm

1. **Breadcrumb Navigation**: Trang chủ > Sản phẩm > Tên sản phẩm
2. **Product Image**: Ảnh lớn với fallback placeholder
3. **Shop Info Card**: Link clickable đến trang shop
4. **Price Display**: Format tiền tệ VNĐ (1.000.000₫)
5. **Stock Badge**: 
   - 🟢 Còn hàng (stock > 10)
   - 🟡 Sắp hết (0 < stock <= 10)
   - 🔴 Hết hàng (stock = 0)
6. **Product Info Card**: Mã SP, loại, trạng thái, kho
7. **Buy Button**: 
   - Active nếu còn hàng & status = ACTIVE
   - Disabled nếu hết hàng hoặc không khả dụng
8. **Favorite Button**:
   - ❤️ Đã yêu thích → Xóa khỏi yêu thích
   - 🤍 Chưa yêu thích → Thêm vào yêu thích
   - 🔒 Chưa đăng nhập → Yêu cầu đăng nhập
9. **Detailed Description**: Hỗ trợ HTML formatting
10. **Navigation Buttons**: Quay lại danh sách, xem shop

### Trang Chi Tiết Shop

1. **Shop Header**: 
   - Banner image (nếu có)
   - Shop avatar/logo
   - Tên shop, category, rating
   - Số sản phẩm đang bán
   - Mô tả shop
2. **Statistics Dashboard**: 4 cards thống kê
   - 📦 Tổng sản phẩm
   - ✅ Còn hàng
   - ⚠️ Sắp hết
   - ❌ Hết hàng
3. **Contact Info Card**: Email, phone
4. **Products Section**:
   - Tiêu đề với icon
   - Dropdown sắp xếp (mới nhất, giá tăng, giá giảm)
   - Grid layout 4 cột (responsive)
   - Product card với badge trạng thái
   - Pagination
5. **Empty State**: Hiển thị khi shop chưa có sản phẩm

---

## 🔐 Security & Validation

### 1. Shop Access Control

```java
// Chỉ cho xem shop ACTIVE
if (!"ACTIVE".equalsIgnoreCase(shop.getStatus())) {
    throw new RuntimeException("Shop này hiện không hoạt động!");
}
```

### 2. Product Visibility

```java
// Chỉ hiển thị sản phẩm ACTIVE cho customer
Page<Product> productsPage = productRepository.findByShopIdAndStatus(
    shopId, 
    ProductStatus.ACTIVE,  // Chỉ lấy ACTIVE
    pageable
);
```

### 3. User Authentication for Favorites

```html
<!-- Kiểm tra đăng nhập -->
<div th:if="${sessionUser != null}">
    <!-- Show favorite button -->
</div>
<div th:if="${sessionUser == null}">
    <!-- Show login button -->
</div>
```

### 4. Stock Validation

```html
<!-- Nút mua chỉ active khi còn hàng và status = ACTIVE -->
<a th:href="@{/orders/checkout/{productId}(productId=${product.id})}"
   th:if="${product.availableStock > 0 and product.status.name() == 'ACTIVE'}"
   class="btn btn-danger btn-buy">
    <i class="fas fa-shopping-cart me-2"></i>Mua ngay
</a>
```

---

## 🚀 Performance Optimization

### 1. Eager Loading để tránh N+1 Query

```java
@Query("SELECT p FROM Product p " +
       "LEFT JOIN FETCH p.shop " +
       "LEFT JOIN FETCH p.category " +
       "WHERE p.id = :id")
Optional<Product> findByIdWithShop(@Param("id") Long id);
```

**Lợi ích:**
- Chỉ 1 query thay vì 3 queries riêng lẻ
- Tránh LazyInitializationException
- Giảm response time

### 2. Pagination

```java
Page<Product> productsPage = productRepository.findByShopIdAndStatus(
    shopId, 
    ProductStatus.ACTIVE, 
    pageable  // Phân trang, không load hết
);
```

**Lợi ích:**
- Giảm memory usage
- Cải thiện loading time
- Better user experience

### 3. Lazy Loading cho danh sách

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "shop_id", insertable = false, updatable = false)
private Shop shop;
```

**Lợi ích:**
- Không load relations không cần thiết
- Tối ưu query

---

## 📱 Responsive Design

### Bootstrap Grid System

```html
<!-- Desktop: 4 cột, Tablet: 6 cột (2 sản phẩm/hàng) -->
<div class="col-md-3 col-sm-6" th:each="product : ${products}">
    <!-- Product card -->
</div>
```

### Sticky Buy Section

```css
.sticky-buy {
    position: sticky;
    top: 20px;
}
```

**Lợi ích:**
- Nút mua luôn hiển thị khi scroll
- Cải thiện conversion rate

---

## 🧪 Test Cases

### Test Xem Chi Tiết Sản Phẩm

1. ✅ Hiển thị đầy đủ thông tin sản phẩm
2. ✅ Hiển thị thông tin shop (eager loaded)
3. ✅ Badge trạng thái stock chính xác
4. ✅ Nút mua active/disabled đúng logic
5. ✅ Favorite button hiển thị đúng trạng thái
6. ✅ Redirect login nếu chưa đăng nhập khi favorite
7. ✅ Mô tả chi tiết render HTML đúng
8. ✅ Link đến shop hoạt động
9. ✅ Breadcrumb navigation đúng
10. ✅ Error handling khi product không tồn tại

### Test Xem Chi Tiết Shop

1. ✅ Hiển thị đầy đủ thông tin shop
2. ✅ Statistics cards tính đúng
3. ✅ Chỉ hiển thị sản phẩm ACTIVE
4. ✅ Sắp xếp theo giá hoạt động đúng
5. ✅ Pagination hoạt động
6. ✅ Empty state hiển thị khi không có sản phẩm
7. ✅ Link xem chi tiết sản phẩm hoạt động
8. ✅ Không cho xem shop INACTIVE
9. ✅ Error handling khi shop không tồn tại
10. ✅ Contact info hiển thị đúng

---

## 📝 Các điểm lưu ý (Notes)

### 1. Image Path Handling

```html
<!-- Product image path -->
<img th:src="${'/itp' + product.image}" />

<!-- Shop image path -->
<img th:src="@{${shop.imageUrl}}" />
```

**Lý do:**
- Product image có prefix path `/assets/img/...`
- Shop image là full path

### 2. Stock Badge Logic

```
stock > 10        → Còn hàng (badge-success)
0 < stock <= 10   → Sắp hết (badge-warning)
stock == 0        → Hết hàng (badge-danger)
```

### 3. Sort Options

```
newest     → ORDER BY id DESC (mặc định)
priceAsc   → ORDER BY price ASC
priceDesc  → ORDER BY price DESC
```

### 4. Favorite Feature

- Yêu cầu đăng nhập
- Sử dụng form POST để add/remove
- Icon khác nhau dựa trên trạng thái
- Set chứa productIds để check nhanh

---

## 🔗 Related Features

### 1. Checkout Flow
- Link: `/orders/checkout/{productId}`
- Xem: `OrderController.java`

### 2. Favorite Products
- Controller: `FavoriteProductController.java`
- Routes: 
  - POST `/favorites/add/{productId}`
  - POST `/favorites/remove/{productId}`

### 3. Product List
- Route: `/products`
- Controller: `ProductController.showAllProducts()`
- Có filter theo category và sort

### 4. Shop List
- Route: `/customer/shop/list`
- Controller: `CustomerShopController.listShops()`

---

## 🛠️ Technologies Used

- **Backend**: Spring Boot, Spring MVC, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5.3.3, Font Awesome 6.4.0
- **Database**: JPA/Hibernate
- **Session Management**: HttpSession
- **Template Engine**: Thymeleaf

---

## 📚 References

### Files liên quan:

**Controllers:**
- `ProductController.java`
- `CustomerShopController.java`
- `FavoriteProductController.java`

**Services:**
- `ProductService.java`
- `ProductServiceImpl.java`
- `FavoriteProductService.java`

**Repositories:**
- `ProductRepository.java`
- `ShopRepository.java`

**Entities:**
- `Product.java`
- `Shop.java`
- `User.java`
- `FavoriteProduct.java`

**Views:**
- `user/product-detail.html`
- `user/shop-detail.html`
- `user/ProductList.html`
- `user/shop-list.html`

---
