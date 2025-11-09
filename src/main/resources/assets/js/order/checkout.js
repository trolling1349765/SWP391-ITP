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
    if (needTopUp > 0) {
        document.getElementById('needTopUpSection').style.display = 'flex';
        document.getElementById('needTopUpAmount').textContent = formatCurrency(needTopUp);
        document.getElementById('submitBtn').disabled = true;
        document.getElementById('submitBtn').classList.add('btn-secondary');
        document.getElementById('submitBtn').classList.remove('btn-primary');
    } else {
        document.getElementById('needTopUpSection').style.display = 'none';
        document.getElementById('submitBtn').disabled = false;
        document.getElementById('submitBtn').classList.remove('btn-secondary');
        document.getElementById('submitBtn').classList.add('btn-primary');
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
        alert('Số dư không đủ! Vui lòng nạp thêm tiền.');
        return;
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
        
        // Update message and quantity in form
        const message = document.getElementById('messageToSeller').value;
        const quantity = parseInt(document.getElementById('quantityInput').value) || 1;
        
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
        
        console.log('📤 [Frontend] Submit form. Backend sẽ xử lý và hold 15 giây...');
        
        // Submit form normally - backend will handle everything
        setTimeout(function() {
            document.getElementById('checkoutForm').submit();
        }, 100);
        
    } catch (error) {
        console.error('❌ [Frontend] Error in submitPurchase:', error);
        alert('Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.');
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

