// Shop Profile Management with AJAX
let isEditMode = false;
let originalData = {};

// Enable edit mode
function enableEdit() {
    isEditMode = true;
    
    // Store original data
    originalData = {
        shopName: document.getElementById('shopName').value,
        category: document.getElementById('selectedCategories').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        shortDescription: document.getElementById('shortDescription').value,
        description: document.getElementById('description').value,
        facebookLink: document.getElementById('facebookLink') ? document.getElementById('facebookLink').value : ''
    };
    
    // Enable all form fields
    document.querySelectorAll('.edit-field').forEach(el => {
        el.readOnly = false;
        el.disabled = false;
    });
    
    // Enable upload buttons
    document.querySelectorAll('.edit-upload').forEach(el => {
        el.classList.remove('disabled');
        el.style.pointerEvents = 'auto';
        el.style.opacity = '1';
    });
    document.getElementById('logoInput').disabled = false;
    document.getElementById('bannerInput').disabled = false;
    
    // Show and enable terms checkbox
    const termsCard = document.getElementById('termsCard');
    const termsCheckbox = document.getElementById('agreeToTerms');
    if (termsCard && termsCheckbox) {
        termsCard.classList.remove('d-none');
        termsCheckbox.disabled = false;
        termsCheckbox.checked = false; // Reset checkbox
    }
    
    // Toggle buttons
    document.getElementById('editBtn').classList.add('d-none');
    document.getElementById('saveBtn').classList.remove('d-none');
    document.getElementById('cancelBtn').classList.remove('d-none');
    
    showAlert('info', '<i class="fas fa-info-circle me-2"></i>Chế độ chỉnh sửa đã bật');
}

// Update selected categories
function updateSelectedCategories() {
    const checkboxes = document.querySelectorAll('.category-checkbox:checked');
    const selected = Array.from(checkboxes).map(cb => cb.value);
    document.getElementById('selectedCategories').value = selected.join(',');
}

// Cancel edit
function cancelEdit() {
    isEditMode = false;
    
    // Restore original data
    document.getElementById('shopName').value = originalData.shopName;
    document.getElementById('selectedCategories').value = originalData.category;
    
    // Restore checkboxes
    const originalCategories = originalData.category.split(',');
    document.querySelectorAll('.category-checkbox').forEach(cb => {
        cb.checked = originalCategories.includes(cb.value);
    });
    
    document.getElementById('email').value = originalData.email;
    document.getElementById('phone').value = originalData.phone;
    document.getElementById('shortDescription').value = originalData.shortDescription;
    document.getElementById('description').value = originalData.description;
    if (document.getElementById('facebookLink')) {
        document.getElementById('facebookLink').value = originalData.facebookLink;
    }
    
    // Disable all form fields
    document.querySelectorAll('.edit-field').forEach(el => {
        el.readOnly = true;
        el.disabled = true;
    });
    
    // Disable upload buttons
    document.querySelectorAll('.edit-upload').forEach(el => {
        el.classList.add('disabled');
        el.style.pointerEvents = 'none';
        el.style.opacity = '0.6';
    });
    document.getElementById('logoInput').disabled = true;
    document.getElementById('bannerInput').disabled = true;
    
    // Hide and disable terms checkbox
    const termsCard = document.getElementById('termsCard');
    const termsCheckbox = document.getElementById('agreeToTerms');
    if (termsCard && termsCheckbox) {
        termsCard.classList.add('d-none');
        termsCheckbox.disabled = true;
        termsCheckbox.checked = false;
    }
    
    // Toggle buttons
    document.getElementById('editBtn').classList.remove('d-none');
    document.getElementById('saveBtn').classList.add('d-none');
    document.getElementById('cancelBtn').classList.add('d-none');
    
    // Clear alert
    document.getElementById('alertContainer').innerHTML = '';
}

// Save profile using AJAX
function saveProfile() {
    const form = document.getElementById('shopProfileForm');
    const formData = new FormData(form);
    
    // Validate
    if (!validateForm()) {
        return;
    }
    
    // Show loading
    const saveBtn = document.getElementById('saveBtn');
    const originalHTML = saveBtn.innerHTML;
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang lưu...';
    
    fetch('/itp/shop/profile/update', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.redirected) {
            window.location.href = response.url;
        } else {
            return response.text();
        }
    })
    .then(data => {
        if (data) {
            showAlert('success', '<i class="fas fa-check-circle me-2"></i>Cập nhật thành công!');
            cancelEdit();
            setTimeout(() => window.location.reload(), 1500);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', '<i class="fas fa-exclamation-circle me-2"></i>Lỗi: ' + error.message);
        saveBtn.disabled = false;
        saveBtn.innerHTML = originalHTML;
    });
}

// Validate form
function validateForm() {
    const shopName = document.getElementById('shopName').value.trim();
    const categories = document.getElementById('selectedCategories').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const agreeToTerms = document.getElementById('agreeToTerms');
    
    if (!shopName) {
        showAlert('warning', 'Vui lòng nhập tên shop');
        return false;
    }
    
    if (!categories) {
        showAlert('warning', 'Vui lòng chọn ít nhất 1 danh mục');
        return false;
    }
    
    if (!email || !isValidEmail(email)) {
        showAlert('warning', 'Email không hợp lệ');
        return false;
    }
    
    if (!phone || !isValidPhone(phone)) {
        showAlert('warning', 'Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)');
        return false;
    }
    
    // Check terms and conditions
    if (agreeToTerms && !agreeToTerms.disabled && !agreeToTerms.checked) {
        showAlert('warning', '<i class="fas fa-exclamation-triangle me-2"></i>Bạn phải đồng ý với điều khoản dịch vụ để cập nhật thông tin');
        return false;
    }
    
    return true;
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPhone(phone) {
    return /^0[0-9]{9}$/.test(phone);
}

// Show alert
function showAlert(type, message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.getElementById('alertContainer');
    container.innerHTML = '';
    container.appendChild(alertDiv);
    
    setTimeout(() => {
        const bsAlert = bootstrap.Alert.getInstance(alertDiv) || new bootstrap.Alert(alertDiv);
        bsAlert.close();
    }, 5000);
}

// Preview functions
function previewLogo(event) {
    const file = event.target.files[0];
    if (file) {
        if (file.size > 5 * 1024 * 1024) {
            alert('File quá lớn! Tối đa 5MB');
            event.target.value = '';
            return;
        }
        const reader = new FileReader();
        reader.onload = function(e) {
            const logoImg = document.getElementById('logoPreview');
            const logoPlaceholder = document.getElementById('logoPlaceholder');
            logoImg.src = e.target.result;
            logoImg.style.display = 'block';
            logoPlaceholder.style.display = 'none';
        }
        reader.readAsDataURL(file);
    }
}

function previewBanner(event) {
    const file = event.target.files[0];
    if (file) {
        if (file.size > 5 * 1024 * 1024) {
            alert('File quá lớn! Tối đa 5MB');
            event.target.value = '';
            return;
        }
        const reader = new FileReader();
        reader.onload = function(e) {
            const bannerImg = document.getElementById('bannerPreview');
            const bannerPlaceholder = document.getElementById('bannerPlaceholder');
            bannerImg.src = e.target.result;
            bannerImg.style.display = 'block';
            bannerPlaceholder.style.display = 'none';
        }
        reader.readAsDataURL(file);
    }
}

function updateCharCount() {
    const textarea = document.getElementById('shortDescription');
    const count = document.getElementById('shortDescCount');
    if (textarea && count) {
        const length = textarea.value.length;
        count.textContent = length;
        
        if (length > 450) {
            count.classList.add('text-danger');
            count.classList.remove('text-muted');
        } else {
            count.classList.add('text-muted');
            count.classList.remove('text-danger');
        }
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    updateCharCount();
    
    // Add event listeners to category checkboxes
    document.querySelectorAll('.category-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', updateSelectedCategories);
    });
    
    // Initialize selected categories
    updateSelectedCategories();
});

