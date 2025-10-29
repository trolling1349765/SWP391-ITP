package fpt.swp.springmvctt.itp.config;

import fpt.swp.springmvctt.itp.entity.*;
import fpt.swp.springmvctt.itp.entity.enums.ProductStatus;
import fpt.swp.springmvctt.itp.entity.enums.ProductType;
import fpt.swp.springmvctt.itp.repository.*;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SampleDataInitializer {

    @Bean
    CommandLineRunner initSampleData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ShopRepository shopRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        return args -> {
            System.out.println("=== Bắt đầu khởi tạo dữ liệu mẫu ===");

            // Kiểm tra xem đã có dữ liệu mẫu chưa
            if (shopRepository.count() > 0) {
                System.out.println("Dữ liệu mẫu đã tồn tại, bỏ qua khởi tạo.");
                return;
            }

            // Lấy role SELLER
            Role sellerRole = roleRepository.findByName("SELLER")
                    .orElseThrow(() -> new RuntimeException("Role SELLER chưa được tạo!"));

            // Lấy role CUSTOMER
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Role CUSTOMER chưa được tạo!"));

            // Tạo categories nếu chưa có
            List<Category> categories = new ArrayList<>();
            if (categoryRepository.count() == 0) {
                String[] categoryNames = {"Thẻ điện thoại", "Tài khoản số", "Phần mềm", "Game", "Khác"};
                for (String name : categoryNames) {
                    Category cat = new Category();
                    cat.setCategoryName(name);
                    cat.setDescription("Danh mục " + name);
                    cat.setCreateAt(LocalDate.now());
                    cat.setCreateBy("system");
                    cat.setIsDeleted(false);
                    categories.add(categoryRepository.save(cat));
                }
                System.out.println("✅ Đã tạo " + categories.size() + " categories");
            } else {
                categories = categoryRepository.findAll();
            }

            // === TẠO SELLER 1 ===
            User seller1 = new User();
            seller1.setFullName("Nguyễn Văn Seller");
            seller1.setEmail("seller1@gmail.com");
            seller1.setUsername("seller1");
            seller1.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
            seller1.setPhone("0901234567");
            seller1.setRole(sellerRole);
            seller1.setStatus("ACTIVE");
            seller1.setProvider("local");
            seller1.setBalance(new BigDecimal("1000000"));
            seller1.setCreateAt(LocalDate.now());
            seller1.setCreateBy("system");
            seller1.setIsDeleted(false);
            seller1 = userRepository.save(seller1);
            System.out.println("�� Đã tạo SELLER 1: " + seller1.getEmail());

            // === TẠO SHOP 1 ===
            Shop shop1 = new Shop();
            shop1.setShopName("Shop MMO Pro");
            shop1.setDescription("Chuyên cung cấp các sản phẩm MMO chất lượng cao, uy tín hàng đầu Việt Nam");
            shop1.setCategory("MMO & Digital Products");
            shop1.setStatus("ACTIVE");
            shop1.setRating(new BigDecimal("4.8"));
            shop1.setEmail("shoppro@gmail.com");
            shop1.setPhone("0901234567");
            shop1.setShopCode("SHOP001");
            shop1.setImageUrl("https://via.placeholder.com/150/667eea/ffffff?text=MMO+Pro");
            shop1.setUser(seller1);
            shop1.setCreateAt(LocalDate.now());
            shop1.setCreateBy("system");
            shop1.setIsDeleted(false);
            shop1 = shopRepository.save(shop1);
            System.out.println("✅ Đã tạo SHOP 1: " + shop1.getShopName());

            // === TẠO SẢN PHẨM CHO SHOP 1 ===
            String[] productNames1 = {
                "Thẻ Viettel 100k",
                "Thẻ Vinaphone 50k",
                "Thẻ Mobifone 200k",
                "Tài khoản Netflix Premium 1 tháng",
                "Tài khoản Spotify Premium 3 tháng",
                "License Microsoft Office 2021",
                "License Windows 11 Pro",
                "Steam Gift Card $10",
                "Google Play Gift Card 100k",
                "iTunes Gift Card $20"
            };

            int[] stocks1 = {100, 150, 80, 50, 40, 30, 25, 60, 70, 35};
            BigDecimal[] prices1 = {
                new BigDecimal("95000"),
                new BigDecimal("48000"),
                new BigDecimal("190000"),
                new BigDecimal("120000"),
                new BigDecimal("350000"),
                new BigDecimal("450000"),
                new BigDecimal("380000"),
                new BigDecimal("250000"),
                new BigDecimal("95000"),
                new BigDecimal("480000")
            };

            for (int i = 0; i < productNames1.length; i++) {
                Product product = new Product();
                product.setShopId(shop1.getId());
                product.setProductName(productNames1[i]);
                product.setDescription("Sản phẩm chất lượng cao, giao hàng nhanh chóng");
                product.setDetailedDescription("Mô tả chi tiết về " + productNames1[i] + ". Sản phẩm được bảo hành, hỗ trợ 24/7.");
                product.setPrice(prices1[i]);
                product.setAvailableStock(stocks1[i]);
                product.setStatus(ProductStatus.ACTIVE);
                // Gán ProductType phù hợp
                if (i < 3) {
                    product.setProductType(ProductType.VIETTEL); // Thẻ điện thoại
                } else if (i < 5) {
                    product.setProductType(ProductType.STREAMING); // Tài khoản streaming
                } else {
                    product.setProductType(ProductType.LICENSE); // License phần mềm
                }
                product.setCategoryId(categories.get(i % categories.size()).getId());
                product.setImage("https://via.placeholder.com/400x300/667eea/ffffff?text=" + (i + 1));
                product.setCreateAt(LocalDate.now());
                product.setCreateBy("seller1");
                product.setIsDeleted(false);
                productRepository.save(product);
            }
            System.out.println("✅ Đã tạo " + productNames1.length + " sản phẩm cho SHOP 1");

            // === TẠO SELLER 2 ===
            User seller2 = new User();
            seller2.setFullName("Trần Thị Shop Owner");
            seller2.setEmail("seller2@gmail.com");
            seller2.setUsername("seller2");
            seller2.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
            seller2.setPhone("0912345678");
            seller2.setRole(sellerRole);
            seller2.setStatus("ACTIVE");
            seller2.setProvider("local");
            seller2.setBalance(new BigDecimal("500000"));
            seller2.setCreateAt(LocalDate.now());
            seller2.setCreateBy("system");
            seller2.setIsDeleted(false);
            seller2 = userRepository.save(seller2);
            System.out.println("✅ Đã tạo SELLER 2: " + seller2.getEmail());

            // === TẠO SHOP 2 ===
            Shop shop2 = new Shop();
            shop2.setShopName("Digital Store VN");
            shop2.setDescription("Cửa hàng số uy tín, chuyên cung cấp tài khoản và phần mềm bản quyền");
            shop2.setCategory("Digital Services");
            shop2.setStatus("ACTIVE");
            shop2.setRating(new BigDecimal("4.5"));
            shop2.setEmail("digitalstore@gmail.com");
            shop2.setPhone("0912345678");
            shop2.setShopCode("SHOP002");
            shop2.setImageUrl("https://via.placeholder.com/150/764ba2/ffffff?text=Digital+VN");
            shop2.setUser(seller2);
            shop2.setCreateAt(LocalDate.now());
            shop2.setCreateBy("system");
            shop2.setIsDeleted(false);
            shop2 = shopRepository.save(shop2);
            System.out.println("✅ Đã tạo SHOP 2: " + shop2.getShopName());

            // === TẠO SẢN PHẨM CHO SHOP 2 ===
            String[] productNames2 = {
                "Tài khoản YouTube Premium 1 năm",
                "Canva Pro 1 năm",
                "Adobe Creative Cloud 1 tháng",
                "Grammarly Premium 1 năm",
                "Thẻ Garena 100k",
                "PlayStation Plus 3 tháng",
                "Xbox Game Pass Ultimate 1 tháng"
            };

            int[] stocks2 = {20, 15, 10, 8, 200, 30, 25};
            BigDecimal[] prices2 = {
                new BigDecimal("280000"),
                new BigDecimal("520000"),
                new BigDecimal("350000"),
                new BigDecimal("480000"),
                new BigDecimal("95000"),
                new BigDecimal("550000"),
                new BigDecimal("320000")
            };

            for (int i = 0; i < productNames2.length; i++) {
                Product product = new Product();
                product.setShopId(shop2.getId());
                product.setProductName(productNames2[i]);
                product.setDescription("Sản phẩm chính hãng, bảo hành đầy đủ");
                product.setDetailedDescription("Chi tiết về " + productNames2[i] + ". Cam kết hàng chính hãng 100%.");
                product.setPrice(prices2[i]);
                product.setAvailableStock(stocks2[i]);
                product.setStatus(ProductStatus.ACTIVE);
                product.setProductType(i == 4 ? ProductType.GIFT : ProductType.SUBSCRIPTION);
                product.setCategoryId(categories.get(i % categories.size()).getId());
                product.setImage("https://via.placeholder.com/400x300/764ba2/ffffff?text=P" + (i + 1));
                product.setCreateAt(LocalDate.now());
                product.setCreateBy("seller2");
                product.setIsDeleted(false);
                productRepository.save(product);
            }
            System.out.println("✅ Đã tạo " + productNames2.length + " sản phẩm cho SHOP 2");

            // === TẠO SELLER 3 ===
            User seller3 = new User();
            seller3.setFullName("Lê Minh Seller");
            seller3.setEmail("seller3@gmail.com");
            seller3.setUsername("seller3");
            seller3.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
            seller3.setPhone("0923456789");
            seller3.setRole(sellerRole);
            seller3.setStatus("ACTIVE");
            seller3.setProvider("local");
            seller3.setBalance(new BigDecimal("750000"));
            seller3.setCreateAt(LocalDate.now());
            seller3.setCreateBy("system");
            seller3.setIsDeleted(false);
            seller3 = userRepository.save(seller3);
            System.out.println("✅ Đã tạo SELLER 3: " + seller3.getEmail());

            // === TẠO SHOP 3 ===
            Shop shop3 = new Shop();
            shop3.setShopName("Game Hub Store");
            shop3.setDescription("Thiên đường game thủ - Mọi thứ bạn cần cho gaming");
            shop3.setCategory("Gaming");
            shop3.setStatus("ACTIVE");
            shop3.setRating(new BigDecimal("4.9"));
            shop3.setEmail("gamehub@gmail.com");
            shop3.setPhone("0923456789");
            shop3.setShopCode("SHOP003");
            shop3.setImageUrl("https://via.placeholder.com/150/f093fb/ffffff?text=Game+Hub");
            shop3.setUser(seller3);
            shop3.setCreateAt(LocalDate.now());
            shop3.setCreateBy("system");
            shop3.setIsDeleted(false);
            shop3 = shopRepository.save(shop3);
            System.out.println("✅ Đã tạo SHOP 3: " + shop3.getShopName());

            // === TẠO SẢN PHẨM CHO SHOP 3 ===
            String[] productNames3 = {
                "Steam Wallet Code $50",
                "Riot Points 500 RP",
                "PUBG Mobile UC 600",
                "Free Fire Diamond 500",
                "Mobile Legends 1000 Diamond",
                "Genshin Impact Welkin Moon"
            };

            int[] stocks3 = {40, 80, 120, 150, 90, 60};
            BigDecimal[] prices3 = {
                new BigDecimal("1200000"),
                new BigDecimal("120000"),
                new BigDecimal("150000"),
                new BigDecimal("140000"),
                new BigDecimal("280000"),
                new BigDecimal("85000")
            };

            for (int i = 0; i < productNames3.length; i++) {
                Product product = new Product();
                product.setShopId(shop3.getId());
                product.setProductName(productNames3[i]);
                product.setDescription("Nạp game nhanh chóng, an toàn");
                product.setDetailedDescription("Dịch vụ nạp " + productNames3[i] + " uy tín, giao dịch tự động 24/7.");
                product.setPrice(prices3[i]);
                product.setAvailableStock(stocks3[i]);
                product.setStatus(ProductStatus.ACTIVE);
                product.setProductType(ProductType.GAME_CURRENCY);
                product.setCategoryId(categories.get(3).getId()); // Game category
                product.setImage("https://via.placeholder.com/400x300/f093fb/ffffff?text=Game" + (i + 1));
                product.setCreateAt(LocalDate.now());
                product.setCreateBy("seller3");
                product.setIsDeleted(false);
                productRepository.save(product);
            }
            System.out.println("✅ Đã tạo " + productNames3.length + " sản phẩm cho SHOP 3");

            // === TẠO VÀI CUSTOMER MẪU ===
            for (int i = 1; i <= 3; i++) {
                User customer = new User();
                customer.setFullName("Khách hàng " + i);
                customer.setEmail("customer" + i + "@gmail.com");
                customer.setUsername("customer" + i);
                customer.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
                customer.setPhone("093456789" + i);
                customer.setRole(customerRole);
                customer.setStatus("ACTIVE");
                customer.setProvider("local");
                customer.setBalance(new BigDecimal("500000"));
                customer.setCreateAt(LocalDate.now());
                customer.setCreateBy("system");
                customer.setIsDeleted(false);
                userRepository.save(customer);
            }
            System.out.println("✅ Đã tạo 3 CUSTOMER mẫu");

            System.out.println("=== Hoàn thành khởi tạo dữ liệu mẫu ===");
            System.out.println("📊 Tổng kết:");
            System.out.println("   - " + userRepository.count() + " users");
            System.out.println("   - " + shopRepository.count() + " shops");
            System.out.println("   - " + productRepository.count() + " products");
            System.out.println("   - " + categoryRepository.count() + " categories");
            System.out.println("\n🔑 Tài khoản test:");
            System.out.println("   SELLER 1: seller1@gmail.com / 123456");
            System.out.println("   SELLER 2: seller2@gmail.com / 123456");
            System.out.println("   SELLER 3: seller3@gmail.com / 123456");
            System.out.println("   CUSTOMER: customer1@gmail.com / 123456");
        };
    }
}

