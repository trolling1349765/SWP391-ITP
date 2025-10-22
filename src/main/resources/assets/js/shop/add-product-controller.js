/**
 * Add Product Controller
 * Main controller that coordinates all add product functionality
 */
class AddProductController {
    constructor() {
        this.formManager = null;
        this.excelProcessor = null;
        this.submitManager = null;
        this.platformFeeConfig = null;
        this.categoryProductTypeMap = null;
        
        this.init();
    }

    init() {
        // Initialize configuration
        this.initializeConfiguration();
        
        // Initialize managers
        this.initializeManagers();
        
        // Setup event listeners
        this.setupEventListeners();
        
        // Initialize UI state
        this.initializeUI();
    }

    initializeConfiguration() {
        // Platform fee configuration by product type
        this.platformFeeConfig = {
            // Telecom cards - Lower fee (2-5%)
            'VIETTEL': { fee: 3, description: 'Thẻ điện thoại - Phí thấp' },
            'MOBIFONE': { fee: 3, description: 'Thẻ điện thoại - Phí thấp' },
            'VINAPHONE': { fee: 3, description: 'Thẻ điện thoại - Phí thấp' },
            'VIETTEL_DATA': { fee: 3, description: 'Gói data - Phí thấp' },
            'MOBIFONE_DATA': { fee: 3, description: 'Gói data - Phí thấp' },
            'VINAPHONE_DATA': { fee: 3, description: 'Gói data - Phí thấp' },
            
            // Digital accounts - Medium fee (5-10%)
            'EMAIL': { fee: 7, description: 'Tài khoản email - Phí trung bình' },
            'SOCIAL': { fee: 7, description: 'Social media - Phí trung bình' },
            'STREAMING': { fee: 7, description: 'Streaming - Phí trung bình' },
            'APP': { fee: 7, description: 'Ứng dụng - Phí trung bình' },
            
            // Gift cards - Medium fee (5-12%)
            'GIFT': { fee: 8, description: 'Thẻ quà tặng - Phí trung bình' },
            'VOUCHER': { fee: 8, description: 'Voucher - Phí trung bình' },
            'COUPON': { fee: 8, description: 'Coupon - Phí trung bình' },
            'PROMO': { fee: 8, description: 'Mã khuyến mãi - Phí trung bình' },
            
            // Software licenses - Higher fee (8-15%)
            'SOFTWARE': { fee: 12, description: 'Key phần mềm - Phí cao' },
            'LICENSE': { fee: 12, description: 'License key - Phí cao' },
            'ACTIVATION': { fee: 12, description: 'Mã kích hoạt - Phí cao' },
            'SUBSCRIPTION': { fee: 12, description: 'Subscription - Phí cao' },
            
            // Gaming - Variable fee (5-20%)
            'GAME_ACC': { fee: 10, description: 'Tài khoản game - Phí biến động' },
            'GAME_ITEM': { fee: 10, description: 'Item game - Phí biến động' },
            'GAME_CURRENCY': { fee: 10, description: 'Tiền tệ game - Phí biến động' },
            'GAME_CODE': { fee: 10, description: 'Gift code game - Phí biến động' },
            
            // Other - Default fee (5-10%)
            'OTHER': { fee: 7, description: 'Khác - Phí mặc định' }
        };

        // Category to ProductType mapping
        this.categoryProductTypeMap = {
            'TELECOM': [
                { value: 'VIETTEL', text: 'Thẻ Viettel' },
                { value: 'MOBIFONE', text: 'Thẻ Mobifone' },
                { value: 'VINAPHONE', text: 'Thẻ Vinaphone' },
                { value: 'VIETTEL_DATA', text: 'Gói data Viettel' },
                { value: 'MOBIFONE_DATA', text: 'Gói data Mobifone' },
                { value: 'VINAPHONE_DATA', text: 'Gói data Vinaphone' }
            ],
            'DIGITAL_ACCOUNTS': [
                { value: 'EMAIL', text: 'Tài khoản email' },
                { value: 'SOCIAL', text: 'Tài khoản social media' },
                { value: 'STREAMING', text: 'Tài khoản streaming' },
                { value: 'APP', text: 'Tài khoản ứng dụng' }
            ],
            'GIFTS_VOUCHERS': [
                { value: 'GIFT', text: 'Thẻ quà tặng' },
                { value: 'VOUCHER', text: 'Voucher' },
                { value: 'COUPON', text: 'Coupon' },
                { value: 'PROMO', text: 'Mã khuyến mãi' }
            ],
            'SOFTWARE_LICENSES': [
                { value: 'SOFTWARE', text: 'Key phần mềm' },
                { value: 'LICENSE', text: 'License key' },
                { value: 'ACTIVATION', text: 'Mã kích hoạt' },
                { value: 'SUBSCRIPTION', text: 'Subscription' }
            ],
            'GAMING': [
                { value: 'GAME_ACC', text: 'Tài khoản game' },
                { value: 'GAME_ITEM', text: 'Item game' },
                { value: 'GAME_CURRENCY', text: 'Tiền tệ game' },
                { value: 'GAME_CODE', text: 'Gift code game' }
            ],
            'OTHER': [
                { value: 'OTHER', text: 'Khác' }
            ]
        };
    }

    initializeManagers() {
        // Wait for other managers to be initialized
        setTimeout(() => {
            this.formManager = window.formManager;
            this.excelProcessor = window.excelProcessor;
            this.submitManager = window.submitManager;
        }, 100);
    }

    setupEventListeners() {
        // Category selection change
        const categorySelect = document.getElementById('categoryId');
        if (categorySelect) {
            categorySelect.addEventListener('change', () => this.updateProductTypeOptions());
        }

        // Product type selection change
        const productTypeSelect = document.getElementById('productTypeSelect');
        if (productTypeSelect) {
            productTypeSelect.addEventListener('change', () => this.updateProductTypeGuide());
        }

        // Face value selection change
        const faceValueSelect = document.getElementById('faceValueSelect');
        if (faceValueSelect) {
            faceValueSelect.addEventListener('change', () => this.updatePriceFromFaceValue());
        }

        // File input change
        const fileInput = document.getElementById('serialFileInput');
        if (fileInput) {
            fileInput.addEventListener('change', (e) => this.handleFileChange(e));
        }
    }

    initializeUI() {
        // Initialize platform fee to empty state
        const platformFeeInput = document.getElementById('platformFeeInput');
        const platformFeeInfo = document.getElementById('platformFeeInfo');
        if (platformFeeInput && platformFeeInfo) {
            platformFeeInput.value = '';
            platformFeeInfo.innerHTML = '';
        }

        // Initial call to update product type options
        setTimeout(() => {
            this.updateProductTypeOptions();
        }, 100);
    }

    updateProductTypeOptions() {
        const categorySelect = document.getElementById('categoryId');
        const productTypeSelect = document.getElementById('productTypeSelect');
        
        if (!categorySelect || !productTypeSelect) {
            return;
        }
        
        const selectedIndex = categorySelect.selectedIndex;
        const selectedCategoryText = categorySelect.options[selectedIndex].text;
        const selectedCategoryValue = categorySelect.options[selectedIndex].value;
        
        // Clear existing options except the first one
        productTypeSelect.innerHTML = '<option value="">-- Chọn kiểu sản phẩm --</option>';
        
        // If no category selected, return
        if (selectedIndex === 0 || !selectedCategoryValue) {
            return;
        }
        
        // Map Vietnamese display names to English category names
        const displayNameToCategoryMap = {
            'Viễn thông': 'TELECOM',
            'Tài khoản số': 'DIGITAL_ACCOUNTS',
            'Quà tặng & Voucher': 'GIFTS_VOUCHERS',
            'Phần mềm & License': 'SOFTWARE_LICENSES',
            'Gaming': 'GAMING',
            'Khác': 'OTHER'
        };
        
        // Get English category name from Vietnamese display name
        const englishCategoryName = displayNameToCategoryMap[selectedCategoryText];
        
        // Add options based on selected category
        if (englishCategoryName && this.categoryProductTypeMap[englishCategoryName]) {
            this.categoryProductTypeMap[englishCategoryName].forEach(option => {
                const optionElement = document.createElement('option');
                optionElement.value = option.value;
                optionElement.textContent = option.text;
                productTypeSelect.appendChild(optionElement);
            });
        }
        
        // Reset product type guide
        this.updateProductTypeGuide();
    }

    updateProductTypeGuide() {
        const productType = document.querySelector('select[name="productType"]').value;
        const faceValueSection = document.getElementById('faceValueSection');
        const platformFeeInput = document.getElementById('platformFeeInput');
        const platformFeeInfo = document.getElementById('platformFeeInfo');
        
        // Hide all guides first
        if (faceValueSection) {
            faceValueSection.style.display = 'none';
        }
        
        // Clear platform fee info
        if (platformFeeInfo) {
            platformFeeInfo.innerHTML = '';
        }
        
        // Clear platform fee input
        if (platformFeeInput) {
            platformFeeInput.value = '';
        }
        
        if (!productType) {
            return;
        }
        
        // Show face value section for telecom products
        if (['VIETTEL', 'MOBIFONE', 'VINAPHONE', 'VIETTEL_DATA', 'MOBIFONE_DATA', 'VINAPHONE_DATA'].includes(productType)) {
            if (faceValueSection) {
                faceValueSection.style.display = 'block';
            }
        }
        
        // Update platform fee
        if (this.platformFeeConfig[productType]) {
            const config = this.platformFeeConfig[productType];
            if (platformFeeInput) {
                platformFeeInput.value = config.fee;
            }
            if (platformFeeInfo) {
                platformFeeInfo.innerHTML = `<small class="text-info">${config.description}</small>`;
            }
        }
    }

    updatePriceFromFaceValue() {
        const faceValueSelect = document.getElementById('faceValueSelect');
        const priceInput = document.getElementById('priceInput');
        
        if (!faceValueSelect || !priceInput) {
            return;
        }
        
        const selectedValue = faceValueSelect.value;
        
        if (selectedValue === 'custom') {
            // Show custom price input
            priceInput.readOnly = false;
            priceInput.placeholder = 'Nhập giá tùy chỉnh';
            priceInput.value = '';
        } else if (selectedValue && selectedValue !== '') {
            // Set price from face value
            priceInput.value = selectedValue;
            priceInput.readOnly = true;
        } else {
            // Clear price
            priceInput.value = '';
            priceInput.readOnly = false;
            priceInput.placeholder = 'Nhập giá sản phẩm';
        }
    }

    handleFileChange(event) {
        if (this.excelProcessor) {
            this.excelProcessor.previewExcelFile(event.target);
        }
    }

    clearFileInput() {
        if (this.excelProcessor) {
            this.excelProcessor.clearFileInput();
        }
    }

    // Public methods for global access
    getFormManager() {
        return this.formManager;
    }

    getExcelProcessor() {
        return this.excelProcessor;
    }

    getSubmitManager() {
        return this.submitManager;
    }
}

// Initialize controller when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.addProductController = new AddProductController();
});

// Export for use in other modules
window.AddProductController = AddProductController;
