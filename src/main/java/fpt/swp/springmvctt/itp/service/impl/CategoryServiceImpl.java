package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.Category;
import fpt.swp.springmvctt.itp.repository.CategoryRepository;
import fpt.swp.springmvctt.itp.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    // ======= Thêm mới cho CRUD + search/pagination =======
    @Override
    public Page<Category> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        String keyword = (q == null) ? "" : q.trim();
        if (keyword.isEmpty()) {
            return categoryRepository.findAll(pageable);
        }
        return categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return (id == null) ? Optional.empty() : categoryRepository.findById(id);
    }

    @Override
    public Category save(Category c) {
        return categoryRepository.save(c);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) categoryRepository.deleteById(id);
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
