// AddProduct specific JavaScript functions

// Platform fee configuration by product type (Fixed rates)
const platformFeeConfig = {
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

// Handle product type selection
function updateProductTypeGuide() {
    const productType = document.querySelector('select[name="productType"]').value;
    const faceValueSection = document.getElementById('faceValueSection');
    const faceValueSelect = document.getElementById('faceValueSelect');
    const priceInput = document.getElementById('priceInput');
    const platformFeeInput = document.getElementById('platformFeeInput');
    const platformFeeInfo = document.getElementById('platformFeeInfo');
    
    // Hide all guides first
    document.querySelectorAll('.phone-card-guide, .email-account-guide, .gift-card-guide, .software-key-guide, .default-guide').forEach(guide => {
        guide.style.display = 'none';
    });
    
    // Show face value section only for telecom cards
    const telecomCards = ['VIETTEL', 'MOBIFONE', 'VINAPHONE', 'VIETTEL_DATA', 'MOBIFONE_DATA', 'VINAPHONE_DATA'];
    if (telecomCards.includes(productType)) {
        faceValueSection.style.display = 'block';
    } else {
        faceValueSection.style.display = 'none';
        // Reset face value and price when not telecom card
        faceValueSelect.value = '';
        priceInput.value = '';
    }
    
    // Update platform fee based on product type
    if (productType && platformFeeConfig[productType]) {
        const config = platformFeeConfig[productType];
        platformFeeInput.value = config.fee;
        
        platformFeeInfo.innerHTML = `
            <div class="alert alert-success p-2">
                <small>
                    <strong>${config.description}</strong><br>
                    Phí sàn cố định: <strong>${config.fee}%</strong>
                </small>
            </div>
        `;
    } else if (productType) {
        // Only show default fee if a product type is selected but not in config
        platformFeeInput.value = 5;
        platformFeeInfo.innerHTML = `
            <div class="alert alert-info p-2">
                <small>
                    <strong>Phí sàn mặc định: 5%</strong>
                </small>
            </div>
        `;
    } else {
        // Clear everything when no product type is selected
        platformFeeInput.value = '';
        platformFeeInfo.innerHTML = '';
    }
    
    // Show appropriate guide based on product type
    if (telecomCards.includes(productType)) {
        document.querySelector('.phone-card-guide').style.display = 'block';
    } else if (['EMAIL', 'SOCIAL', 'STREAMING', 'APP'].includes(productType)) {
        document.querySelector('.email-account-guide').style.display = 'block';
    } else if (['GIFT', 'VOUCHER', 'COUPON', 'PROMO'].includes(productType)) {
        document.querySelector('.gift-card-guide').style.display = 'block';
    } else if (['SOFTWARE', 'LICENSE', 'ACTIVATION', 'SUBSCRIPTION'].includes(productType)) {
        document.querySelector('.software-key-guide').style.display = 'block';
    } else {
        document.querySelector('.default-guide').style.display = 'block';
    }
    
    // Update profit calculation
    updateProfitCalculation();
}

// Handle face value selection
function updatePriceFromFaceValue() {
    const faceValueSelect = document.getElementById('faceValueSelect');
    const priceInput = document.getElementById('priceInput');
    
    if (faceValueSelect.value && faceValueSelect.value !== 'custom') {
        // Auto-fill price with face value
        priceInput.value = faceValueSelect.value;
    } else if (faceValueSelect.value === 'custom') {
        // Allow custom input
        const customValue = prompt('Nhập mệnh giá tùy chỉnh:');
        if (customValue && !isNaN(customValue)) {
            faceValueSelect.value = customValue;
            priceInput.value = customValue;
        } else {
            faceValueSelect.value = '';
        }
    }
}

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
    // DIGITAL_ACCOUNTS (Tài khoản số)
    'DIGITAL_ACCOUNTS': [
        { value: 'EMAIL', text: 'Tài khoản email' },
        { value: 'SOCIAL', text: 'Tài khoản social media' },
        { value: 'STREAMING', text: 'Tài khoản streaming' },
        { value: 'APP', text: 'Tài khoản ứng dụng' }
    ],
    // GIFTS_VOUCHERS (Quà tặng & Voucher)
    'GIFTS_VOUCHERS': [
        { value: 'GIFT', text: 'Thẻ quà tặng' },
        { value: 'VOUCHER', text: 'Voucher' },
        { value: 'COUPON', text: 'Coupon' },
        { value: 'PROMO', text: 'Mã khuyến mãi' }
    ],
    // SOFTWARE_LICENSES (Phần mềm & License)
    'SOFTWARE_LICENSES': [
        { value: 'SOFTWARE', text: 'Key phần mềm' },
        { value: 'LICENSE', text: 'License key' },
        { value: 'ACTIVATION', text: 'Mã kích hoạt' },
        { value: 'SUBSCRIPTION', text: 'Subscription' }
    ],
    // GAMING (Gaming)
    'GAMING': [
        { value: 'GAME_ACC', text: 'Tài khoản game' },
        { value: 'GAME_ITEM', text: 'Item game' },
        { value: 'GAME_CURRENCY', text: 'Tiền tệ game' },
        { value: 'GAME_CODE', text: 'Gift code game' }
    ],
    // OTHER (Khác)
    'OTHER': [
        { value: 'OTHER', text: 'Khác' }
    ]
};

// Handle category selection change
function updateProductTypeOptions() {
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
    if (englishCategoryName && categoryProductTypeMap[englishCategoryName]) {
        categoryProductTypeMap[englishCategoryName].forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option.value;
            optionElement.textContent = option.text;
            productTypeSelect.appendChild(optionElement);
        });
    }
    
    // Reset product type guide
    updateProductTypeGuide();
}

// Initialize event listener for category change
document.addEventListener('DOMContentLoaded', function() {
    // Wait a bit for DOM to be fully ready
    setTimeout(function() {
        const categorySelect = document.getElementById('categoryId');
        const productTypeSelect = document.getElementById('productTypeSelect');
        
        if (categorySelect) {
            categorySelect.addEventListener('change', function() {
                updateProductTypeOptions();
            });
            
            // Also try onchange attribute as backup
            categorySelect.setAttribute('onchange', 'updateProductTypeOptions()');
            
            // Initial call
            updateProductTypeOptions();
        }
        
    }, 100); // Wait 100ms
    
    initializePlatformFeeHandler();
    initializeFileUploadHandler();
    
    // Initialize platform fee to empty state
    const platformFeeInput = document.getElementById('platformFeeInput');
    const platformFeeInfo = document.getElementById('platformFeeInfo');
    if (platformFeeInput && platformFeeInfo) {
        platformFeeInput.value = '';
        platformFeeInfo.innerHTML = '';
    }
});

// Initialize platform fee handler
function initializePlatformFeeHandler() {
    const platformFeeInput = document.getElementById('platformFeeInput');
    const priceInput = document.getElementById('priceInput');
    
    if (platformFeeInput && priceInput) {
        // Only show platform fee info, no calculation yet
        platformFeeInput.addEventListener('input', function() {
            showPlatformFeeInfo();
        });
        
        // Price change doesn't trigger calculation anymore
        priceInput.addEventListener('input', function() {
            clearProfitCalculation();
        });
    }
}

// Show platform fee info only (no calculation)
function showPlatformFeeInfo() {
    const platformFeeInput = document.getElementById('platformFeeInput');
    const platformFeeInfo = document.getElementById('platformFeeInfo');
    const platformFee = parseFloat(platformFeeInput.value) || 0;
    
    if (platformFee > 0) {
        platformFeeInfo.innerHTML = `
            <div class="alert alert-info p-2">
                <small>
                    <strong>Phí sàn cố định: ${platformFee}%</strong><br>
                    <em>Lợi nhuận sẽ được tính sau khi import Excel</em>
                </small>
            </div>
        `;
    } else {
        platformFeeInfo.innerHTML = '';
    }
}

// Clear profit calculation
function clearProfitCalculation() {
    const platformFeeInfo = document.getElementById('platformFeeInfo');
    const platformFeeInput = document.getElementById('platformFeeInput');
    const platformFee = parseFloat(platformFeeInput.value) || 0;
    
    if (platformFee > 0) {
        platformFeeInfo.innerHTML = `
            <div class="alert alert-info p-2">
                <small>
                    <strong>Phí sàn cố định: ${platformFee}%</strong><br>
                    <em>Lợi nhuận sẽ được tính sau khi import Excel</em>
                    </small>
            </div>
        `;
    } else {
        platformFeeInfo.innerHTML = '';
    }
}

// Initialize file upload handler
function initializeFileUploadHandler() {
    const fileInput = document.getElementById('serialFileInput');
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                showFileInfo(file);
                validateExcelFile(file);
            } else {
                clearFileInfo();
            }
        });
    }
}
// Show file information
function showFileInfo(file) {
    const fileInfo = document.getElementById('fileInfo');
    const fileSize = (file.size / 1024).toFixed(2);
    const fileType = file.name.split('.').pop().toUpperCase();
    
    fileInfo.innerHTML = `
        <div class="alert alert-info p-2">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <strong>📄 ${file.name}</strong><br>
                    <small class="text-muted">Size: ${fileSize} KB | Type: ${fileType}</small>
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm" onclick="clearFileInput()">
                    🗑️ Xóa
                </button>
            </div>
        </div>
    `;
}

// Clear file info
function clearFileInfo() {
    const fileInfo = document.getElementById('fileInfo');
    fileInfo.innerHTML = '';
}
// Clear file input
function clearFileInput() {
    const fileInput = document.getElementById('serialFileInput');
    const fileInfo = document.getElementById('fileInfo');
    const importResults = document.getElementById('importResults');
    
    // Clear file input
    fileInput.value = '';
    
    // Clear file info
    fileInfo.innerHTML = '';
    
    // Hide preview and results
    hideExcelPreview();
    importResults.style.display = 'none';
    
    // Show success message
    showTemporaryMessage('File đã được xóa! Bạn có thể upload file mới.', 'success');
}

// Show temporary message
function showTemporaryMessage(message, type = 'info') {
    const fileInfo = document.getElementById('fileInfo');
    const alertClass = type === 'success' ? 'alert-success' : 'alert-info';
    
    fileInfo.innerHTML = `
        <div class="alert ${alertClass} p-2">
            <small>${message}</small>
        </div>
    `;
    
    // Auto-hide after 3 seconds
    setTimeout(() => {
        fileInfo.innerHTML = '';
    }, 3000);
}

// Show import loading state
function showImportLoading() {
    const importResults = document.getElementById('importResults');
    const importSummary = document.getElementById('importSummary');
    
    importResults.style.display = 'block';
    importSummary.innerHTML = `
        <div class="col-12">
            <div class="alert alert-info">
                <div class="d-flex align-items-center">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                    <span>Đang xử lý file Excel...</span>
                </div>
            </div>
        </div>
    `;
}

// Hide import loading state
function hideImportLoading() {
    // Loading state will be replaced by actual results
}

// Validate Excel file using real API
function validateExcelFile(file) {
    if (!file) return;
    
    showImportLoading();
    
    const productTypeSelect = document.getElementById('productTypeSelect');
    const productType = productTypeSelect.value;
    
    if (!productType) {
        alert('Vui lòng chọn kiểu sản phẩm trước khi upload Excel');
        hideImportLoading();
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('productType', productType);
    
    fetch('/itp/shop/previewExcel', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        hideImportLoading();
        
        if (data.success) {
            // Add price validation
            const priceInput = document.getElementById('priceInput');
            const unitPrice = parseFloat(priceInput.value) || 0;
            
            // Add price validation to results
            if (unitPrice > 0 && data.serials && data.serials.length > 0) {
                const firstSerial = data.serials[0];
                if (firstSerial.faceValue && firstSerial.faceValue !== unitPrice) {
                    data.warnings = data.warnings || [];
                    data.warnings.push(
                        `Giá trong form (${unitPrice.toLocaleString()} ₫) khác với giá trong Excel (${firstSerial.faceValue.toLocaleString()} ₫)`
                    );
                }
            }
            
            // Display results
            displayImportResults(data);
        } else {
            // Show error
            const importResults = document.getElementById('importResults');
            importResults.innerHTML = `
                <div class="card">
                    <div class="card-header bg-danger text-white">
                        <h6 class="mb-0">❌ Lỗi Import</h6>
                    </div>
                    <div class="card-body">
                        <div class="alert alert-danger">
                            <strong>Lỗi:</strong> ${data.message || data.error || 'Không thể xử lý file Excel'}
                        </div>
                    </div>
                </div>
            `;
            importResults.style.display = 'block';
        }
    })
    .catch(error => {
        hideImportLoading();
        
        const importResults = document.getElementById('importResults');
        importResults.innerHTML = `
            <div class="card">
                <div class="card-header bg-danger text-white">
                    <h6 class="mb-0">❌ Lỗi Kết Nối</h6>
                </div>
                <div class="card-body">
                    <div class="alert alert-danger">
                        <strong>Lỗi:</strong> Không thể kết nối đến server. Vui lòng thử lại.
                    </div>
                </div>
            </div>
        `;
        importResults.style.display = 'block';
    });
}

// Display import results from API
function displayImportResults(data) {
    const importResults = document.getElementById('importResults');
    const importSummary = document.getElementById('importSummary');
    const importDetails = document.getElementById('importDetails');
    
    // Show import results section
    importResults.style.display = 'block';
    
    // Display summary cards
    importSummary.innerHTML = `
        <div class="col-md-3">
            <div class="card bg-primary text-white">
                <div class="card-body text-center">
                    <h5 class="card-title">${data.totalRows || 0}</h5>
                    <p class="card-text mb-0">Tổng dòng</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-success text-white">
                <div class="card-body text-center">
                    <h5 class="card-title">${data.importedCount || 0}</h5>
                    <p class="card-text mb-0">Thành công</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-warning text-white">
                <div class="card-body text-center">
                    <h5 class="card-title">${data.skippedCount || 0}</h5>
                    <p class="card-text mb-0">Bỏ qua</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-danger text-white">
                <div class="card-body text-center">
                    <h5 class="card-title">${(data.errors || []).length}</h5>
                    <p class="card-text mb-0">Lỗi</p>
                </div>
            </div>
        </div>
    `;
    
    // Display details
    let detailsHtml = '';
    
    if (data.errors && data.errors.length > 0) {
        detailsHtml += `
            <div class="col-md-6">
                <div class="card border-danger">
                    <div class="card-header bg-danger text-white">
                        <h6 class="mb-0">❌ Lỗi Import</h6>
                    </div>
                    <div class="card-body">
                        <ul class="mb-0">
                            ${data.errors.map(error => `<li class="text-danger">${error}</li>`).join('')}
                        </ul>
                    </div>
                </div>
            </div>
        `;
    }
    
    if (data.warnings && data.warnings.length > 0) {
        detailsHtml += `
            <div class="col-md-6">
                <div class="card border-warning">
                    <div class="card-header bg-warning text-white">
                        <h6 class="mb-0">⚠️ Cảnh báo</h6>
                    </div>
                    <div class="card-body">
                        <ul class="mb-0">
                            ${data.warnings.map(warning => {
                                // Highlight price validation warnings
                                if (warning.includes('Giá trong form')) {
                                    return `<li class="text-warning"><strong>💰 ${warning}</strong></li>`;
                                }
                                return `<li class="text-warning">${warning}</li>`;
                            }).join('')}
                        </ul>
                    </div>
                </div>
            </div>
        `;
    }
    
    if (data.duplicateSerials && data.duplicateSerials.length > 0) {
        detailsHtml += `
            <div class="col-md-6">
                <div class="card border-info">
                    <div class="card-header bg-info text-white">
                        <h6 class="mb-0">🔄 Serial trùng lặp</h6>
                    </div>
                    <div class="card-body">
                        <ul class="mb-0">
                            ${data.duplicateSerials.map(serial => `<li class="text-info">${serial}</li>`).join('')}
                        </ul>
                    </div>
                </div>
            </div>
        `;
    }
    
    importDetails.innerHTML = detailsHtml;
    
    // Show serial list table
    if (data.serials && data.serials.length > 0) {
        showSerialListTable(data.serials);
        showValidationTable(data.serials);
    }
    
    // Show success message and profit calculation if all imports successful
    if ((!data.errors || data.errors.length === 0) && (data.skippedCount === 0 || !data.skippedCount)) {
        // Calculate profit after successful import
        const priceInput = document.getElementById('priceInput');
        const platformFeeInput = document.getElementById('platformFeeInput');
        const unitPrice = parseFloat(priceInput.value) || 0;
        const platformFee = parseFloat(platformFeeInput.value) || 0;
        
        if (unitPrice > 0 && platformFee > 0 && data.importedCount > 0) {
            const profitData = calculateProfitAfterImport(data.importedCount, unitPrice, platformFee);
            
            importSummary.innerHTML += `
                <div class="col-12 mt-3">
                    <div class="alert alert-success">
                        <strong>✅ Import thành công!</strong> Tất cả ${data.importedCount} serials đã được thêm vào hệ thống.
                    </div>
                </div>
                <div class="col-12 mt-2">
                    <div class="card border-success">
                        <div class="card-header bg-success text-white">
                            <h6 class="mb-0">💰 Tính Toán Lợi Nhuận</h6>
                        </div>
                        <div class="card-body">
                            <div class="row g-2">
                                <div class="col-md-3">
                                    <small class="text-muted">Serial đã nhập:</small><br>
                                    <strong class="text-primary">${profitData.importedCount} serial</strong>
                                </div>
                                <div class="col-md-3">
                                    <small class="text-muted">Giá đơn vị:</small><br>
                                    <strong class="text-info">${profitData.unitPrice.toLocaleString()} ₫</strong>
                                </div>
                                <div class="col-md-3">
                                    <small class="text-muted">Tổng doanh thu:</small><br>
                                    <strong class="text-primary">${profitData.totalRevenue.toLocaleString()} ₫</strong>
                                </div>
                                <div class="col-md-3">
                                    <small class="text-muted">Phí sàn (${profitData.platformFee}%):</small><br>
                                    <strong class="text-warning">-${profitData.platformFeeAmount.toLocaleString()} ₫</strong>
                                </div>
                                <div class="col-12">
                                    <hr class="my-2">
                                    <div class="text-center">
                                        <small class="text-muted">Lợi nhuận thực tế:</small><br>
                                        <strong class="text-success fs-4">${profitData.netProfit.toLocaleString()} ₫</strong>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } else {
            importSummary.innerHTML += `
                <div class="col-12 mt-3">
                    <div class="alert alert-success">
                        <strong>✅ Import thành công!</strong> Tất cả ${data.importedCount} serials đã được thêm vào hệ thống.
                    </div>
                </div>
            `;
        }
    }
}

// Calculate profit after Excel import
function calculateProfitAfterImport(importedCount, unitPrice, platformFee) {
    const totalRevenue = importedCount * unitPrice;
    const platformFeeAmount = totalRevenue * (platformFee / 100);
    const netProfit = totalRevenue - platformFeeAmount;
    
    return {
        totalRevenue: totalRevenue,
        platformFeeAmount: platformFeeAmount,
        netProfit: netProfit,
        importedCount: importedCount,
        unitPrice: unitPrice,
        platformFee: platformFee
    };
}

// Show serial list table
function showSerialListTable(serials) {
    const serialListSection = document.getElementById('serialListSection');
    const serialListBody = document.getElementById('serialListBody');
    
    if (serials && serials.length > 0) {
        serialListBody.innerHTML = serials.map((serial, index) => `
            <tr>
                <td>${index + 1}</td>
                <td><code>${serial.serialCode}</code></td>
                <td><code>${serial.secretCode}</code></td>
                <td><strong>${serial.faceValue.toLocaleString()} ₫</strong></td>
                <td><small>${serial.information}</small></td>
                <td><span class="badge bg-success">${serial.status}</span></td>
            </tr>
        `).join('');
        
        serialListSection.style.display = 'block';
    } else {
        serialListSection.style.display = 'none';
    }
}

// Show validation check table
function showValidationTable(serials) {
    const validationTableSection = document.getElementById('validationTableSection');
    const validationTableBody = document.getElementById('validationTableBody');
    
    if (serials && serials.length > 0) {
        validationTableBody.innerHTML = serials.map(serial => {
            const statusBadge = serial.validation === 'PASS' ? 
                '<span class="badge bg-success">PASS</span>' : 
                '<span class="badge bg-danger">FAIL</span>';
            
            const errorsHtml = serial.errors.length > 0 ? 
                serial.errors.map(error => `<small class="text-danger">• ${error}</small>`).join('<br>') : 
                '<small class="text-muted">Không có</small>';
            
            const warningsHtml = serial.warnings.length > 0 ? 
                serial.warnings.map(warning => `<small class="text-warning">• ${warning}</small>`).join('<br>') : 
                '<small class="text-muted">Không có</small>';
            
            return `
                <tr>
                    <td><strong>${serial.row}</strong></td>
                    <td><code>${serial.serialCode}</code></td>
                    <td>${statusBadge}</td>
                    <td>${errorsHtml}</td>
                    <td>${warningsHtml}</td>
                    <td><span class="badge bg-info">${serial.status}</span></td>
                </tr>
            `;
        }).join('');
        
        validationTableSection.style.display = 'block';
    } else {
        validationTableSection.style.display = 'none';
    }
}

// Preview Excel file content
function previewExcelFile(input) {
    const file = input.files[0];
    if (!file) {
        hideExcelPreview();
        return;
    }
    
    if (!file.name.toLowerCase().endsWith('.xlsx') && !file.name.toLowerCase().endsWith('.xls')) {
        alert('Vui lòng chọn file Excel (.xlsx hoặc .xls)');
        input.value = '';
        hideExcelPreview();
        return;
    }
    
    // Show file info
    const fileInfo = document.getElementById('fileInfo');
    fileInfo.innerHTML = `
        <div class="alert alert-success">
            <strong>✅ File Excel đã sẵn sàng!</strong><br>
            <strong>📁 Tên file:</strong> ${file.name}<br>
            <strong>📊 Kích thước:</strong> ${(file.size / 1024).toFixed(2)} KB<br>
            <strong>📅 Ngày tạo:</strong> ${new Date(file.lastModified).toLocaleString()}<br>
            <small class="text-muted">Đang xử lý và hiển thị danh sách serials sẽ được thêm vào...</small>
        </div>
    `;
    
    // Parse Excel file
    parseExcelFile(file);
}

// Parse Excel file and display preview
function parseExcelFile(file) {
    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = new Uint8Array(e.target.result);
            const workbook = XLSX.read(data, {type: 'array'});
            const sheetName = workbook.SheetNames[0];
            const worksheet = workbook.Sheets[sheetName];
            
            // Convert to JSON
            const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1});
            
            if (jsonData.length < 2) {
                showExcelPreviewError('File Excel không có dữ liệu hoặc chỉ có header');
                return;
            }
            
            // Validate headers
            const headers = jsonData[0];
            const expectedHeaders = ['Serial Code', 'Secret Code', 'Face Value', 'Information'];
            
            if (headers.length < expectedHeaders.length) {
                showExcelPreviewError(`File Excel thiếu cột. Cần có: ${expectedHeaders.join(', ')}`);
                return;
            }
            
            // Parse data rows
            const serials = [];
            for (let i = 1; i < jsonData.length; i++) {
                const row = jsonData[i];
                if (row.length === 0 || !row[0]) continue; // Skip empty rows
                
                const serial = {
                    serialCode: row[0] || '',
                    secretCode: row[1] || '',
                    faceValue: parseFloat(row[2]) || 0,
                    information: row[3] || '',
                    status: 'READY',
                    validation: 'PASS',
                    errors: [],
                    warnings: []
                };
                
                // Basic validation
                if (!serial.serialCode.trim()) {
                    serial.validation = 'FAIL';
                    serial.errors.push('Serial code trống');
                }
                
                if (serial.faceValue <= 0) {
                    serial.validation = 'FAIL';
                    serial.errors.push('Face value không hợp lệ');
                }
                
                serials.push(serial);
            }
            
            showExcelPreview(serials);
            
        } catch (error) {
            console.error('Error parsing Excel:', error);
            showExcelPreviewError('Lỗi đọc file Excel: ' + error.message);
        }
    };
    
    reader.readAsArrayBuffer(file);
}

// Show Excel preview table
function showExcelPreview(serials) {
    const previewSection = document.getElementById('excelPreview');
    const previewBody = document.getElementById('excelPreviewBody');
    const totalSerialsSpan = document.getElementById('totalSerials');
    
    if (serials && serials.length > 0) {
        // Update total count
        totalSerialsSpan.textContent = serials.length;
        
        previewBody.innerHTML = serials.map((serial, index) => `
            <tr>
                <td>${index + 1}</td>
                <td><code>${serial.serialCode}</code></td>
                <td><code>${serial.secretCode}</code></td>
                <td><strong>${serial.faceValue.toLocaleString()} ₫</strong></td>
                <td><small>${serial.information}</small></td>
                <td>
                    <span class="badge ${serial.validation === 'PASS' ? 'bg-success' : 'bg-danger'}">
                        ${serial.validation === 'PASS' ? 'Sẵn sàng' : 'Có lỗi'}
                    </span>
                </td>
            </tr>
        `).join('');
        
        previewSection.style.display = 'block';
    } else {
        hideExcelPreview();
    }
}

// Show Excel preview error
function showExcelPreviewError(message) {
    const previewSection = document.getElementById('excelPreview');
    const previewBody = document.getElementById('excelPreviewBody');
    
    previewBody.innerHTML = `
        <tr>
            <td colspan="6" class="text-center text-danger">
                <i class="fas fa-exclamation-triangle"></i> ${message}
            </td>
        </tr>
    `;
    
    previewSection.style.display = 'block';
}

// Hide Excel preview
function hideExcelPreview() {
    const previewSection = document.getElementById('excelPreview');
    previewSection.style.display = 'none';
}

// Confirm submit with preview
function confirmSubmit() {
    const fileInput = document.getElementById('serialFileInput');
    const previewSection = document.getElementById('excelPreview');
    
    if (!fileInput.files[0]) {
        alert('Vui lòng chọn file Excel trước khi tạo sản phẩm!');
        return false;
    }
    
    if (previewSection.style.display === 'none') {
        alert('Vui lòng đợi file Excel được xử lý hoàn tất!');
        return false;
    }
    
    // Use AJAX confirmation instead of simple confirm
    const form = document.querySelector('form[th\\:action="@{/shop/addProduct}"]');
    if (form && window.submitFormWithConfirmation) {
        submitFormWithConfirmation(form);
        return false; // Prevent default form submission
    }
    
    // Fallback to original confirmation if AJAX not available
    const totalSerials = document.getElementById('totalSerials').textContent;
    const productName = document.querySelector('input[name="productName"]').value;
    
    const confirmMessage = `
Bạn có chắc chắn muốn tạo sản phẩm "${productName}" với ${totalSerials} serials?

Sau khi tạo, ${totalSerials} serials sẽ được thêm vào hệ thống và sản phẩm sẽ có trạng thái HIDDEN.

Bạn có thể thay đổi trạng thái sang ACTIVE sau khi tạo xong.
    `.trim();
    
    return confirm(confirmMessage);
}

// Update profit calculation
function updateProfitCalculation() {
    // This function is called when product type changes
    // Implementation can be added here if needed
}