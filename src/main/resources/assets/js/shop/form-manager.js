/**
 * Form Manager
 * Handles form validation, state management, and user interactions
 */
class FormManager {
    constructor() {
        this.form = document.querySelector('form');
        this.submitButton = document.querySelector('button[type="submit"]');
        this.warningElement = null;
        this.isSubmitting = false;
        
        this.init();
    }

    init() {
        if (this.form) {
            this.form.addEventListener('submit', this.handleSubmit.bind(this));
        }
        
        if (this.submitButton) {
            this.submitButton.addEventListener('click', this.handleSubmitClick.bind(this));
        }
        
        // Initialize form validation
        this.initializeValidation();
    }

    initializeValidation() {
        const requiredFields = this.form.querySelectorAll('[required]');
        
        requiredFields.forEach(field => {
            field.addEventListener('blur', () => this.validateField(field));
            field.addEventListener('input', () => this.clearFieldError(field));
        });
    }

    validateField(field) {
        const value = field.value.trim();
        const isValid = value !== '' && field.checkValidity();
        
        if (!isValid) {
            this.showFieldError(field, 'Trường này là bắt buộc');
        } else {
            this.clearFieldError(field);
        }
        
        return isValid;
    }

    showFieldError(field, message) {
        this.clearFieldError(field);
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error text-danger mt-1';
        errorDiv.textContent = message;
        
        field.parentNode.appendChild(errorDiv);
        field.classList.add('is-invalid');
    }

    clearFieldError(field) {
        const errorDiv = field.parentNode.querySelector('.field-error');
        if (errorDiv) {
            errorDiv.remove();
        }
        field.classList.remove('is-invalid');
    }

    validateForm() {
        const requiredFields = this.form.querySelectorAll('[required]');
        let isValid = true;
        
        requiredFields.forEach(field => {
            if (!this.validateField(field)) {
                isValid = false;
            }
        });
        
        return isValid;
    }

    handleSubmitClick(event) {
        if (this.isSubmitting) {
            event.preventDefault();
            return false;
        }
        
        // Check for duplicate serials before allowing submit
        if (!this.checkSerialValidation()) {
            event.preventDefault();
            return false;
        }
        
        return true;
    }

    handleSubmit(event) {
        if (this.isSubmitting) {
            event.preventDefault();
            return false;
        }
        
        if (!this.validateForm()) {
            event.preventDefault();
            this.showFormError('Vui lòng điền đầy đủ thông tin bắt buộc');
            return false;
        }
        
        if (!this.checkSerialValidation()) {
            event.preventDefault();
            return false;
        }
        
        this.setSubmittingState(true);
        return true;
    }

    checkSerialValidation() {
        const previewDiv = document.getElementById('excelPreview');
        
        if (!previewDiv || previewDiv.style.display === 'none') {
            return true; // No Excel file uploaded, allow submit
        }
        
        // Check for duplicate badges
        const duplicateBadges = previewDiv.querySelectorAll('.badge.bg-danger, .badge.bg-warning');
        if (duplicateBadges.length > 0) {
            this.showSerialError('Không thể tạo sản phẩm vì có serial trùng!');
            return false;
        }
        
        // Check for valid serials
        const validBadges = previewDiv.querySelectorAll('.badge.bg-success');
        if (validBadges.length === 0) {
            this.showSerialError('Không có serial hợp lệ nào để thêm!');
            return false;
        }
        
        return true;
    }

    showSerialError(message) {
        alert(`${message}\n\nVui lòng:\n- Xóa các serial trùng trong file Excel\n- Hoặc xóa file và upload file mới\n- Hoặc sử dụng serial khác`);
    }

    showFormError(message) {
        this.hideWarning();
        
        this.warningElement = document.createElement('div');
        this.warningElement.className = 'alert alert-danger mt-3';
        this.warningElement.innerHTML = `
            <strong>Lỗi:</strong> ${message}
        `;
        
        this.form.insertBefore(this.warningElement, this.form.firstChild);
        
        // Auto hide after 5 seconds
        setTimeout(() => {
            this.hideWarning();
        }, 5000);
    }

    hideWarning() {
        if (this.warningElement) {
            this.warningElement.remove();
            this.warningElement = null;
        }
    }

    setSubmittingState(isSubmitting) {
        this.isSubmitting = isSubmitting;
        
        if (this.submitButton) {
            if (isSubmitting) {
                this.submitButton.disabled = true;
                this.submitButton.classList.add('btn-loading');
                this.submitButton.innerHTML = '<span class="btn-text">Đang xử lý...</span>';
            } else {
                this.submitButton.disabled = false;
                this.submitButton.classList.remove('btn-loading');
                this.submitButton.innerHTML = 'Tạo sản phẩm';
            }
        }
    }

    resetForm() {
        if (this.form) {
            this.form.reset();
        }
        
        this.hideWarning();
        this.setSubmittingState(false);
        
        // Clear Excel preview
        const previewDiv = document.getElementById('excelPreview');
        if (previewDiv) {
            previewDiv.style.display = 'none';
            previewDiv.innerHTML = '';
        }
        
        // Clear file info
        const fileInfo = document.getElementById('fileInfo');
        if (fileInfo) {
            fileInfo.innerHTML = '';
        }
        
        // Reset submit button state
        this.updateSubmitButtonState(0, 0, 0);
    }

    updateSubmitButtonState(validCount, duplicateCount, dbDuplicateCount) {
        const totalDuplicates = duplicateCount + dbDuplicateCount;
        
        if (!this.submitButton) return;
        
        if (totalDuplicates > 0) {
            // Disable submit button if there are duplicates
            this.submitButton.disabled = true;
            this.submitButton.classList.remove('btn-primary');
            this.submitButton.classList.add('btn-secondary');
            this.submitButton.title = `Không thể submit vì có ${totalDuplicates} serial trùng`;
            
            this.showSubmitWarning(`Không thể tạo sản phẩm vì có ${totalDuplicates} serial trùng. Vui lòng sửa file Excel.`);
        } else if (validCount > 0) {
            // Enable submit button if there are valid serials
            this.submitButton.disabled = false;
            this.submitButton.classList.remove('btn-secondary');
            this.submitButton.classList.add('btn-primary');
            this.submitButton.title = `Có thể tạo sản phẩm với ${validCount} serial hợp lệ`;
            
            this.hideSubmitWarning();
        } else {
            // Disable if no valid serials
            this.submitButton.disabled = true;
            this.submitButton.classList.remove('btn-primary');
            this.submitButton.classList.add('btn-secondary');
            this.submitButton.title = 'Không có serial hợp lệ nào';
            
            this.showSubmitWarning('Không có serial hợp lệ nào để tạo sản phẩm.');
        }
    }

    showSubmitWarning(message) {
        this.hideSubmitWarning();
        
        const warningDiv = document.createElement('div');
        warningDiv.id = 'submitWarning';
        warningDiv.className = 'submit-warning';
        warningDiv.innerHTML = `
            <span class="warning-icon">!</span>
            ${message}
        `;
        
        this.submitButton.parentNode.appendChild(warningDiv);
    }

    hideSubmitWarning() {
        const warningMsg = document.getElementById('submitWarning');
        if (warningMsg) {
            warningMsg.remove();
        }
    }
}

// Initialize form manager when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.formManager = new FormManager();
});

// Export for use in other modules
window.FormManager = FormManager;
