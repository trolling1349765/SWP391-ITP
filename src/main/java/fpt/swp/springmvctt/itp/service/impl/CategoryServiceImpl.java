package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        try {
            List<Category> categories = categoryRepository.findAll();
            
            // If no categories exist, create default ones
            if (categories.isEmpty()) {
                initializeDefaultCategories();
                categories = categoryRepository.findAll();
            }
            
            // Return all categories as-is (Vietnamese names from database)
            return categories;
        } catch (Exception e) {
            return List.of();
        }
    }
    
    private void initializeDefaultCategories() {
        try {
            // Create default categories (BaseEntity will handle timestamps automatically)
            // These are high-level, broad categories in ENGLISH
            Category telecomCategory = new Category();
            telecomCategory.setCategoryName("TELECOM");
            telecomCategory.setDescription("Telecommunications services and phone cards");
            categoryRepository.save(telecomCategory);
            
            Category digitalAccountCategory = new Category();
            digitalAccountCategory.setCategoryName("DIGITAL_ACCOUNTS");
            digitalAccountCategory.setDescription("Digital accounts, email, social media, streaming");
            categoryRepository.save(digitalAccountCategory);
            
            Category giftVoucherCategory = new Category();
            giftVoucherCategory.setCategoryName("GIFTS_VOUCHERS");
            giftVoucherCategory.setDescription("Gift cards, vouchers, coupons, promotional codes");
            categoryRepository.save(giftVoucherCategory);
            
            Category softwareCategory = new Category();
            softwareCategory.setCategoryName("SOFTWARE_LICENSES");
            softwareCategory.setDescription("Software keys, licenses, activation codes, subscriptions");
            categoryRepository.save(softwareCategory);
            
            Category gamingCategory = new Category();
            gamingCategory.setCategoryName("GAMING");
            gamingCategory.setDescription("Gaming accounts, items, currencies, gift codes");
            categoryRepository.save(gamingCategory);
            
            Category otherCategory = new Category();
            otherCategory.setCategoryName("OTHER");
            otherCategory.setDescription("Other products not in above categories");
            categoryRepository.save(otherCategory);
            
        } catch (Exception e) {
            // Log error but don't throw exception
            System.err.println("Error initializing default categories: " + e.getMessage());
        }
    }
}
