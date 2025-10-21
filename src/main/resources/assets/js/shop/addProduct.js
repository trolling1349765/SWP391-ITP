// AddProduct JavaScript Functions

// Category to ProductType mapping (Categories in English, ProductTypes in Vietnamese)
const categoryProductTypeMap = {
    // TELECOM (Viễn thông)
    'TELECOM': [
        { value: 'VIETTEL', text: 'Thẻ Viettel' },
        { value: 'MOBIFONE', text: 'Thẻ Mobifone' },
        { value: 'VINAPHONE', text: 'Thẻ Vinaphone' },
        { value: 'VIETTEL_DATA', text: 'Gói data Viettel' },
        { value: 'MOBIFONE_DATA', text: 'Gói data Mobifone' },
        { value: 'VINAPHONE_DATA', text: 'Gói data Vinaphone' }
    ],
    
    // DIGITAL_ACCOUNT (Tài khoản số)
    'DIGITAL_ACCOUNT': [
        { value: 'EMAIL', text: 'Tài khoản Email' },
        { value: 'SOCIAL', text: 'Tài khoản Mạng xã hội' },
        { value: 'STREAMING', text: 'Tài khoản Streaming' },
        { value: 'GAMING', text: 'Tài khoản Game' },
        { value: 'CLOUD', text: 'Tài khoản Cloud' }
    ],
    
    // GIFT_CARD (Thẻ quà tặng)
    'GIFT_CARD': [
        { value: 'GOOGLE_PLAY', text: 'Thẻ Google Play' },
        { value: 'APPLE_STORE', text: 'Thẻ Apple Store' },
        { value: 'STEAM', text: 'Thẻ Steam' },
        { value: 'NETFLIX', text: 'Thẻ Netflix' },
        { value: 'SPOTIFY', text: 'Thẻ Spotify' }
    ],
    
    // SOFTWARE (Phần mềm)
    'SOFTWARE': [
        { value: 'ANTIVIRUS', text: 'Phần mềm Antivirus' },
        { value: 'OFFICE', text: 'Phần mềm Office' },
        { value: 'DESIGN', text: 'Phần mềm Thiết kế' },
        { value: 'DEVELOPMENT', text: 'Phần mềm Lập trình' }
    ],
    
    // OTHER (Khác)
    'OTHER': [
        { value: 'OTHER', text: 'Khác' }
    ]
};

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeCategoryMapping();
    initializeProductTypeHandler();
    initializePlatformFeeHandler();
    initializeFileUploadHandler();
});

// Initialize category mapping
function initializeCategoryMapping() {
    const categoryRadios = document.querySelectorAll('input[name="category"]');
    const productTypeSection = document.getElementById('productTypeSection');
    
    if (!productTypeSection) return;
    
    categoryRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                updateProductTypeOptions(this.value);
                productTypeSection.style.display = 'block';
            }
        });
    });
    
    // Set initial state
    const checkedCategory = document.querySelector('input[name="category"]:checked');
    if (checkedCategory) {
        updateProductTypeOptions(checkedCategory.value);
        productTypeSection.style.display = 'block';
    }
}

// Update product type options based on selected category
function updateProductTypeOptions(category) {
    const productTypeSelect = document.getElementById('productType');
    if (!productTypeSelect) return;
    
    const options = categoryProductTypeMap[category] || [];
    
    // Clear existing options
    productTypeSelect.innerHTML = '';
    
    // Add new options
    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option.value;
        optionElement.textContent = option.text;
        productTypeSelect.appendChild(optionElement);
    });
    
    // Trigger change event
    productTypeSelect.dispatchEvent(new Event('change'));
}

// Initialize product type handler
function initializeProductTypeHandler() {
    const productTypeSelect = document.getElementById('productType');
    if (!productTypeSelect) return;
    
    productTypeSelect.addEventListener('change', function() {
        updatePlatformFeeInfo(this.value);
    });
    
    // Set initial state
    updatePlatformFeeInfo(productTypeSelect.value);
}

// Update platform fee information
function updatePlatformFeeInfo(productType) {
    const platformFeeSection = document.getElementById('platformFeeSection');
    const platformFeeInfo = document.getElementById('platformFeeInfo');
    
    if (!platformFeeSection || !platformFeeInfo) return;
    
    const feeInfo = getPlatformFeeInfo(productType);
    
    if (feeInfo) {
        platformFeeInfo.innerHTML = `
            <div class="row g-2">
                <div class="col-md-6">
                    <small class="text-muted">Phí nền tảng:</small><br>
                    <strong class="text-warning">${feeInfo.platformFee}%</strong>
                </div>
                <div class="col-md-6">
                    <small class="text-muted">Lợi nhuận dự kiến:</small><br>
                    <strong class="text-success">${feeInfo.profitMargin}%</strong>
                </div>
            </div>
            <div class="mt-2">
                <small class="text-muted">${feeInfo.description}</small>
            </div>
        `;
        platformFeeSection.style.display = 'block';
    } else {
        platformFeeSection.style.display = 'none';
    }
}

// Get platform fee information for product type
function getPlatformFeeInfo(productType) {
    const feeMap = {
        'VIETTEL': { platformFee: 5, profitMargin: 15, description: 'Thẻ Viettel có phí thấp, lợi nhuận cao' },
        'MOBIFONE': { platformFee: 5, profitMargin: 15, description: 'Thẻ Mobifone có phí thấp, lợi nhuận cao' },
        'VINAPHONE': { platformFee: 5, profitMargin: 15, description: 'Thẻ Vinaphone có phí thấp, lợi nhuận cao' },
        'VIETTEL_DATA': { platformFee: 8, profitMargin: 12, description: 'Gói data có phí cao hơn thẻ cào' },
        'MOBIFONE_DATA': { platformFee: 8, profitMargin: 12, description: 'Gói data có phí cao hơn thẻ cào' },
        'VINAPHONE_DATA': { platformFee: 8, profitMargin: 12, description: 'Gói data có phí cao hơn thẻ cào' },
        'EMAIL': { platformFee: 10, profitMargin: 20, description: 'Tài khoản email có giá trị cao' },
        'SOCIAL': { platformFee: 12, profitMargin: 18, description: 'Tài khoản mạng xã hội có nhu cầu cao' },
        'STREAMING': { platformFee: 15, profitMargin: 25, description: 'Tài khoản streaming có lợi nhuận cao' },
        'GAMING': { platformFee: 10, profitMargin: 20, description: 'Tài khoản game có cộng đồng lớn' },
        'CLOUD': { platformFee: 8, profitMargin: 15, description: 'Tài khoản cloud cho doanh nghiệp' },
        'GOOGLE_PLAY': { platformFee: 5, profitMargin: 15, description: 'Thẻ Google Play phổ biến' },
        'APPLE_STORE': { platformFee: 5, profitMargin: 15, description: 'Thẻ Apple Store có giá trị cao' },
        'STEAM': { platformFee: 8, profitMargin: 12, description: 'Thẻ Steam cho game thủ' },
        'NETFLIX': { platformFee: 10, profitMargin: 20, description: 'Thẻ Netflix cho giải trí' },
        'SPOTIFY': { platformFee: 10, profitMargin: 20, description: 'Thẻ Spotify cho âm nhạc' },
        'ANTIVIRUS': { platformFee: 15, profitMargin: 25, description: 'Phần mềm antivirus cho bảo mật' },
        'OFFICE': { platformFee: 20, profitMargin: 30, description: 'Phần mềm Office cho văn phòng' },
        'DESIGN': { platformFee: 18, profitMargin: 28, description: 'Phần mềm thiết kế chuyên nghiệp' },
        'DEVELOPMENT': { platformFee: 15, profitMargin: 25, description: 'Phần mềm lập trình cho dev' },
        'OTHER': { platformFee: 10, profitMargin: 15, description: 'Sản phẩm khác' }
    };
    
    return feeMap[productType] || null;
}

// Initialize platform fee handler
function initializePlatformFeeHandler() {
    const platformFeeInput = document.getElementById('platformFee');
    if (!platformFeeInput) return;
    
    // Set default platform fee based on product type
    const productTypeSelect = document.getElementById('productType');
    if (productTypeSelect) {
        productTypeSelect.addEventListener('change', function() {
            const feeInfo = getPlatformFeeInfo(this.value);
            if (feeInfo) {
                platformFeeInput.value = feeInfo.platformFee;
            }
        });
    }
}

// Initialize file upload handler
function initializeFileUploadHandler() {
    const fileInput = document.getElementById('serialFileInput');
    if (!fileInput) return;
    
    fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            showFileInfo(file);
        }
    });
}

// Show file information
function showFileInfo(file) {
    const fileInfo = document.getElementById('fileInfo');
    if (!fileInfo) return;
    
    const fileSize = (file.size / 1024).toFixed(2);
    const fileDate = new Date(file.lastModified).toLocaleString();
    
    fileInfo.innerHTML = `
        <div class="alert alert-info">
            <strong>📁 File đã chọn:</strong> ${file.name}<br>
            <strong>📊 Kích thước:</strong> ${fileSize} KB<br>
            <strong>📅 Ngày tạo:</strong> ${fileDate}
        </div>
    `;
}

// Clear file input
function clearFileInput() {
    const fileInput = document.getElementById('serialFileInput');
    const fileInfo = document.getElementById('fileInfo');
    
    if (fileInput) {
        fileInput.value = '';
    }
    
    if (fileInfo) {
        fileInfo.innerHTML = '';
    }
    
    showTemporaryMessage('File đã được xóa! Bạn có thể upload file mới.', 'success');
}

// Show temporary message
function showTemporaryMessage(message, type = 'info') {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    const container = document.querySelector('.container-fluid .row .col-md-9');
    if (container) {
        container.insertAdjacentHTML('afterbegin', alertHtml);
        
        setTimeout(() => {
            const alert = container.querySelector('.alert');
            if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 3000);
    }
}

// Test API function
function testAPI() {
    showTemporaryMessage('🔍 Đang test API...', 'info');
    
    // Simulate API test
    setTimeout(() => {
        showTemporaryMessage('✅ API hoạt động bình thường!', 'success');
    }, 1000);
}

// Export functions to global scope
window.AddProductUtils = {
    clearFileInput: clearFileInput,
    testAPI: testAPI,
    showTemporaryMessage: showTemporaryMessage
};
