package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.repository.OrderItemRepository;
import fpt.swp.springmvctt.itp.repository.ProductRepository;
import fpt.swp.springmvctt.itp.repository.ProductStoreRepository;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.service.CategoryService;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.ExcelImportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import fpt.swp.springmvctt.itp.entity.Shop;
import fpt.swp.springmvctt.itp.repository.ShopRepository;
import fpt.swp.springmvctt.itp.service.ShopService;
import fpt.swp.springmvctt.itp.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.time.LocalDate;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class    ShopController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CategoryService categoryService;
    private final ExcelImportService excelImportService;
    private final ProductStoreRepository productStoreRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final StorageService storageService;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;


    private Long getShopIdFromSession(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            throw new IllegalStateException("User chưa đăng nhập!");
        }
        
        // Tìm shop theo user_id (tránh lazy loading issue)
        Shop shop = shopRepository.findByUserId(sessionUser.getId())
            .orElseThrow(() -> new IllegalStateException(
                "Tài khoản của bạn chưa có shop. Vui lòng đăng ký shop trước!"
            ));
        
        return shop.getId();
    }

    private void putCurrentPath(Model model, HttpServletRequest request) {
        model.addAttribute("currentPath", request != null ? request.getRequestURI() : "");
    }
    
    private void addShopToModel(Model model, HttpSession session) {
        try {
            Long shopId = getShopIdFromSession(session);
            Shop shop = shopRepository.findById(shopId).orElse(null);
            model.addAttribute("shop", shop);
            
            // Thêm user balance vào model để hiển thị tiền
            User sessionUser = (User) session.getAttribute("user");
            if (sessionUser != null) {
                // Reload user từ DB để lấy balance mới nhất
                User user = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
                model.addAttribute("userBalance", user.getBalance());
            }
        } catch (Exception e) {
            // If error, just don't add shop to model (will show default "Admin")
            System.err.println("Could not load shop for header: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model, HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes ra) {
        try {
            putCurrentPath(model, request);
            addShopToModel(model, session);
            
            // Get shopId from session
            Long shopId;
            try {
                shopId = getShopIdFromSession(session);
            } catch (IllegalStateException e) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:/";
            }

            // Input validation
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;

            // Get products with error handling
            List<Product> allProducts;
            try {
                allProducts = productService.listByShop(shopId);
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
                return "redirect:/shop/dashboard";
            }

            // Handle empty state
            if (allProducts == null || allProducts.isEmpty()) {
                model.addAttribute("products", new ArrayList<>());
                model.addAttribute("stockMap", new LinkedHashMap<>());
                model.addAttribute("batchMap", new LinkedHashMap<>());
                model.addAttribute("currentPage", 1);
                model.addAttribute("totalPages", 0);
                model.addAttribute("pageSize", size);
                model.addAttribute("totalProducts", 0);
                model.addAttribute("info", "Chưa có sản phẩm nào. Hãy thêm sản phẩm đầu tiên!");
                return "shop/dashboard";
            }

            // Sort products
            allProducts.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));

            // Calculate pagination
            int totalProducts = allProducts.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / size));
            page = Math.max(1, Math.min(page, totalPages));

            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalProducts);

            // Safe sublist
            List<Product> products;
            if (startIndex >= totalProducts) {
                products = new ArrayList<>();
            } else {
                products = allProducts.subList(startIndex, endIndex);
            }

            // Build stock maps with error handling
            Map<Long, Integer> stockMap = new LinkedHashMap<>();
            Map<Long, Map<java.math.BigDecimal, Long>> batchMap = new LinkedHashMap<>();

            for (Product p : products) {
                try {
                    stockMap.put(p.getId(), p.getAvailableStock());
                    Map<java.math.BigDecimal, Long> batches = inventoryService.getStockByBatches(p.getId());
                    batchMap.put(p.getId(), batches != null ? batches : new LinkedHashMap<>());
                } catch (Exception e) {
                    System.err.println("Error processing product " + p.getId() + ": " + e.getMessage());
                    stockMap.put(p.getId(), 0);
                    batchMap.put(p.getId(), new LinkedHashMap<>());
                }
            }

            model.addAttribute("products", products);
            model.addAttribute("stockMap", stockMap);
            model.addAttribute("batchMap", batchMap);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalProducts", totalProducts);

            return "shop/dashboard";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            return "redirect:/shop/dashboard";
        }
    }

    // Add Product
    @GetMapping("/addProduct")
    public String addProductForm(Model model, HttpServletRequest request, HttpSession session) {
        putCurrentPath(model, request);
        addShopToModel(model, session);
        model.addAttribute("form", new ProductForm());
        model.addAttribute("categories", categoryService.findAll());
        return "shop/addProduct";
    }

    @PostMapping("/addProduct")
    public String addProductSubmit(@Valid @ModelAttribute("form") ProductForm form,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   RedirectAttributes ra) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đã nhập");
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            ra.addFlashAttribute("form", form);
            return "redirect:/shop/addProduct";
        }

        try {
            Long shopId = getShopIdFromSession(session);
            Product created = productService.createProduct(shopId, form); // HIDDEN

            // Product created successfully - serials will be added later via updateProduct
            String successMsg = String.format("Đã tạo sản phẩm '%s' (#%d) thành công! Vui lòng vào 'Sửa' để thêm mã sản phẩm từ Excel.",
                    created.getProductName(),
                    created.getId());
            ra.addFlashAttribute("ok", successMsg);

            return "redirect:/shop/dashboard";

        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi tạo sản phẩm: " + e.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/shop/addProduct";
        }
    }

    // Update Product
    @GetMapping("/updateProduct/{id}")
    public String updateProductForm(@PathVariable Long id, Model model, HttpServletRequest request, HttpSession session) {
        putCurrentPath(model, request);
        addShopToModel(model, session);
        Product product = productService.get(id);
        ProductForm form = new ProductForm();
        form.setProductName(product.getProductName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setCategoryId(product.getCategoryId());
        form.setImg(product.getImage());
        model.addAttribute("productId", id);
        model.addAttribute("product", product);
        model.addAttribute("availableStock", product.getAvailableStock());
        model.addAttribute("form", form);
        model.addAttribute("categories", categoryService.findAll());
        return "shop/updateProduct";
    }

    @PostMapping("/updateProduct/{id}")
    public String updateProductSubmit(@PathVariable Long id,
                                      @ModelAttribute("form") ProductForm form,
                                      @RequestParam(required = false) ProductStatus status,
                                      RedirectAttributes ra) {
        try {
            Product updated = productService.updateProduct(id, form);

            // Import serials from Excel file if provided
            if (form.getSerialFile() != null && !form.getSerialFile().isEmpty()) {
                try {
                    System.out.println(" Starting Excel import for product " + id + "...");
                    ExcelImportForm importForm = new ExcelImportForm();
                    importForm.setProductId(id);
                    importForm.setExcelFile(form.getSerialFile());
                    importForm.setOverrideExisting(false);

                    ImportResult result = excelImportService.importSerialsFromExcel(importForm);
                    System.out.println(" Import completed!");
                    System.out.println("   - Imported: " + result.getImportedCount());
                    System.out.println("   - Skipped: " + result.getSkippedCount());
                    System.out.println("   - Errors: " + result.getErrors().size());
                    System.out.println("   - Warnings: " + result.getWarnings().size());

                    // Rebuild product quantity after import
                    updated = inventoryService.rebuildProductQuantity(id);
                    System.out.println(" Final availableStock: " + updated.getAvailableStock());

                } catch (Exception e) {
                    System.err.println("Error importing serials: " + e.getMessage());
                    e.printStackTrace();
                    ra.addFlashAttribute("error", "Lỗi khi import Excel: " + e.getMessage());
                    return "redirect:/shop/updateProduct/" + id;
                }
            }

            // Cập nhật status
            if (status != null) {
                updated = productService.changeStatus(id, status);
                String successMsg = String.format("Đã cập nhật sản phẩm '%s' (#%d) → Trạng thái: %s",
                        updated.getProductName(),
                        updated.getId(),
                        status);
                if (form.getSerialFile() != null && !form.getSerialFile().isEmpty()) {
                    successMsg += " | Đã import " + updated.getAvailableStock() + " serials từ Excel";
                }
                ra.addFlashAttribute("ok", successMsg);
            } else {
                String successMsg = String.format("Đã cập nhật sản phẩm '%s' (#%d) thành công!",
                        updated.getProductName(),
                        updated.getId());
                if (form.getSerialFile() != null && !form.getSerialFile().isEmpty()) {
                    successMsg += " | Đã import " + updated.getAvailableStock() + " serials từ Excel";
                }
                ra.addFlashAttribute("ok", successMsg);
            }
            return "redirect:/shop/dashboard";
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/shop/updateProduct/" + id;
        }
    }

    // ACTIVE/HIDDEN/BLOCKED
    @PostMapping("/products/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam ProductStatus status,
                               RedirectAttributes ra) {
        productService.changeStatus(id, status);
        ra.addFlashAttribute("ok", "Đã đổi trạng thái sản phẩm #" + id + " → " + status);
        return "redirect:/shop/dashboard";
    }

    // INVENTORY (serial)
    @GetMapping("/inventory")
    public String inventory(Model model, HttpServletRequest request, HttpSession session) {
        putCurrentPath(model, request);
        addShopToModel(model, session);
        Long shopId = getShopIdFromSession(session);
        List<Product> products = productService.listByShop(shopId);

        // Load categories for filter
        List<Category> allCategories = categoryRepository.findAll();
        model.addAttribute("allCategories", allCategories);

        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        products.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));

        // Tạo stockMap, batchMap và tính toán thống kê
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        Map<Long, Map<java.math.BigDecimal, Long>> batchMap = new LinkedHashMap<>();
        int totalActiveProducts = 0;
        int lowStockProducts = 0;
        int outOfStockProducts = 0;

        for (Product p : products) {
            int stock = p.getAvailableStock();
            stockMap.put(p.getId(), stock);
            // Get stock by batches (grouped by price)
            batchMap.put(p.getId(), inventoryService.getStockByBatches(p.getId()));

            // Calculate statistics
            if (p.getStatus().name().equals("ACTIVE")) {
                totalActiveProducts++;
            }
            if (stock == 0) {
                outOfStockProducts++;
            } else if (stock <= 10) {
                lowStockProducts++;
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("stockMap", stockMap);
        model.addAttribute("batchMap", batchMap);
        model.addAttribute("totalActiveProducts", totalActiveProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);
        model.addAttribute("form", new StockForm());
        return "shop/inventory";
    }


    // Add Serial form
    @GetMapping("/addSerial")
    public String addSerialForm(Model model, HttpServletRequest request, HttpSession session) {
        putCurrentPath(model, request);
        Long shopId = getShopIdFromSession(session);
        List<Product> products = productService.listByShop(shopId);

        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        products.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));

        model.addAttribute("products", products);
        model.addAttribute("form", new StockForm());
        return "shop/addSerial";
    }

    @PostMapping("/addSerial")
    public String addSerialSubmit(@ModelAttribute("form") StockForm form, RedirectAttributes ra) {
        inventoryService.addOrUpdateStock(form); // tạo/cộng serial + rebuild stock
        ra.addFlashAttribute("ok", "Đã cập nhật kho cho SP #" + form.getProductId());
        return "redirect:/shop/inventory";
    }


    // Chi tiết sản phẩm
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        try {
            putCurrentPath(model, request);

            Product p = productService.get(id);

            // Get serial data from database instead of JSON files
            List<ProductStore> productStores = productStoreRepository.findByProductIdOrderByIdDesc(id);

            // Convert ProductStore entities to Map format for template compatibility
            List<Map<String, Object>> serials = new ArrayList<>();
            int activeCount = 0, hiddenCount = 0, blockedCount = 0;

            for (ProductStore ps : productStores) {
                // ============================================================
                // FIX: Kiểm tra xem serial đã bán chưa
                // ============================================================
                // Nếu ProductStore có OrderItem với order status = COMPLETED hoặc PENDING
                // thì coi như đã bán
                boolean isSold = orderItemRepository.isProductStoreSold(ps.getId());
                
                Map<String, Object> serialMap = new LinkedHashMap<>();
                serialMap.put("serialCode", ps.getSerialCode());
                serialMap.put("secretCode", ps.getSecretCode());
                serialMap.put("quantity", 1); // Each serial = 1 item
                serialMap.put("faceValue", ps.getFaceValue());
                serialMap.put("information", ps.getInfomation());
                serialMap.put("status", ps.getStatus().name());
                serialMap.put("importDate", ps.getCreateAt() != null ? ps.getCreateAt().toString() : "N/A");
                serialMap.put("isSold", isSold); // Kiểm tra từ database
                serials.add(serialMap);

                // Count by status
                switch (ps.getStatus()) {
                    case ACTIVE: activeCount++; break;
                    case HIDDEN: hiddenCount++; break;
                    case BLOCKED: blockedCount++; break;
                }
            }

            model.addAttribute("productDetail", p);
            model.addAttribute("product", p);
            model.addAttribute("serials", serials);
            model.addAttribute("serialCount", serials.size());
            model.addAttribute("activeCount", activeCount);
            model.addAttribute("hiddenCount", hiddenCount);
            model.addAttribute("blockedCount", blockedCount);

            // Get category name for display
            String categoryDisplayName = "Chưa phân loại";
            if (p.getCategoryId() != null) {
                try {
                    Category category = categoryRepository.findById(p.getCategoryId()).orElse(null);
                    if (category != null) {
                        categoryDisplayName = getCategoryDisplayName(category.getCategoryName());
                    }
                } catch (Exception e) {
                    System.err.println("Error getting category: " + e.getMessage());
                }
            }
            model.addAttribute("categoryDisplayName", categoryDisplayName);

            System.out.println("ProductDetail - Product ID: " + id + ", Serial count from DB: " + serials.size());

            return "shop/ProductDetail";

        } catch (Exception e) {
            System.err.println("Error loading product detail: " + e.getMessage());
            return "redirect:/shop/dashboard";
        }
    }

    /**
     * Helper method to check if product type is telecom card
     */
    public boolean isTelecomCard(ProductType productType) {
        return productType != null &&
                (productType == ProductType.VIETTEL ||
                        productType == ProductType.MOBIFONE ||
                        productType == ProductType.VINAPHONE ||
                        productType == ProductType.VIETTEL_DATA ||
                        productType == ProductType.MOBIFONE_DATA ||
                        productType == ProductType.VINAPHONE_DATA);
    }

    /**
     * Helper method to get product type display name from ProductType
     */
    public String getSupplierName(ProductType productType) {
        if (productType == null) return "Không xác định";

        switch (productType) {
            case VIETTEL:
                return "Thẻ Viettel";
            case MOBIFONE:
                return "Thẻ Mobifone";
            case VINAPHONE:
                return "Thẻ Vinaphone";
            case VIETTEL_DATA:
                return "Gói data Viettel";
            case MOBIFONE_DATA:
                return "Gói data Mobifone";
            case VINAPHONE_DATA:
                return "Gói data Vinaphone";
            case EMAIL:
                return "Tài khoản email";
            case SOCIAL:
                return "Tài khoản social media";
            case STREAMING:
                return "Tài khoản streaming";
            case APP:
                return "Tài khoản ứng dụng";
            case GIFT:
                return "Thẻ quà tặng";
            case VOUCHER:
                return "Voucher";
            case COUPON:
                return "Coupon";
            case PROMO:
                return "Mã khuyến mãi";
            case SOFTWARE:
                return "Key phần mềm";
            case LICENSE:
                return "License key";
            case ACTIVATION:
                return "Mã kích hoạt";
            case SUBSCRIPTION:
                return "Subscription";
            case GAME_ACC:
                return "Tài khoản game";
            case GAME_ITEM:
                return "Item game";
            case GAME_CURRENCY:
                return "Tiền tệ game";
            case GAME_CODE:
                return "Gift code game";
            case OTHER:
            default:
                return "Khác";
        }
    }

    /**
     * Helper method to convert Vietnamese category name to display name for frontend
     * This returns the same Vietnamese name that will be used in JavaScript mapping
     */
    public String getCategoryDisplayName(String categoryName) {
        // Categories in database are in Vietnamese - return as-is for Thymeleaf display
        // JavaScript will handle the mapping from Vietnamese to English keys
        return categoryName;
    }

    @PostMapping("/check-serial-duplicates")
    @ResponseBody
    public Map<String, Object> checkSerialDuplicates(@RequestBody List<String> serialCodes) {
        try {
            List<String> duplicates = new ArrayList<>();

            for (String serialCode : serialCodes) {
                if (productStoreRepository.existsBySerialCode(serialCode)) {
                    duplicates.add(serialCode);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("duplicates", duplicates);
            result.put("totalChecked", serialCodes.size());
            result.put("duplicateCount", duplicates.size());

            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    // Test endpoint to check categories
    @GetMapping("/test-categories")
    @ResponseBody
    public String testCategories() {
        List<Category> categories = categoryService.findAll();
        StringBuilder result = new StringBuilder();
        result.append("Current Categories:\n");
        for (Category cat : categories) {
            result.append("ID: ").append(cat.getId())
                    .append(", Name: ").append(cat.getCategoryName())
                    .append(", Description: ").append(cat.getDescription())
                    .append("\n");
        }
        return result.toString();
    }

    @PostMapping("/stocks/{psId}/status")
    public String changeSerialStatus(@PathVariable("psId") Long productStoreId,
                                     @RequestParam("status") ProductStatus status,
                                     @RequestParam("productId") Long productId,
                                     RedirectAttributes ra) {
        inventoryService.changeSerialStatus(productStoreId, status);
        ra.addFlashAttribute("ok", "Đã đổi trạng thái serial → " + status);
        return "redirect:/shop/products/" + productId + "/serials";
    }

    @DeleteMapping("/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();

        System.out.println("Delete request for product ID: " + id);

        try {
            Long shopId = getShopIdFromSession(session);

            // Kiểm tra sản phẩm
            Product product = productService.get(id);
            System.out.println("Found product: " + product.getProductName() + ", Shop ID: " + product.getShopId());

            // Kiểm tra sản phẩm có thuộc về shop
            if (!product.getShopId().equals(shopId)) {
                System.out.println("Access denied: Product belongs to shop " + product.getShopId() + ", but current shop is " + shopId);
                response.put("success", false);
                response.put("message", "Không có quyền xóa sản phẩm này");
                return ResponseEntity.status(403).body(response);
            }

            // Xóa sản phẩm
            System.out.println("Deleting product: " + product.getProductName());
            productService.delete(id);

            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm thành công");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.out.println("Product not found: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Không tìm thấy sản phẩm");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/importTemplate/{productId}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadImportTemplate(@PathVariable Long productId) {
        try {
            byte[] template = excelImportService.generateExcelTemplate(productId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "import_template_" + productId + ".xlsx");
            return ResponseEntity.ok().headers(headers).body(template);
        } catch (Exception e) {
            System.out.println("Error generating template: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/addProductTemplate")
    @ResponseBody
    public ResponseEntity<byte[]> downloadAddProductTemplate() {
        try {
            // Generate template for new product (productId = 0 means new product)
            byte[] template = excelImportService.generateExcelTemplate(0L);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "add_product_template.xlsx");
            return ResponseEntity.ok().headers(headers).body(template);
        } catch (Exception e) {
            System.out.println("Error generating add product template: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/previewExcel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> previewExcel(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("productType") String productType) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            System.out.println("Preview request for file: " + file.getOriginalFilename() + ", productType: " + productType);

            if (!excelImportService.validateExcelFormat(file)) {
                response.put("success", false);
                response.put("message", "Định dạng file Excel không đúng. Vui lòng sử dụng template chuẩn.");
                return ResponseEntity.badRequest().body(response);
            }

            // Create ExcelImportForm for preview
            ExcelImportForm form = new ExcelImportForm();
            form.setProductId(0L); // Temporary ID for preview
            form.setExcelFile(file);
            form.setOverrideExisting(false);

            Map<String, Object> result = excelImportService.previewExcelImport(form);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error previewing Excel: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi preview: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Upload ảnh riêng - nhanh và có preview
    @PostMapping("/uploadImage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn file ảnh");
                return ResponseEntity.badRequest().body(response);
            }

            // Save image using StorageService
            String imagePath = productService.saveImage(file);

            response.put("success", true);
            response.put("message", "Upload ảnh thành công!");
            response.put("imagePath", imagePath);

            System.out.println(" Image uploaded successfully: " + imagePath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" Error uploading image: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/importSerials")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importSerials(@ModelAttribute ExcelImportForm form) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            System.out.println("Import request for product ID: " + form.getProductId());

            if (!excelImportService.validateExcelFormat(form.getExcelFile())) {
                response.put("success", false);
                response.put("message", "Định dạng file Excel không đúng. Vui lòng sử dụng template chuẩn.");
                return ResponseEntity.badRequest().body(response);
            }

            ImportResult result = excelImportService.importSerialsFromExcel(form);

            response.put("success", true);
            response.put("message", "Import hoàn thành!");
            response.put("totalRows", result.getTotalRows());
            response.put("importedCount", result.getImportedCount());
            response.put("skippedCount", result.getSkippedCount());
            response.put("errors", result.getErrors());
            response.put("warnings", result.getWarnings());
            response.put("duplicateSerials", result.getDuplicateSerials());
            response.put("invalidSerials", result.getInvalidSerials());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error importing serials: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi import: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    //
    @GetMapping
    public String viewShop(Model model) {
        Shop shop = new Shop();
        shop.setShopName("EverGift Store");
        shop.setShopCode("EV001");
        shop.setCategory("Gifts & Flowers");
        shop.setStatus("active");
        shop.setEmail("evergift@gmail.com");
        shop.setPhone("0123456789");
        shop.setDescription("This is a demo shop for testing UI.");

        model.addAttribute("shop", shop);
        model.addAttribute("totalProducts", 120);
        model.addAttribute("inStock", 100);
        model.addAttribute("lowStock", 10);
        model.addAttribute("outOfStock", 10);

        return "shop-detail";
    }



    @PostMapping("/upload-image")
    public String uploadShopImage(@RequestParam("file") MultipartFile file,
                                  @RequestParam("shopId") Long shopId) {
        String imageUrl = storageService.uploadImage(file);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        shop.setImage(imageUrl);
        shopRepository.save(shop);
        return "redirect:/shop/details/" + shopId;
    }

    // ===== SHOP PROFILE MANAGEMENT =====
    
    @GetMapping("/profile")
    public String showShopProfile(Model model, HttpSession session, HttpServletRequest request) {
        try {
            Long shopId = getShopIdFromSession(session);
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new IllegalStateException("Shop không tồn tại!"));
            
            // Load all categories
            List<Category> allCategories = categoryRepository.findAll();
            
            // Load statistics (giống dashboard)
            List<Product> allProducts = productRepository.findByShopIdOrderByIdDesc(shopId);
            long totalProducts = allProducts.size();
            long activeProducts = allProducts.stream()
                    .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                    .count();
            
            // Calculate total stock
            long totalStock = 0;
            for (Product product : allProducts) {
                List<ProductStore> stores = productStoreRepository.findByProductIdOrderByIdDesc(product.getId());
                totalStock += stores.size();
            }
            
            model.addAttribute("shop", shop);
            model.addAttribute("allCategories", allCategories);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("activeProducts", activeProducts);
            model.addAttribute("totalStock", totalStock);
            model.addAttribute("currentPath", "/shop/profile");
            
            addShopToModel(model, session);
            putCurrentPath(model, request);
            
            return "shop/shop-profile";
        } catch (Exception e) {
            System.err.println("Error loading shop profile: " + e.getMessage());
            return "redirect:/shop/dashboard";
        }
    }
    
    @PostMapping("/profile/update")
    public String updateShopProfile(
            @RequestParam("shopName") String shopName,
            @RequestParam("category") String category,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "shopImage", required = false) MultipartFile shopImage,
            @RequestParam(value = "logoImage", required = false) MultipartFile logoImage,
            HttpSession session,
            RedirectAttributes ra) {
        try {
            // ===== BACKEND VALIDATION =====
            
            // Validate shop name
            if (shopName == null || shopName.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Tên shop không được để trống");
                return "redirect:/shop/profile";
            }
            if (shopName.length() > 100) {
                ra.addFlashAttribute("error", "Tên shop không được vượt quá 100 ký tự");
                return "redirect:/shop/profile";
            }
            
            // Validate category
            if (category == null || category.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn ít nhất 1 danh mục");
                return "redirect:/shop/profile";
            }
            
            // Validate email
            if (email == null || email.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Email không được để trống");
                return "redirect:/shop/profile";
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                ra.addFlashAttribute("error", "Email không hợp lệ");
                return "redirect:/shop/profile";
            }
            
            // Validate phone
            if (phone == null || phone.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Số điện thoại không được để trống");
                return "redirect:/shop/profile";
            }
            if (!phone.matches("^0[0-9]{9}$")) {
                ra.addFlashAttribute("error", "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0");
                return "redirect:/shop/profile";
            }
            
            // Validate short description length
            if (shortDescription != null && shortDescription.length() > 500) {
                ra.addFlashAttribute("error", "Mô tả ngắn không được vượt quá 500 ký tự");
                return "redirect:/shop/profile";
            }
            
            // Validate image size
            if (shopImage != null && !shopImage.isEmpty()) {
                if (shopImage.getSize() > 5 * 1024 * 1024) { // 5MB
                    ra.addFlashAttribute("error", "Ảnh bìa không được vượt quá 5MB");
                    return "redirect:/shop/profile";
                }
            }
            
            if (logoImage != null && !logoImage.isEmpty()) {
                if (logoImage.getSize() > 5 * 1024 * 1024) { // 5MB
                    ra.addFlashAttribute("error", "Logo không được vượt quá 5MB");
                    return "redirect:/shop/profile";
                }
            }
            
            // ===== UPDATE SHOP =====
            
            Long shopId = getShopIdFromSession(session);
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new IllegalStateException("Shop không tồn tại!"));
            
            // Update basic info
            shop.setShopName(shopName.trim());
            shop.setCategory(category.trim());
            shop.setEmail(email.trim().toLowerCase());
            shop.setPhone(phone.trim());
            shop.setShortDescription(shortDescription != null ? shortDescription.trim() : null);
            shop.setDescription(description != null ? description.trim() : null);
            
            // Upload shop banner image if provided (lưu local + copy to target)
            if (shopImage != null && !shopImage.isEmpty()) {
                String imageUrl = storageService.saveShopBanner(shopImage);
                shop.setImage(imageUrl);
                System.out.println(" Shop banner updated: " + imageUrl);
            }
            
            // Upload shop logo if provided (lưu local + copy to target)
            if (logoImage != null && !logoImage.isEmpty()) {
                String logoUrl = storageService.saveShopLogo(logoImage);
                shop.setImageUrl(logoUrl);
                System.out.println(" Shop logo updated: " + logoUrl);
            }
            
            shopRepository.save(shop);
            
            ra.addFlashAttribute("success", "Cập nhật thông tin shop thành công!");
            return "redirect:/shop/profile";
            
        } catch (Exception e) {
            System.err.println("Error updating shop profile: " + e.getMessage());
            ra.addFlashAttribute("error", "Lỗi khi cập nhật shop: " + e.getMessage());
            return "redirect:/shop/profile";
        }
    }
}
