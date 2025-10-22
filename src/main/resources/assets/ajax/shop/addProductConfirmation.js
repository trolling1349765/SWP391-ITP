
function showAddProductConfirmation(productData, onConfirm, onCancel) {
    // Create confirmation message
    const message = `
        <div class="confirmation-dialog">
            <div class="confirmation-header">
                <h5><i class="fas fa-question-circle text-warning"></i> Xác nhận thêm sản phẩm</h5>
            </div>
            <div class="confirmation-body">
                <p><strong>Bạn có chắc muốn add thêm sản phẩm không?</strong></p>
                <div class="product-info">
                    <p><strong>Tên sản phẩm:</strong> ${productData.productName || 'Chưa nhập'}</p>
                    <p><strong>Giá:</strong> ${productData.price ? productData.price.toLocaleString() + ' ₫' : 'Chưa nhập'}</p>
                    <p><strong>Danh mục:</strong> ${productData.categoryName || 'Chưa chọn'}</p>
                    <p><strong>Số serials:</strong> ${productData.serialCount || 0} serials</p>
                </div>
                <div class="alert alert-info">
                    <small><i class="fas fa-info-circle"></i> Sản phẩm sẽ được tạo với trạng thái HIDDEN và có thể thay đổi sau.</small>
                </div>
            </div>
            <div class="confirmation-footer">
                <button type="button" class="btn btn-success" onclick="confirmAddProduct()">
                    <i class="fas fa-check"></i> Có, thêm sản phẩm
                </button>
                <button type="button" class="btn btn-secondary" onclick="cancelAddProduct()">
                    <i class="fas fa-times"></i> Hủy bỏ
                </button>
            </div>
        </div>
    `;

    // Show modal
    showConfirmationModal(message, onConfirm, onCancel);
}
function showConfirmationModal(content, onConfirm, onCancel) {
    // Remove existing modal if any
    const existingModal = document.getElementById('addProductConfirmationModal');
    if (existingModal) {
        existingModal.remove();
    }

    // Create modal HTML
    const modalHTML = `
        <div class="modal fade" id="addProductConfirmationModal" tabindex="-1" aria-labelledby="addProductConfirmationModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header bg-warning text-dark">
                        <h5 class="modal-title" id="addProductConfirmationModalLabel">
                            <i class="fas fa-exclamation-triangle"></i> Xác nhận thêm sản phẩm
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        ${content}
                    </div>
                </div>
            </div>
        </div>
    `;

    // Add modal to body
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('addProductConfirmationModal'));
    modal.show();

    // Store callbacks globally for access from buttons
    window.addProductConfirmCallback = onConfirm;
    window.addProductCancelCallback = onCancel;

    // Handle modal close events
    const modalElement = document.getElementById('addProductConfirmationModal');
    modalElement.addEventListener('hidden.bs.modal', function() {
        if (window.addProductCancelCallback) {
            window.addProductCancelCallback();
        }
        modalElement.remove();
    });
}

function confirmAddProduct() {
    // Hide modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('addProductConfirmationModal'));
    modal.hide();

    // Execute confirm callback
    if (window.addProductConfirmCallback) {
        window.addProductConfirmCallback();
    }

    // Clean up
    window.addProductConfirmCallback = null;
    window.addProductCancelCallback = null;
}

function cancelAddProduct() {
    // Hide modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('addProductConfirmationModal'));
    modal.hide();

    // Execute cancel callback
    if (window.addProductCancelCallback) {
        window.addProductCancelCallback();
    }

    // Clean up
    window.addProductConfirmCallback = null;
    window.addProductCancelCallback = null;
}

function submitProductFormAjax(formData, onSuccess, onError) {
    // Show loading indicator
    showLoadingIndicator();

    fetch('/itp/shop/addProduct', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        hideLoadingIndicator();
        
        if (response.ok) {
            return response.text();
        } else {
            throw new Error('Server error: ' + response.status);
        }
    })
    .then(data => {
        if (onSuccess) {
            onSuccess(data);
        }
        showSuccessMessage('Sản phẩm đã được thêm thành công!');
    })
    .catch(error => {
        hideLoadingIndicator();
        console.error('Error adding product:', error);
        
        if (onError) {
            onError(error);
        }
        showErrorMessage('Có lỗi xảy ra khi thêm sản phẩm: ' + error.message);
    });
}

function showLoadingIndicator() {
    const loadingHTML = `
        <div id="addProductLoading" class="loading-overlay">
            <div class="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Đang xử lý...</span>
                </div>
                <p class="mt-2">Đang thêm sản phẩm...</p>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', loadingHTML);
}


function hideLoadingIndicator() {
    const loadingElement = document.getElementById('addProductLoading');
    if (loadingElement) {
        loadingElement.remove();
    }
}

function showSuccessMessage(message) {
    const alertHTML = `
        <div class="alert alert-success alert-dismissible fade show position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
            <i class="fas fa-check-circle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', alertHTML);
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        const alert = document.querySelector('.alert-success');
        if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, 5000);
}

function showErrorMessage(message) {
    const alertHTML = `
        <div class="alert alert-danger alert-dismissible fade show position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
            <i class="fas fa-exclamation-triangle"></i> ${message}
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', alertHTML);
    
    // Auto-hide after 7 seconds
    setTimeout(() => {
        const alert = document.querySelector('.alert-danger');
        if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, 7000);
}


function submitFormWithConfirmation(form) {
    // Collect form data
    const formData = new FormData(form);
    
    // Get product information for confirmation
    const productData = {
        productName: formData.get('productName'),
        price: parseFloat(formData.get('price')) || 0,
        categoryName: getSelectedCategoryName(),
        serialCount: getSerialCount()
    };

    // Show confirmation dialog
    showAddProductConfirmation(productData, 
        // On confirm
        function() {
            submitProductFormAjax(formData,
                // On success
                function(response) {
                    // Redirect to dashboard after success
                    setTimeout(() => {
                        window.location.href = '/itp/shop/dashboard';
                    }, 1500);
                },
                // On error
                function(error) {
                    console.error('Failed to add product:', error);
                }
            );
        },
        // On cancel
        function() {
            console.log('User cancelled adding product');
        }
    );
}

function getSelectedCategoryName() {
    const categorySelect = document.querySelector('select[name="categoryId"]');
    if (categorySelect && categorySelect.selectedIndex > 0) {
        return categorySelect.options[categorySelect.selectedIndex].text;
    }
    return 'Chưa chọn';
}


function getSerialCount() {
    const totalSerialsSpan = document.getElementById('totalSerials');
    if (totalSerialsSpan) {
        return parseInt(totalSerialsSpan.textContent) || 0;
    }
    return 0;
}

// CSS styles for confirmation dialog
const confirmationStyles = `
    <style>
        .confirmation-dialog {
            padding: 0;
        }
        
        .confirmation-header {
            background-color: #fff3cd;
            padding: 15px;
            border-bottom: 1px solid #ffeaa7;
            margin: -15px -15px 15px -15px;
        }
        
        .confirmation-body {
            padding: 0;
        }
        
        .product-info {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin: 15px 0;
        }
        
        .product-info p {
            margin-bottom: 8px;
        }
        
        .confirmation-footer {
            display: flex;
            gap: 10px;
            justify-content: flex-end;
            margin-top: 20px;
        }
        
        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        }
        
        .loading-spinner {
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            text-align: center;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
        
        .loading-spinner p {
            margin-top: 15px;
            color: #666;
        }
    </style>
`;

// Add styles to head
document.head.insertAdjacentHTML('beforeend', confirmationStyles);

// Export functions for global access
window.showAddProductConfirmation = showAddProductConfirmation;
window.submitFormWithConfirmation = submitFormWithConfirmation;
window.submitProductFormAjax = submitProductFormAjax;
