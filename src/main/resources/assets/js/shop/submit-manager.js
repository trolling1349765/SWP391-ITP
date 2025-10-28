/**
 * Submit Manager
 * Handles form submission, validation, and success animations
 */
class SubmitManager {
    constructor() {
        this.form = document.querySelector('form');
        this.submitButton = document.querySelector('button[type="submit"]');
        this.successManager = null;
        this.isSubmitting = false;
        
        this.init();
    }

    init() {
        this.successManager = new SuccessAnimationManager();
        
        if (this.form) {
            this.form.addEventListener('submit', this.handleSubmit.bind(this));
        }
        
        if (this.submitButton) {
            this.submitButton.addEventListener('click', this.handleSubmitClick.bind(this));
        }
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
        
        return this.confirmSubmit();
    }

    handleSubmit(event) {
        if (this.isSubmitting) {
            event.preventDefault();
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

    confirmSubmit() {
        const productName = document.querySelector('input[name="productName"]').value;
        const excelFile = document.getElementById('serialFileInput').files[0];
        
        let confirmMessage = `Bạn có chắc chắn muốn tạo sản phẩm "${productName}"?`;
        
        if (excelFile) {
            confirmMessage += `\n\nFile Excel "${excelFile.name}" sẽ được xử lý để thêm serials.`;
        }
        
        const confirmed = confirm(confirmMessage);
        
        if (confirmed) {
            this.setSubmittingState(true);
            this.setupSuccessAnimation(productName);
        }
        
        return confirmed;
    }

    setupSuccessAnimation(productName) {
        // Add form submit listener for success
        const form = document.querySelector('form');
        if (form) {
            form.addEventListener('submit', () => {
                // This will be called after successful submission
                setTimeout(() => {
                    this.successManager.showSuccess(
                        `Sản phẩm "${productName}" đã được tạo thành công!`,
                        'Bạn có thể tiếp tục thêm sản phẩm khác hoặc quay về dashboard.'
                    );
                }, 1000);
            });
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

// Initialize submit manager when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.submitManager = new SubmitManager();
});

// Export for use in other modules
window.SubmitManager = SubmitManager;
