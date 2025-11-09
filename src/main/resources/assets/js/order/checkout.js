/**
 * Checkout Page JavaScript
 * Handles quantity calculation, form submission, and validation
 */

// Global variables - will be initialized from server
let unitPrice = 0;
let maxStock = 0;
let userBalance = 0;

/**
 * Initialize checkout page with server data
 */
function initCheckout(price, stock, balance) {
    unitPrice = price;
    maxStock = stock;
    userBalance = balance;
    
    console.log('Initialized:', { unitPrice, maxStock, userBalance });
}

/**
 * Update total amount and check balance
 */
function updateTotal() {
    const input = document.getElementById('quantityInput');
    let quantity = parseInt(input.value) || 1;
    
    // Validate quantity
    if (quantity < 1) {
        quantity = 1;
        input.value = 1;
    }
    if (quantity > maxStock) {
        quantity = maxStock;
        input.value = maxStock;
    }
    
    const total = unitPrice * quantity;
    
    console.log('Calculating:', { quantity, unitPrice, total });
    
    // Update displays
    document.getElementById('productTotal').textContent = formatCurrency(total);
    document.getElementById('finalTotal').textContent = formatCurrency(total);
    document.getElementById('totalAmountDisplay').textContent = formatCurrency(total);
    document.getElementById('formQuantity').value = quantity;
    
    // Check balance
    const needTopUp = total - userBalance;
    const insufficientBalanceInfo = document.getElementById('insufficientBalanceInfo');
    
    if (needTopUp > 0) {
        // Show warning section
        document.getElementById('needTopUpSection').style.display = 'flex';
        document.getElementById('needTopUpAmount').textContent = formatCurrency(needTopUp);
        
        // Show insufficient balance info
        if (insufficientBalanceInfo) {
            insufficientBalanceInfo.style.display = 'block';
        }
        
        // Disable submit button and change style
        document.getElementById('submitBtn').disabled = true;
        document.getElementById('submitBtn').classList.add('btn-secondary');
        document.getElementById('submitBtn').classList.remove('btn-primary');
        document.getElementById('submitBtn').textContent = '⛔ Không đủ tiền - Vui lòng nạp tiền';
        
        // Show alert banner if not already shown
        showInsufficientBalanceAlert(needTopUp);
    } else {
        // Hide warning section
        document.getElementById('needTopUpSection').style.display = 'none';
        
        // Hide insufficient balance info
        if (insufficientBalanceInfo) {
            insufficientBalanceInfo.style.display = 'none';
        }
        
        // Enable submit button
        document.getElementById('submitBtn').disabled = false;
        document.getElementById('submitBtn').classList.remove('btn-secondary');
        document.getElementById('submitBtn').classList.add('btn-primary');
        document.getElementById('submitBtn').textContent = 'Đặt hàng';
        
        // Hide alert banner if shown
        hideInsufficientBalanceAlert();
    }
}

/**
 * Increase quantity by 1
 */
function increaseQuantity() {
    const input = document.getElementById('quantityInput');
    let current = parseInt(input.value) || 1;
    if (current < maxStock) {
        current += 1;
        input.value = current;
        updateTotal();
    } else {
        alert('Số lượng không thể vượt quá tồn kho: ' + maxStock);
    }
}

/**
 * Decrease quantity by 1
 */
function decreaseQuantity() {
    const input = document.getElementById('quantityInput');
    let current = parseInt(input.value) || 1;
    if (current > 1) {
        current -= 1;
        input.value = current;
        updateTotal();
    }
}

/**
 * Format number as Vietnamese currency
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(amount)) + ' ₫';
}

/**
 * Show confirmation modal before purchase
 */
function confirmPurchase() {
    const quantity = parseInt(document.getElementById('quantityInput').value) || 1;
    const total = unitPrice * quantity;
    const remaining = userBalance - total;
    const needTopUp = total - userBalance;
    
    // Check if balance is sufficient
    if (needTopUp > 0) {
        // Show error alert with detailed information
        const alertMessage = `
️ Tài khoản của bạn không đủ tiền để thanh toán!

 Số dư hiện tại: ${formatCurrency(userBalance)}
 Tổng tiền cần thanh toán: ${formatCurrency(total)}
 Số tiền cần nạp thêm: ${formatCurrency(needTopUp)}

Vui lòng nạp tiền vào tài khoản để tiếp tục mua hàng.
Liên hệ admin để được hỗ trợ nạp tiền.
        `.trim();
        
        alert(alertMessage);
        return;
    }
    
    // Update modal content
    document.getElementById('confirmQuantity').textContent = quantity;
    document.getElementById('confirmTotalAmount').textContent = formatCurrency(total);
    document.getElementById('confirmBalance').textContent = formatCurrency(userBalance);
    
    if (remaining >= 0) {
        document.getElementById('confirmRemainingBalance').style.display = 'flex';
        document.getElementById('confirmRemainingAmount').textContent = formatCurrency(remaining);
        document.getElementById('confirmRemainingAmount').classList.remove('text-danger');
        document.getElementById('confirmRemainingAmount').classList.add('text-success');
    } else {
        document.getElementById('confirmRemainingBalance').style.display = 'none';
    }
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('confirmPurchaseModal'));
    modal.show();
}

/**
 * Submit purchase form after confirmation
 */
function submitPurchase() {
    try {
        console.log('🛒 [Frontend] Bắt đầu quá trình mua hàng...');
        
        // Double-check balance before submitting
        const quantity = parseInt(document.getElementById('quantityInput').value) || 1;
        const total = unitPrice * quantity;
        const needTopUp = total - userBalance;
        
        if (needTopUp > 0) {
            const alertMessage = `
 Tài khoản của bạn không đủ tiền!

Số dư hiện tại: ${formatCurrency(userBalance)}
Tổng tiền cần thanh toán: ${formatCurrency(total)}
Cần nạp thêm: ${formatCurrency(needTopUp)}

Vui lòng nạp tiền vào tài khoản để tiếp tục.
            `.trim();
            
            alert(alertMessage);
            return;
        }
        
        // Update message and quantity in form
        const message = document.getElementById('messageToSeller').value;
        
        document.getElementById('formMessage').value = message;
        document.getElementById('formQuantity').value = quantity;
        
        // Close modal
        const modalElement = document.getElementById('confirmPurchaseModal');
        if (modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }
        }
        
        console.log(' Submit form. Hệ thống sẽ xử lý và hold ...');
        
        // Submit form normally - backend will handle everything
        setTimeout(function() {
            document.getElementById('checkoutForm').submit();
        }, 100);
        
    } catch (error) {
        console.error(' Error in submitPurchase:', error);
        alert('Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.');
    }
}

/**
 * Show insufficient balance alert banner
 */
function showInsufficientBalanceAlert(needTopUp) {
    // Remove existing alert if any
    hideInsufficientBalanceAlert();
    
    // Create alert banner
    const alertDiv = document.createElement('div');
    alertDiv.id = 'insufficientBalanceAlert';
    alertDiv.className = 'alert alert-warning alert-dismissible fade show mt-3';
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" class="bi bi-exclamation-triangle-fill me-2" viewBox="0 0 16 16">
                <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
            </svg>
            <div class="flex-grow-1">
                <strong> Tài khoản không đủ tiền!</strong>
                <div class="mt-1">
                    Bạn cần nạp thêm <strong class="text-danger">${formatCurrency(needTopUp)}</strong> vào tài khoản để thanh toán đơn hàng này.
                    <br>
                    <small>Vui lòng liên hệ admin để được hỗ trợ nạp tiền.</small>
                </div>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    // Insert after the error message div or at the top of checkout container
    const checkoutContainer = document.querySelector('.checkout-container');
    const errorDiv = document.querySelector('.alert-danger');
    if (errorDiv && errorDiv.nextSibling) {
        checkoutContainer.insertBefore(alertDiv, errorDiv.nextSibling);
    } else if (checkoutContainer) {
        const h2 = checkoutContainer.querySelector('h2');
        if (h2 && h2.nextSibling) {
            checkoutContainer.insertBefore(alertDiv, h2.nextSibling);
        }
    }
}

/**
 * Hide insufficient balance alert banner
 */
function hideInsufficientBalanceAlert() {
    const existingAlert = document.getElementById('insufficientBalanceAlert');
    if (existingAlert) {
        existingAlert.remove();
    }
}

/**
 * Initialize event listeners on page load
 */
document.addEventListener('DOMContentLoaded', function() {
    // Prevent manual input of invalid values
    const quantityInput = document.getElementById('quantityInput');
    if (quantityInput) {
        quantityInput.addEventListener('blur', function() {
            updateTotal();
        });
    }
    
    // Update total on initial load
    updateTotal();
    
    // Attach event listener to confirm button
    const confirmBtn = document.getElementById('confirmPurchaseBtn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            submitPurchase();
        });
    }
});

