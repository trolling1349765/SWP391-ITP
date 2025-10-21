package fpt.swp.springmvctt.itp.controller;

import fpt.swp.springmvctt.itp.dto.request.ProductForm;
import fpt.swp.springmvctt.itp.dto.request.StockForm;
import fpt.swp.springmvctt.itp.dto.request.ExcelImportForm;
import fpt.swp.springmvctt.itp.dto.response.ImportResult;
import fpt.swp.springmvctt.itp.entity.Product;
import fpt.swp.springmvctt.itp.entity.ProductStore;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.service.CategoryService;
import fpt.swp.springmvctt.itp.service.InventoryService;
import fpt.swp.springmvctt.itp.service.ProductService;
import fpt.swp.springmvctt.itp.service.ExcelImportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final CategoryService categoryService;
    private final ExcelImportService excelImportService;
    private static final Long SHOP_ID = 1L; // shop demo

    private void putCurrentPath(Model model, HttpServletRequest request) {
        model.addAttribute("currentPath", request != null ? request.getRequestURI() : "");
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> allProducts = productService.listByShop(SHOP_ID);
        
        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        allProducts.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));
        
        // Tính toán phân trang
        int totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        page = Math.max(1, Math.min(page, totalPages)); // Đảm bảo page hợp lệ
        
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalProducts);
        List<Product> products = allProducts.subList(startIndex, endIndex);
        
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        for (Product p : products) stockMap.put(p.getId(), p.getAvailableStock());
        
        model.addAttribute("products", products);
        model.addAttribute("stockMap", stockMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalProducts", totalProducts);
        
        return "shop/dashboard";
    }

    // Add Product
    @GetMapping("/addProduct")
    public String addProductForm(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        model.addAttribute("form", new ProductForm());
        model.addAttribute("categories", categoryService.findAll());
        return "shop/addProduct";
    }

    @PostMapping("/addProduct")
    public String addProductSubmit(@ModelAttribute("form") ProductForm form, 
                                    @RequestParam(required = false) String serial,
                                    @RequestParam(required = false) String secretCode,
                                    @RequestParam(required = false) Integer stockQuantity,
                                    RedirectAttributes ra) {
        Product created = productService.createProduct(SHOP_ID, form); // HIDDEN
        
        //  serial
        if (serial != null && !serial.isBlank() && stockQuantity != null && stockQuantity > 0) {
            StockForm stockForm = new StockForm();
            stockForm.setProductId(created.getId());
            stockForm.setSerial(serial);
            stockForm.setCode(secretCode);
            stockForm.setQuantity(stockQuantity);
            inventoryService.addOrUpdateStock(stockForm);
            ra.addFlashAttribute("ok", "Đã tạo sản phẩm #" + created.getId() + " và thêm " + stockQuantity + " vào kho (HIDDEN)");
        } else {
            ra.addFlashAttribute("ok", "Đã tạo sản phẩm #" + created.getId() + " (HIDDEN)");
        }
        return "redirect:/shop/dashboard";
    }

    // Update Product
    @GetMapping("/updateProduct/{id}")
    public String updateProductForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
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
        productService.updateProduct(id, form);
        
        // Cập nhật status
        if (status != null) {
            productService.changeStatus(id, status);
            ra.addFlashAttribute("ok", "Đã cập nhật sản phẩm #" + id + " → Status: " + status);
        } else {
            ra.addFlashAttribute("ok", "Đã cập nhật sản phẩm #" + id);
        }
        return "redirect:/shop/dashboard";
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
    public String inventory(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> products = productService.listByShop(SHOP_ID);
        
        // Sắp xếp theo ID tăng dần (từ bé lên lớn)
        products.sort((p1, p2) -> Long.compare(p1.getId(), p2.getId()));
        
        // Tạo stockMap và tính toán thống kê
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        int totalActiveProducts = 0;
        int lowStockProducts = 0;
        int outOfStockProducts = 0;
        
        for (Product p : products) {
            int stock = p.getAvailableStock();
            stockMap.put(p.getId(), stock);
            
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
        model.addAttribute("totalActiveProducts", totalActiveProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);
        model.addAttribute("form", new StockForm());
        return "shop/inventory";
    }


    // Add Serial form
    @GetMapping("/addSerial")
    public String addSerialForm(Model model, HttpServletRequest request) {
        putCurrentPath(model, request);
        List<Product> products = productService.listByShop(SHOP_ID);
        
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
    public String productDetail(@PathVariable Long id, Model model) {
        Product p = productService.get(id);
        
        // Get serial data from JSON file instead of database
        List<Map<String, Object>> serials = excelImportService.getProductSerials(id);
        
        model.addAttribute("productDetail", p);
        model.addAttribute("product", p);
        model.addAttribute("serials", serials);
        model.addAttribute("serialCount", serials.size());
        
        System.out.println("ProductDetail - Product ID: " + id + ", Serial count: " + serials.size());
        
        return "shop/ProductDetail";
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
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<>();
        
        System.out.println("Delete request for product ID: " + id);
        
        try {
            // Kiểm tra sản phẩm
            Product product = productService.get(id);
            System.out.println("Found product: " + product.getProductName() + ", Shop ID: " + product.getShopId());
            
            // Kiểm tra sản phẩm có thuộc về shop
            if (!product.getShopId().equals(SHOP_ID)) {
                System.out.println("Access denied: Product belongs to shop " + product.getShopId() + ", but current shop is " + SHOP_ID);
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
            return ResponseEntity.notFound().build();
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
    public ResponseEntity<Map<String, Object>> previewExcel(@ModelAttribute ExcelImportForm form) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            System.out.println("Preview request for product ID: " + form.getProductId());
            
            if (!excelImportService.validateExcelFormat(form.getExcelFile())) {
                response.put("success", false);
                response.put("message", "Định dạng file Excel không đúng. Vui lòng sử dụng template chuẩn.");
                return ResponseEntity.badRequest().body(response);
            }
            
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

}
