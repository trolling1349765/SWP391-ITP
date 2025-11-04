// Update Product JavaScript
// Category to ProductType mapping
const categoryProductTypeMap = {
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

// Vietnamese to English category mapping (from database Vietnamese names)
const displayNameToCategoryMap = {
    'Thẻ điện thoại': 'TELECOM',
    'Tài khoản số': 'DIGITAL_ACCOUNTS',
    'Phần mềm': 'SOFTWARE_LICENSES',
    'Khác': 'OTHER'
};

// Current product type will be set from DOM on page load
let currentProductType = '';

// Update product type options based on category
function updateProductTypeOptions() {
    const categorySelect = document.getElementById('categorySelect');
    const productTypeSelect = document.getElementById('productTypeSelect');
    
    if (!categorySelect || !productTypeSelect) return;
    
    const selectedCategoryText = categorySelect.options[categorySelect.selectedIndex].text;
    const englishCategoryName = displayNameToCategoryMap[selectedCategoryText];
    
    // Debug logging
    console.log('=== Update Product - Category Selection ===');
    console.log('Selected Text:', selectedCategoryText);
    console.log('Mapped English Category:', englishCategoryName);
    console.log('Available Product Types:', englishCategoryName ? categoryProductTypeMap[englishCategoryName] : 'NOT FOUND');
    
    // Clear existing options
    productTypeSelect.innerHTML = '<option value="">-- Chọn kiểu sản phẩm --</option>';
    
    // Add new options based on category
    if (englishCategoryName && categoryProductTypeMap[englishCategoryName]) {
        categoryProductTypeMap[englishCategoryName].forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option.value;
            optionElement.textContent = option.text;
            
            // Preserve current selection
            if (option.value === currentProductType) {
                optionElement.selected = true;
            }
            
            productTypeSelect.appendChild(optionElement);
        });
    }
}

// Upload ảnh mới ngay lập tức - NHANH
function uploadNewImage(input) {
    const file = input.files[0];
    if (!file) return;
    
    const statusEl = document.getElementById('uploadStatus');
    const preview = document.getElementById('imagePreview');
    const previewImg = document.getElementById('previewImg');
    const imgInput = document.querySelector('input[name="img"]');
    
    statusEl.innerHTML = '<i class="fas fa-spinner fa-spin text-primary"></i> Đang upload...';
    preview.style.display = 'none';
    
    const formData = new FormData();
    formData.append('file', file);
    
    fetch('/itp/shop/uploadImage', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const imagePath = data.imagePath;
            imgInput.value = imagePath; // Cập nhật hidden input
            previewImg.src = imagePath;
            preview.style.display = 'block';
            statusEl.innerHTML = '<i class="fas fa-check-circle text-success"></i> Upload thành công!';
            console.log(' Image updated:', imagePath);
        } else {
            statusEl.innerHTML = '<i class="fas fa-times-circle text-danger"></i> ' + data.message;
        }
    })
    .catch(error => {
        console.error('Upload error:', error);
        statusEl.innerHTML = '<i class="fas fa-times-circle text-danger"></i> Lỗi upload';
    });
}

// Xóa ảnh mới
function removeNewImage() {
    document.getElementById('imageInput').value = '';
    document.getElementById('imagePreview').style.display = 'none';
    document.getElementById('uploadStatus').innerHTML = '';
    // Giữ lại ảnh cũ trong hidden input
}

// Excel upload functions
function previewExcelFile(input) {
    const file = input.files[0];
    if (!file) {
        hideExcelPreview();
        return;
    }
    
    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = new Uint8Array(e.target.result);
            const workbook = XLSX.read(data, { type: 'array' });
            const worksheet = workbook.Sheets[workbook.SheetNames[0]];
            const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
            
            parseExcelFile(jsonData);
            
        } catch (error) {
            showExcelPreviewError('Lỗi đọc file Excel: ' + error.message);
        }
    };
    
    reader.readAsArrayBuffer(file);
}

function parseExcelFile(data) {
    if (data.length < 2) {
        showExcelPreviewError('File Excel không có dữ liệu hoặc định dạng không đúng');
        return;
    }
    
    const headers = data[0];
    const expectedHeaders = ['Serial Code', 'Secret Code'];
    
    if (!expectedHeaders.every(header => headers.includes(header))) {
        showExcelPreviewError('File Excel không đúng định dạng. Cần có các cột: ' + expectedHeaders.join(', '));
        return;
    }
    
    const serials = [];
    let validCount = 0;
    let errorCount = 0;
    let duplicateCount = 0;
    
    for (let i = 1; i < data.length; i++) {
        const row = data[i];
        if (row.length < 2) continue;
        
        const serialCode = row[0] ? String(row[0]).trim() : '';
        const secretCode = row[1] ? String(row[1]).trim() : '';
        
        if (serialCode && secretCode) {
            const isDuplicateInFile = serials.some(s => s.serialCode === serialCode);
            
            serials.push({
                serialCode: serialCode,
                secretCode: secretCode,
                status: isDuplicateInFile ? 'duplicate' : 'ready'
            });
            
            if (isDuplicateInFile) {
                duplicateCount++;
            } else {
                validCount++;
            }
        } else {
            serials.push({
                serialCode: serialCode || '',
                secretCode: secretCode || '',
                status: 'error'
            });
            errorCount++;
        }
    }
    
    if (serials.length === 0) {
        showExcelPreviewError('Không tìm thấy dữ liệu mã sản phẩm nào trong file Excel');
        return;
    }
    
    checkSerialDuplicates(serials, validCount, errorCount, duplicateCount);
}

function checkSerialDuplicates(serials, validCount, errorCount, duplicateCount) {
    const validSerials = serials.filter(s => s.status === 'ready');
    
    if (validSerials.length === 0) {
        showExcelPreview(serials, validCount, errorCount, duplicateCount);
        return;
    }
    
    const serialCodes = validSerials.map(s => s.serialCode);
    
    fetch('/itp/shop/check-serial-duplicates', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(serialCodes)
    })
    .then(response => response.json())
    .then(data => {
        serials.forEach(serial => {
            if (serial.status === 'ready' && data.duplicates.includes(serial.serialCode)) {
                serial.status = 'duplicate_db';
            }
        });
        
        const dbDuplicateCount = data.duplicates.length;
        const finalValidCount = validCount - dbDuplicateCount;
        
        showExcelPreview(serials, finalValidCount, errorCount, duplicateCount, dbDuplicateCount);
    })
    .catch(error => {
        console.error('Error checking duplicates:', error);
        showExcelPreview(serials, validCount, errorCount, duplicateCount);
    });
}

function showExcelPreview(serials, validCount, errorCount, duplicateCount = 0, dbDuplicateCount = 0) {
    const previewDiv = document.getElementById('excelPreview');
    if (!previewDiv) return;
    
    updateSubmitButtonState(validCount, duplicateCount, dbDuplicateCount);
    
    let tableHTML = `
        <div class="card mt-3">
            <div class="card-header">
                <h6 class="mb-0">Danh sách sản phẩm sẽ được thêm vào</h6>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-sm table-striped mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>STT</th>
                                <th>Serial Code</th>
                                <th>Secret Code</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
    `;
    
    serials.forEach((serial, index) => {
        let statusBadge;
        switch(serial.status) {
            case 'ready':
                statusBadge = '<span class="badge bg-success">Sẵn sàng</span>';
                break;
            case 'duplicate':
                statusBadge = '<span class="badge bg-warning">Trùng trong file</span>';
                break;
            case 'duplicate_db':
                statusBadge = '<span class="badge bg-danger">Trùng trong DB</span>';
                break;
            case 'error':
            default:
                statusBadge = '<span class="badge bg-danger">Có lỗi</span>';
                break;
        }
        
        tableHTML += `
            <tr>
                <td>${index + 1}</td>
                <td>${serial.serialCode}</td>
                <td>${serial.secretCode}</td>
                <td>${statusBadge}</td>
            </tr>
        `;
    });
    
    tableHTML += `
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="card-footer">
                    <small class="text-muted">
                        Tổng cộng: ${serials.length} serials sẽ được thêm vào
                        ${validCount > 0 ? ` (${validCount} hợp lệ)` : ''}
                        ${errorCount > 0 ? ` (${errorCount} có lỗi)` : ''}
                    </small>
                </div>
            </div>
        </div>
    `;
    
    previewDiv.innerHTML = tableHTML;
    previewDiv.style.display = 'block';
}

function showExcelPreviewError(message) {
    const previewDiv = document.getElementById('excelPreview');
    if (!previewDiv) return;
    
    previewDiv.innerHTML = `
        <div class="alert alert-danger mt-3">
            <strong>Lỗi:</strong> ${message}
        </div>
    `;
    previewDiv.style.display = 'block';
}

function hideExcelPreview() {
    const previewDiv = document.getElementById('excelPreview');
    if (previewDiv) {
        previewDiv.style.display = 'none';
        previewDiv.innerHTML = '';
    }
}

function updateSubmitButtonState(validCount, duplicateCount, dbDuplicateCount) {
    const submitButton = document.querySelector('button[type="submit"]');
    const totalDuplicates = duplicateCount + dbDuplicateCount;
    
    if (!submitButton) return;
    
    if (totalDuplicates > 0) {
        submitButton.disabled = true;
        submitButton.classList.remove('btn-primary');
        submitButton.classList.add('btn-secondary');
        submitButton.title = `Không thể submit vì có ${totalDuplicates} sản phẩm trùng`;
        
        let warningMsg = document.getElementById('submitWarning');
        if (!warningMsg) {
            warningMsg = document.createElement('div');
            warningMsg.id = 'submitWarning';
            warningMsg.className = 'alert alert-warning mt-2';
            submitButton.parentNode.appendChild(warningMsg);
        }
        warningMsg.innerHTML = ` Không thể cập nhật sản phẩm vì có ${totalDuplicates} mã sản phẩm trùng. Vui lòng sửa file Excel.`;
    } else if (validCount > 0) {
        submitButton.disabled = false;
        submitButton.classList.remove('btn-secondary');
        submitButton.classList.add('btn-primary');
        submitButton.title = `Có thể cập nhật với ${validCount} mã sản phẩm hợp lệ`;
        
        const warningMsg = document.getElementById('submitWarning');
        if (warningMsg) {
            warningMsg.remove();
        }
    } else {
        submitButton.disabled = false; // Allow update even without Excel
        submitButton.classList.remove('btn-secondary');
        submitButton.classList.add('btn-primary');
        submitButton.title = '';
        
        const warningMsg = document.getElementById('submitWarning');
        if (warningMsg) {
            warningMsg.remove();
        }
    }
}

function clearFileInput() {
    const fileInput = document.getElementById('serialFileInput');
    const fileInfo = document.getElementById('fileInfo');
    const previewDiv = document.getElementById('excelPreview');
    
    if (fileInput) {
        fileInput.value = '';
    }
    
    if (fileInfo) {
        fileInfo.innerHTML = '';
    }
    
    if (previewDiv) {
        previewDiv.style.display = 'none';
        previewDiv.innerHTML = '';
    }
    
    const submitButton = document.querySelector('button[type="submit"]');
    if (submitButton) {
        submitButton.disabled = false;
        submitButton.classList.remove('btn-secondary');
        submitButton.classList.add('btn-primary');
        submitButton.title = '';
    }
    
    const warningMsg = document.getElementById('submitWarning');
    if (warningMsg) {
        warningMsg.remove();
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Get current product type from select element or data attribute
    const productTypeSelect = document.getElementById('productTypeSelect');
    if (productTypeSelect) {
        // Try to get from value first (if already set by Thymeleaf)
        currentProductType = productTypeSelect.value || '';
        // If not set, try data attribute
        if (!currentProductType) {
            currentProductType = productTypeSelect.getAttribute('data-current-type') || '';
        }
    }
    
    // Initialize product type options based on current category
    updateProductTypeOptions();
    
    // Fix form submission
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            const excelFile = document.getElementById('serialFileInput');
            const previewDiv = document.getElementById('excelPreview');
            
            // Check if there are any duplicate serials
            if (previewDiv && previewDiv.style.display !== 'none') {
                const duplicateBadges = previewDiv.querySelectorAll('.badge.bg-danger, .badge.bg-warning');
                if (duplicateBadges.length > 0) {
                    e.preventDefault();
                    alert('Không thể cập nhật sản phẩm vì có serial trùng!\n\nVui lòng:\n- Xóa các serial trùng trong file Excel\n- Hoặc xóa file và upload file mới\n- Hoặc sử dụng serial khác');
                    return false;
                }
            }
            
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang cập nhật...';
            }
        });
    }
    
    // Fix navigation buttons
    const navBtns = document.querySelectorAll('a[href*="/shop/"]');
    navBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            this.style.opacity = '0.6';
        });
    });
});

